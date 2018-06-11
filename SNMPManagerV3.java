import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/*
Versão para SNMP V3
"1.3.6.1.2.1.2.2.1.10.12";//ifInOctets
"1.3.6.1.2.1.2.2.1.16.12";//ifOutOctets
"1.3.6.1.2.1.2.2.1.5.12";//ifSpeed
".1.3.6.1.2.1.1.3.0";//SysUpTime
*/

public class SNMPManagerV3
{
  private static String  ipAddress  = "10.0.0.102";
  private static String  port       = "161";

  private static String  oidValueSysUpTime  = ".1.3.6.1.2.1.1.3.0";//SysUpTime
  private static String  oidValueifInOctets = "1.3.6.1.2.1.2.2.1.10.12";//ifOutOctets
  private static String  oidValueifOutOctets = "1.3.6.1.2.1.2.2.1.16.12";//ifOutOctets
  private static String  oidValueifSpeed = "1.3.6.1.2.1.2.2.1.5.12";//ifSpeed
  
  private static int     snmpVersion  = SnmpConstants.version3;
  
  
public void UtilizacaoRede() throws Exception{
	
	  System.out.println("SNMPV3 Manager");
	  
	  TransportMapping transport = new DefaultUdpTransportMapping();
	  transport.listen();
	  Snmp snmp = new Snmp(transport);
	  
	  USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
	  SecurityModels.getInstance().addSecurityModel(usm);

	  snmp.getUSM().addUser(new OctetString("MD5DES"),
			  new UsmUser(new OctetString("MD5DES"),
                        AuthMD5.ID,
                        new OctetString("MD5DESUserAuthPassword"),
                        PrivDES.ID,
                        new OctetString("MD5DESUserPrivPassword")));
	    

	  	UserTarget  Utarget = new UserTarget();
	  	Utarget.setVersion(snmpVersion);
	  	Utarget.setAddress(new UdpAddress(ipAddress + "/" + port));
	  	Utarget.setRetries(2);
	  	Utarget.setTimeout(1000);
	  	Utarget.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
	  	Utarget.setSecurityName(new OctetString("MD5DES"));
	    

	    // Cria a PDU
	    PDU pdu = new ScopedPDU();
		pdu.add(new VariableBinding(new OID(oidValueSysUpTime)));
		pdu.add(new VariableBinding(new OID(oidValueifInOctets)));
		pdu.add(new VariableBinding(new OID(oidValueifOutOctets)));
	    pdu.setType(PDU.GETNEXT);	    
	    

	    System.out.println("Enviando Request...");
	    ResponseEvent response = snmp.get(pdu, Utarget);
	 
	 //Processo Response
	 if (response != null){
		 System.out.println("lendo resposta...");
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
	  Snmp snmp = new Snmp(transport);
	  
	  USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
	  SecurityModels.getInstance().addSecurityModel(usm);

	  snmp.getUSM().addUser(new OctetString("MD5DES"),
			  new UsmUser(new OctetString("MD5DES"),
                          AuthMD5.ID,
                          new OctetString("MD5DESUserAuthPassword"),
                          PrivDES.ID,
                          new OctetString("MD5DESUserPrivPassword")));
	    

	  	UserTarget  Utarget = new UserTarget();
	  	Utarget.setVersion(snmpVersion);
	  	Utarget.setAddress(new UdpAddress(ipAddress + "/" + port));
	  	Utarget.setRetries(2);
	  	Utarget.setTimeout(1000);
	  	Utarget.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
	  	Utarget.setSecurityName(new OctetString("MD5DES"));
	    

	    // Cria a PDU
	    PDU pdu = new ScopedPDU();
	    pdu.add(new VariableBinding(new OID(oidValueifSpeed)));
	    pdu.setType(PDU.GETNEXT);	    
	    

	   // System.out.println("Enviando Request...");
	    ResponseEvent response = snmp.get(pdu, Utarget);

	    // Processo Response
	    if (response != null)
	    {
	    //  System.out.println("lendo resposta...");
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
	  System.out.println("SNMPV3 Manager");
	  
	  SNMPManagerV3 Manager = new SNMPManagerV3();
	  Manager.ValueifSpeed();
	  
	  Manager.UtilizacaoRede();
	  System.out.println("Aguardando 30 segundos para segunda coleta");
	  Thread.sleep( 30 * 1000 ); // aguarda 30segundos 
	  
	  Manager.UtilizacaoRede();
  }
    
}