import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/*
Versão para SNMP V1
"1.3.6.1.2.1.2.2.1.10.12";//ifInOctets
"1.3.6.1.2.1.2.2.1.16.12";//ifOutOctets
"1.3.6.1.2.1.2.2.1.5.12";//ifSpeed
".1.3.6.1.2.1.1.3.0";//SysUpTime
*/

public class SNMPManager
{
  private static String  ipAddress  = "10.0.0.102";
  private static String  port    = "161";

  private static String  oidValueSysUpTime  = ".1.3.6.1.2.1.1.3.0";//SysUpTime
  private static String  oidValueifInOctets = "1.3.6.1.2.1.2.2.1.10.12";//ifOutOctets
  private static String  oidValueifOutOctets = "1.3.6.1.2.1.2.2.1.16.12";//ifOutOctets
  private static String  oidValueifSpeed = "1.3.6.1.2.1.2.2.1.5.12";//ifSpeed
  
  private static int     snmpVersion  = SnmpConstants.version1;
  private static String  community  = "public";
  
  
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
	 //System.out.println("Enviando Request...");
	 ResponseEvent response = snmp.get(pdu, comtarget);
	 
	 //Processo Response
	 if (response != null){
		 //System.out.println("lendo resposta...");
		 PDU responsePDU = response.getResponse();
		 if (responsePDU != null){
			 int errorStatus = responsePDU.getErrorStatus();
			 int errorIndex = responsePDU.getErrorIndex();
			 String errorStatusText = responsePDU.getErrorStatusText();
			 if (errorStatus == PDU.noError){
				 System.out.println("Snmp Get Response = " + responsePDU.getVariableBindings());
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
  
  public static void main(String[] args) throws Exception
  {
	  System.out.println("SNMP Manager");
	  
	  SNMPManager Manager = new SNMPManager();
	  Manager.ValueifSpeed();
	  
	  
	  Manager.UtilizacaoRede();
	  System.out.println("Iniciando Intervalo de 30 segundos");
	  Thread.sleep( 30 * 1000 ); // aguarda 30segundos 
	  Manager.UtilizacaoRede();
  }
    
}