/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projetosnmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * 
 */
public class SNMPV1 extends Thread{

    /**
     * Utilizaremos threads, pois pretendemos chamar o método Sleep
     */
    //Variáveis necessárias
    //Variáveis para guardar valores retornados dos objetos da MIBII. Utilizaremos LONG para termos uma certa segunda segurança com relação ao tamanho dos dados
    
    //Variáveis abaixo, referenciam-se aos octetos de entrada
    long inOctetsInicial;           //Octetos de entrada inicial
    long inOctetsFinal;             //Octetos de entrada final
    long deltaInOctets;             //Variação de octetos de entrada
    
    //Variáveis abaixo, referenciam-se aos octetos de saída
    long outOctetsInicial;          //Octetos de saída inicial
    long outOctetsFinal;            //Octetos de saída final
    long deltaOutOctets;            //Variação de octetos de saída
    
    //Variáveis abaixo, referenciam-se ao tempo sysUpTime
    long sysUpTimeInicial;          //Tempo inicial
    long sysUpTimeFinal;            //Tempo final
    long deltaSysUpTime;            //Variação do tempo
    
    //Variáveis para tempo de resposta das PDUs
    double tempoInicial;
    double tempoFinal;
    double [] deltaTempo = new double[35];      //Serão feitas 35 requisições
    
    long ifSpeed;                   //ifSpeed
    
    // Variáveis que contém os números dos objetos MIBII
    private String  enderecoIP;                   //IP
    private String  porta    = "161";
    private String  valor1  = "1.3.6.1.2.1.2.2.1.10.3"; //ifInOctets.1
    private String  valor2  = "1.3.6.1.2.1.2.2.1.16.3"; //ifOutOctets.1
    private String  valor3  = "1.3.6.1.2.1.1.3.0";      //sysUpTime.0 || Objeto escalar(terminado com 0)
    private String  valor4  = "1.3.6.1.2.1.2.2.1.5.2";  //ifSpeed.1 
    
    private int    versaoSNMP  = SnmpConstants.version1;
    private String  comunidade  = "public";
    
    //Variáveis para médias
    double[] utilInterface = new double[35];             //Para cada vez deve calcular a utilização da interface
    double mediaUtil;                                   //Utilizaremos a variavel anterior para o calcula desta
    double desvioPadrao;                                //Desvio padrão do processo
    
    public SNMPV1(String ip){
        this.enderecoIP = ip;                           //Recendo o ip
    }
    
    //Método run, onde as requisições serão mandadas e respostas esperadas serão recebidas
    public void run(){
    
    try{
        //Configurações para o gerente
        TransportMapping transport;
        transport = new DefaultUdpTransportMapping();
        transport.listen();


        // Criando o objeto de endereço
        CommunityTarget comtarget = new CommunityTarget();
        comtarget.setCommunity(new OctetString(comunidade));    //comunidade public
        comtarget.setVersion(versaoSNMP);                       //Versão SNMP
        comtarget.setAddress(new UdpAddress(enderecoIP + "/" + porta)); 
        comtarget.setRetries(2);
        comtarget.setTimeout(1000);
        
        //A partir daqui será montada a PDU
        //Precisaremos de duas PDUs, uma contendo a requisição de ifSpeed, e outra não
        //PDU1 com ifSpeed
        PDU pdu1 = new PDU();
        pdu1.add(new VariableBinding(new OID(valor1))); //ifInOctets
        pdu1.add(new VariableBinding(new OID(valor2))); //ifOutOctets
        pdu1.add(new VariableBinding(new OID(valor3))); //sysUpTime
        pdu1.add(new VariableBinding(new OID(valor4))); //ifSpeed
        pdu1.setType(PDU.GET);
        pdu1.setRequestID(new Integer32(1));
        
        //Segunda PDU, sem ifSpeed
        PDU pdu2 = new PDU();
        pdu2.add(new VariableBinding(new OID(valor1))); //ifInOctets
        pdu2.add(new VariableBinding(new OID(valor2))); //ifOutOctets
        pdu2.add(new VariableBinding(new OID(valor3))); //sysUpTime
        pdu2.setType(PDU.GET);
        pdu2.setRequestID(new Integer32(1));
        
        //Objeto snmp que será enviado para o agente
        Snmp snmp = new Snmp(transport);
        
        //A partir daqui, acontecerá o envio de PDUs e o recepção de respostas
        boolean primeiraPDU = true;
        for(int c=0; c<35; c++){
            // Hora do envio
            System.out.println("Requisicao " + 1 + " da vez " + c);
            tempoInicial = System.currentTimeMillis();
            if (primeiraPDU){
                requisicaoPDU1(snmp, pdu1,comtarget);
                primeiraPDU = false;
            }    
            else
                requisicaoPDU2(snmp, pdu2, comtarget);

            // aguardar 2s
            this.sleep(15000);
            System.out.println("Requisicao " + 2 + " da vez " + c);
            requisicaoPDU3(snmp, pdu2, comtarget);
            
            // Hora do recebimento
            tempoFinal = System.currentTimeMillis();

            // calculando as diferenças
            deltaTempo[c] = tempoFinal-tempoInicial;
            deltaInOctets = inOctetsFinal-inOctetsInicial;
            //System.out.println("octetos de entrada: " + deltaInOctets);
            deltaOutOctets = outOctetsFinal-outOctetsInicial;
            //System.out.println("octetos de saída: " + deltaOutOctets);
            deltaSysUpTime = sysUpTimeFinal-sysUpTimeInicial;
            //System.out.println("SysupTime: " + deltaSysUpTime);
            //System.out.println("IfSpeed: " + ifSpeed);

            // caculo da utilizacao da interface de rede
            utilInterface[c]=utilInterface(deltaInOctets,deltaOutOctets,deltaSysUpTime,ifSpeed);
            System.out.println("Tempo de resposta: " + deltaTempo[c]);
            System.out.println("Utilização da interface na vez " + c + " " + utilInterface[c]);

            // aguardar 5s
            this.sleep(20000);
        }
        snmp.close();
        double resultadoMedia = calculaMedia(utilInterface);
        System.out.println("Média da utilização da Interface: " + resultadoMedia);
        double resultadoDesvioPadrao = desvioPadrao(utilInterface, resultadoMedia);
        System.out.println("Desvio Padrão: " + resultadoDesvioPadrao);
   
        }catch (Exception ex) {
            Logger.getLogger(SNMPV1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double utilInterface(long deltaInOctets, long deltaOutOctets, long deltaSysUpTime, long ifSpeed){
        //Calculo da UTILIZAÇÃO DA INTERFACE
        double soma = deltaInOctets+deltaOutOctets;
        double divisao = deltaSysUpTime/100;
        double multiplicacao = ifSpeed*divisao;
        double resultado = soma/multiplicacao;
        return 8*resultado*100;
    }
    
    public double calculaMedia(double[] utilInterface){
        //Vamos calcular a média da Utilização da Interface
        for(int c=0; c<35; c++){
            mediaUtil+=utilInterface[c];
        }
        return mediaUtil/35;
    }
    
    public double desvioPadrao(double[] utilInterface, double media){
        /*Formula do desvio padrão*/
        double[] aoQuadrado = new double[35];
        double subtracao=0;
        double mediaParc=0;
        double divisao=0;
        for(int c=0; c<35; c++){
            subtracao=mediaUtil-utilInterface[c];
            aoQuadrado[c]=subtracao*subtracao;
        }
        for(int c=0; c<35; c++){
            mediaParc+=aoQuadrado[c];
        }
        divisao=mediaParc/(35-1);
        return Math.sqrt(divisao);
    }
    
    public void transformaTempo(long sysUpTime){
        long tempo = sysUpTime/100;
        long hora = tempo/3600;
        long minuto = (tempo - (hora*3600))/60;
        long segundos = (tempo - (hora*3600) - (minuto*60));
        long milisegundos =  tempo - (hora*3600) - (minuto*60) - segundos;
        
        System.out.println(hora + ":" + minuto + ":" + segundos + "." + milisegundos);
    }
    
    public void requisicaoPDU1(Snmp snmp, PDU pdu, CommunityTarget comtarget)throws Exception{

        
        ResponseEvent resposta = snmp.get(pdu, comtarget);  //Resposta do agente
        if (resposta != null)                               //Se a resposta não for nula
        {
            PDU respostaPDU = resposta.getResponse();           //Extraindo essa resposta
       
            if (respostaPDU != null)                            //Resposta extraída
            {
                int errorStatus = respostaPDU.getErrorStatus();


                if (errorStatus == PDU.noError)
                {
                    // captura do ifInOctets inicial
                    inOctetsInicial = respostaPDU.get(0).getVariable().toLong();
                    System.out.println(valor1 + ": " + inOctetsInicial);
                    // captura do ifOutOctets inicial
                    outOctetsInicial = respostaPDU.get(1).getVariable().toLong();
                    System.out.println(valor2 + ": " + outOctetsInicial);
                    // captura do sysUpTime inicial - milisegundos
                    sysUpTimeInicial = respostaPDU.get(2).getVariable().toLong();
                    System.out.print(valor3 + ": " + sysUpTimeInicial + " tempo convertido->");
                    transformaTempo(sysUpTimeInicial);
                    // captura do ifSpeed inicial - milisegundos
                    ifSpeed =  respostaPDU.get(3).getVariable().toLong();
                    System.out.println(valor4 + ": " + ifSpeed);

                }
            }

        }
    }
    
    public void requisicaoPDU2(Snmp snmp, PDU pdu, CommunityTarget comtarget)throws Exception{

        //Sem buscar o ifSPEED
        ResponseEvent resposta = snmp.get(pdu, comtarget);  //Resposta do agente
        if (resposta != null)                               //Se a resposta não for nula
        {
            PDU respostaPDU = resposta.getResponse();           //Extraindo essa resposta
       
            if (respostaPDU != null)                            //Resposta extraída
            {
                int errorStatus = respostaPDU.getErrorStatus();


                if (errorStatus == PDU.noError)
                {
                    // captura do ifInOctets inicial
                    inOctetsInicial = respostaPDU.get(0).getVariable().toLong();
                    System.out.println(valor1 + ": " + inOctetsInicial);
                    // captura do ifOutOctets inicial
                    outOctetsInicial = respostaPDU.get(1).getVariable().toLong();
                    System.out.println(valor2 + ": " + outOctetsInicial);
                    // captura do sysUpTime inicial - milisegundos
                    sysUpTimeInicial = respostaPDU.get(2).getVariable().toLong();
                    System.out.print(valor3 + ": " + sysUpTimeInicial + " tempo convertido ->");
                    transformaTempo(sysUpTimeInicial);

                }
            }

        }
    }
    
    public void requisicaoPDU3(Snmp snmp, PDU pdu, CommunityTarget comtarget)throws Exception{

            
        ResponseEvent resposta = snmp.get(pdu, comtarget);  //Resposta do agente
        if (resposta != null)                               //Se a resposta não for nula
        {
            PDU respostaPDU = resposta.getResponse();           //Extraindo essa resposta
       
            if (respostaPDU != null)                            //Resposta extraída
            {
                int errorStatus = respostaPDU.getErrorStatus();

                if (errorStatus == PDU.noError)
                {     
                    // captura do ifInOctets inicial
                    inOctetsFinal = respostaPDU.get(0).getVariable().toLong();
                    System.out.println(valor1 + ": " + inOctetsFinal);
                    // captura do ifOutOctets inicial
                    outOctetsFinal = respostaPDU.get(1).getVariable().toLong();
                    System.out.println(valor1 + ": " + outOctetsFinal);
                    // captura do sysUpTime inicial - milisegundos
                    sysUpTimeFinal = respostaPDU.get(2).getVariable().toLong();
                    System.out.print(valor2 + ": " + sysUpTimeFinal + " tempo convertido ->");
                    transformaTempo(sysUpTimeFinal);
                }
            }

           }
        }  
}
