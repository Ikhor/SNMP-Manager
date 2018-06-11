/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Manager;

import java.io.*;
import javax.swing.JTextPane;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/*
Versão para SNMP V1
"1.3.6.1.2.1.2.2.1.10.12";//ifInOctets
"1.3.6.1.2.1.2.2.1.16.12";//ifOutOctets
"1.3.6.1.2.1.2.2.1.5.12";//ifSpeed
".1.3.6.1.2.1.1.3.0";//SysUpTime
*/

public class GerenteV1
{
  private static String  ipAddress  = "10.0.0.102";
  private static String  port    = "161";
  private static String  community  = "public";
  
  private static String  oidValueSysUpTime  = ".1.3.6.1.2.1.1.3.0";//SysUpTime
  private static String  oidValueifInOctets = "1.3.6.1.2.1.2.2.1.10.12";//ifOutOctets
  private static String  oidValueifOutOctets = "1.3.6.1.2.1.2.2.1.16.12";//ifOutOctets
  private static String  oidValueifSpeed = "1.3.6.1.2.1.2.2.1.5.12";//ifSpeed
  private static String resposta;
  
  private static int     snmpVersion  = SnmpConstants.version1;
  
  
  public String getResponse(){
      return resposta;
  }
  
  public GerenteV1(){}
  
  public GerenteV1(String ipAdress,String port,String community){
      GerenteV1.ipAddress = ipAdress;
      GerenteV1.port = port;
      GerenteV1.community = community;
  }
   
  
  public void UtilizacaoRede() throws Exception{

            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(new OctetString(community));
            comtarget.setVersion(snmpVersion);
            comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
            comtarget.setRetries(2);
            comtarget.setTimeout(1000);
            comtarget.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);

            // Cria a PDU
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oidValueSysUpTime)));
            pdu.add(new VariableBinding(new OID(oidValueifInOctets)));
            pdu.add(new VariableBinding(new OID(oidValueifOutOctets)));
            pdu.setType(PDU.GET);
            pdu.setRequestID(new Integer32(1));

            Snmp snmp = new Snmp(transport);
            ResponseEvent response = snmp.get(pdu, comtarget);

            //Processo Response
            if (response != null){                            
                    PDU responsePDU = response.getResponse();
                    if (responsePDU != null){
                            int errorStatus = responsePDU.getErrorStatus();
                            int errorIndex = responsePDU.getErrorIndex();
                            String errorStatusText = responsePDU.getErrorStatusText();
                            if (errorStatus == PDU.noError){
                                File arq;
                                FileWriter write;
                                PrintWriter escrever;
                                try{ 
                                    arq = new File("response.csv");
                                    write = new FileWriter(arq);
                                    escrever = new PrintWriter(write);
                                    resposta = responsePDU.getVariableBindings().toString();
                                    String dados[] = resposta.split(",");
                                    
                                    String dadosV2[] = dados[2].split("=");
                                    
                                    String dadosV3[] = dados[3].split("=");
                                    dadosV3 = dadosV3[1].split("]");
                                    
                                    resposta = dados[1].substring(1) + "," + dadosV2[1] + "," + dadosV3[0];
                                    
                                    escrever.println(resposta);
                                    
                                    
                                    escrever.close();
                                    write.close();
                                }catch(IOException e){
                                    e.printStackTrace();
                                }
                                //System.out.println("Snmp Get Response = " + responsePDU.getVariableBindings());
                            }
                            else{
                                    System.out.println("Error: Request Falhou!");
                                    System.out.println("Error Status = " + errorStatus);
                                    System.out.println("Error Index = " + errorIndex);
                                    System.out.println("Error Status Text = " + errorStatusText);
                            }
                    }
                    else{
                            System.out.println("Error: PDU de reposta é null");
                    }
            }
            else{
                    System.out.println("Error: Tempo excedido... ");
            }
            
    snmp.close();
    }

  public void ValueifSpeed() throws IOException{


                TransportMapping transport = new DefaultUdpTransportMapping();
                transport.listen();

                CommunityTarget comtarget = new CommunityTarget();
                comtarget.setCommunity(new OctetString(community));
                comtarget.setVersion(snmpVersion);
                comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
                comtarget.setRetries(2);
                comtarget.setTimeout(1000);

                // Cria a PDU
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(new OID(oidValueifSpeed)));
                pdu.setType(PDU.GET);
                pdu.setRequestID(new Integer32(1));


                Snmp snmp = new Snmp(transport);

            // System.out.println("Enviando Request...");
                ResponseEvent response = snmp.get(pdu, comtarget);

                // Processo Response
                if (response != null)
                {
                // System.out.println("lendo resposta...");
                PDU responsePDU = response.getResponse();

                if (responsePDU != null)
                {
                    int errorStatus = responsePDU.getErrorStatus();
                    int errorIndex = responsePDU.getErrorIndex();
                    String errorStatusText = responsePDU.getErrorStatusText();

                    if (errorStatus == PDU.noError)
                    {
                    System.out.println("Snmp Get Response = " + responsePDU.getVariableBindings());
                    }
                    else
                    {
                    System.out.println("Error: Request Falhou!");
                    System.out.println("Error Status = " + errorStatus);
                    System.out.println("Error Index = " + errorIndex);
                    System.out.println("Error Status Text = " + errorStatusText);
                    }
                }
                else
                {
                    System.out.println("Error: PDU de reposta é null");
                }
                }
                else
                {
                System.out.println("Error: Tempo excedido... ");
                }
                snmp.close();
    }
  
  
    public static void main(String[] args) throws Exception{
            System.out.println("SNMP Manager");
         
            GerenteV1 Manager = new GerenteV1();
            Manager.ValueifSpeed();


            Manager.UtilizacaoRede();
            System.out.println("Iniciando Intervalo de 30 segundos");
            Thread.sleep( 30 * 1000 ); // aguarda 30segundos 
            Manager.UtilizacaoRede();
    }
    
}