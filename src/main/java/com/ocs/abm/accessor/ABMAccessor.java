package com.ocs.abm.accessor;

import java.io.IOException;
import java.sql.Timestamp;

import com.ocs.bean.abm.ARQResult;
import com.ocs.bean.abm.Balance;
import com.ocs.bean.abm.Counter;
import com.ocs.bean.abm.Subscriber;
import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Float32;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_Integer32;
import com.ocs.protocol.diameter.AVP_Integer64;
import com.ocs.protocol.diameter.AVP_Time;
import com.ocs.protocol.diameter.AVP_UTF8String;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.AVP_Unsigned64;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.MessageHeader;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.node.Capability;
import com.ocs.protocol.diameter.node.EmptyHostNameException;
import com.ocs.protocol.diameter.node.InvalidSettingException;
import com.ocs.protocol.diameter.node.NodeSettings;
import com.ocs.protocol.diameter.node.Peer;
import com.ocs.protocol.diameter.node.SimpleSyncClient;
import com.ocs.protocol.diameter.node.UnsupportedTransportProtocolException;


public class ABMAccessor{

	public enum OCF_MessageType{
		MsgT_ARQ,
		MsgT_ARS,
		MsgT_AOQ,
		MsgT_AOS,
	}
	
	private static String host_id = "127.0.0.1";
	private static String realm = "cmcc.com";
//	private static String dest_host = "192.168.1.133";
	private static String dest_host = "172.22.192.62";
	private static int dest_port = 3868;
	private static SimpleSyncClient ssc;
	
	public static final void main(String args[]) throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException{
		
		System.out.println("ABM Accessor is running");
		
		// 调用ARQ
		ARQResult rslt = (ARQResult)sendARQ("13430321124","12345677SSS");
		
		System.out.println(rslt.toString());
		for( Balance b : rslt.getBalances() )
			System.out.println(b.toString());
		for( Counter c : rslt.getCounters() )
			System.out.println(c.toString());
		
		// 调用sendAOQ
	}
	
	private static void prepareNode() throws EmptyHostNameException{
		Capability capability = new Capability();
		capability.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);
		//capability.addAcctApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);
		
		NodeSettings node_settings;
		try {
			node_settings  = new NodeSettings(
				host_id, realm,
				99999, //vendor-id
				capability,
				0,
				"OCF", 0x01000000);
		} catch (InvalidSettingException e) {
			System.out.println(e.toString());
			return;
		}
		
		Peer peers[] = new Peer[]{
			new Peer(dest_host,dest_port)
		};
		
		ssc = new SimpleSyncClient(node_settings,peers);
		
	}
	
	public static Object sendMessage(Message msg){
		
		//Send it
		Message answer = ssc.sendRequest(msg);
	
	    return processResponse(answer);
	}
	
	private static Message generateARQMassage( String sessionID, String subscriber_id, MessageHeader messageHeader) {
		
		messageHeader.command_code = ProtocolConstants._3GPP_COMMAND_AR;
		
		Message initialMessage = new Message(messageHeader);
		
		//Build Credit-Control Initial Request
		// <AQR> ::= < Diameter Header: 241, REQ, PXY >
		//  < Session-Id >
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, sessionID));
		//  { Origin-Host }
		//  { Origin-Realm }
		ssc.node().addOurHostAndRealm(initialMessage);
		//  { Destination-Realm }
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,"cmcc.com"));
		//  [ Destination-Host ]
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST,dest_host));
		
		//  { CC-Request-Type } 不同的request类型不同了！
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST));
		//  [ Event-Timestamp ]
		initialMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,(int)(System.currentTimeMillis()/1000)));
		//  { Service-Identifier }
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_SERVICE_IDENTIFIER,0));
		//  [ Actual-Time ]
		initialMessage.add(new AVP_Time(10014 ,(int)System.currentTimeMillis()/1000));
		//  [ Begin-Time ]
		initialMessage.add(new AVP_Time(10030,(int)System.currentTimeMillis()/1000));
		
		// *[ Subscription-Id ]
		initialMessage.add(new AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
											new AVP[] {new AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE, ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
													   new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA, subscriber_id)}));
		
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a lie but a minor one
		return initialMessage;
	}
	
	public static ARQResult sendARQ(String subscriberID, String sessionID ) throws EmptyHostNameException, IOException, UnsupportedTransportProtocolException, InterruptedException{
		prepareNode();
		
		ssc.start();
		ssc.waitForConnection(); //allow connection to be established. 阻塞直到获得connection
		
		MessageHeader messageHeader = new MessageHeader(); //要不要考虑那个hop-by-hop id的字段？？ Node类有生成这个id
		
		messageHeader.application_id = ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL; // ？
		messageHeader.setRequest(true);
		messageHeader.setProxiable(true);
		
		Message msg = generateARQMassage(sessionID, subscriberID, messageHeader);
		
		ARQResult r =(ARQResult) sendMessage(msg);
		
		ssc.stop();
		
		return r;
	}
	
	private static Object processResponse(Message answer) {
		System.out.println("\n*****************");
		System.out.println(">> CF(ABM Accessor) received an message.");
		
		// Now look at the result
		if (answer == null) {
			System.out.println("No response");
			return null;
		}
		AVP result_code = answer.find(ProtocolConstants.DI_RESULT_CODE);
		if (result_code == null) {
			System.out.println("No result code");
			return null;
		}
		
		int cmdCode = answer.hdr.command_code;
		switch(cmdCode){
		case ProtocolConstants._3GPP_COMMAND_AR:
			return processARQResponse(answer);
		default:return null;
		}
	}
	
	private static ARQResult processARQResponse(Message answer) {
		ARQResult arqRslt = new ARQResult();
		
		AVP result_code = answer.find(ProtocolConstants.DI_RESULT_CODE);
		
		try {
			System.out.print("Result:");
			AVP_Unsigned32 result_code_u32 = new AVP_Unsigned32(result_code);
			int rc = result_code_u32.queryValue();
			switch (rc) {
			case ProtocolConstants.DIAMETER_RESULT_SUCCESS:
				System.out.println("Success");
				
				// get subscriber
				AVP savp = answer.find(ProtocolConstants.DI_SUBSCRIPTION_ID);
				if(savp!=null){
					Subscriber s = new Subscriber();
					AVP[] avps = new AVP_Grouped(savp).queryAVPs();
					for( AVP tmpAvp : avps ){
						int code = tmpAvp.code;
						switch (code){
						case ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE: 
							s.setSubscriberIdType(new AVP_Unsigned32(tmpAvp).queryValue());
							break;
						case ProtocolConstants.DI_SUBSCRIPTION_ID_DATA:
							s.setSubscriberIdData(new AVP_UTF8String(tmpAvp).queryValue());
							break;
						default:
							break;
						}
					}
					System.out.println(s.toString());
				}
				
				// get balances
				for ( AVP avp : answer.subset(ProtocolConstants._3GPP_DI_BALANCE)){
					AVP[] avps = new AVP_Grouped(avp).queryAVPs();
					Balance b = new Balance();
					for( AVP tmpAvp : avps ){
						int code = tmpAvp.code;
						switch (code) {
						case ProtocolConstants._3GPP_DI_BALANCE_ID:
							b.setBalanceID(new AVP_Unsigned64(tmpAvp).queryValue());
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_TYPE:
							b.setBalanceType(new AVP_Integer32(tmpAvp).queryValue());
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_EXPIRY_DATE:
							b.setBalanceExpDate(new Timestamp(new AVP_Time(tmpAvp).queryDate().getTime()));
							break;
						case ProtocolConstants._3GPP_DI_BALANCE_VALUE:
							b.setBalanceValue(new AVP_Float32(tmpAvp).queryValue());
							break;
						default:
							break;
						}
					}
					System.out.println(b.toString());
					arqRslt.getBalances().add(b);
				}
				
				// get counters
				for ( AVP avp : answer.subset(ProtocolConstants._3GPP_DI_COUNTER)){
					AVP[] avps = new AVP_Grouped(avp).queryAVPs();
					Counter c = new com.ocs.bean.abm.Counter();
					for( AVP tmpAvp : avps ){
						int code = tmpAvp.code;
						switch (code) {
						case ProtocolConstants._3GPP_DI_COUNTER_ID:
							c.setCounterID(new AVP_Unsigned64(tmpAvp).queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_TYPE:
							c.setCounterType(new AVP_Integer64(tmpAvp).queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_EXPIRY_DATE:
							c.setCounterExpTime(new Timestamp(new AVP_Time(tmpAvp).queryDate().getTime()));
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_VALUE:
							c.setCounterValue(new AVP_Float32(tmpAvp).queryValue());
							break;
						case ProtocolConstants._3GPP_DI_COUNTER_THRESHOLD:
							c.setCounterThreshold(new AVP_Float32(tmpAvp).queryValue());
							break;
						default:
							break;
						}
					}
					System.out.println(c.toString());
					arqRslt.getCounters().add(c);
				}
				break;
			case ProtocolConstants.DIAMETER_RESULT_END_USER_SERVICE_DENIED:
				System.out.println("End user service denied");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_CONTROL_NOT_APPLICABLE:
				System.out.println("Credit-control not applicable");
				break;
			case ProtocolConstants.DIAMETER_RESULT_CREDIT_LIMIT_REACHED:
				System.out.println("Credit-limit reached");
				break;
			case ProtocolConstants.DIAMETER_RESULT_USER_UNKNOWN:
				System.out.println("User unknown");
				break;
			case ProtocolConstants.DIAMETER_RESULT_RATING_FAILED:
				System.out.println("Rating failed");
				break;
			default:
				// Some other error
				// There are too many to decode them all.
				// We just print the classification
				if (rc >= 1000 && rc < 1999)
					System.out.println("Informational: " + rc);
				else if (rc >= 2000 && rc < 2999)
					System.out.println("Success: " + rc);
				else if (rc >= 3000 && rc < 3999)
					System.out.println("Protocl error: " + rc);
				else if (rc >= 4000 && rc < 4999)
					System.out.println("Transient failure: " + rc);
				else if (rc >= 5000 && rc < 5999)
					System.out.println("Permanent failure: " + rc);
				else
					System.out.println("(unknown error class): " + rc);

			}
			
			return arqRslt;
			
		} catch (InvalidAVPLengthException ex) {
			System.out.println("result-code was illformed");
			return null;
		}
	}
	
}
