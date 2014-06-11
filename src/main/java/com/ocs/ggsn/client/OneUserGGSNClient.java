package com.ocs.ggsn.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_OctetString;
import com.ocs.protocol.diameter.AVP_Time;
import com.ocs.protocol.diameter.AVP_UTF8String;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.AVP_Unsigned64;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.MessageHeader;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.Utils;
import com.ocs.protocol.diameter.node.Capability;
import com.ocs.protocol.diameter.node.InvalidSettingException;
import com.ocs.protocol.diameter.node.NodeSettings;
import com.ocs.protocol.diameter.node.Peer;
import com.ocs.protocol.diameter.node.SimpleSyncClient;
import com.ocs.protocol.diameter.node.UnsupportedTransportProtocolException;
import com.ocs.utils.MessageUtils;

public class OneUserGGSNClient {
	
	@SuppressWarnings("unused")
	public static final void main(String args[]) throws Exception {
		String host_id = "127.0.0.1";
		String realm = "cmcc.com";
		String dest_host = "127.0.0.1";
		int dest_port = 3868;
		
		Capability capability = new Capability();
		capability.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);
		//capability.addAcctApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);
		
		final NodeSettings node_settings;
		try {
			node_settings  = new NodeSettings(
				host_id, realm,
				99999, //vendor-id
				capability,
				0,
				"GGSN", 0x01000000);
		} catch (InvalidSettingException e) {
			System.out.println(e.toString());
			return;
		}
		
		final Peer peers[] = new Peer[]{
			new Peer(dest_host,dest_port)
		};
		
		SimpleSyncClient ssc = new SimpleSyncClient(
				node_settings, peers);
		try {
			ssc.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedTransportProtocolException e) {
			e.printStackTrace();
		}
		try {
			ssc.waitForConnection();// 阻塞直到获得connection
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // allow connection to be established.

		List<Message> messages = generateOneSessionMessages(ssc);
		for (Message message : messages) {
			AVP_UTF8String sessionIdAVP = new AVP_UTF8String(message.find(ProtocolConstants.DI_SESSION_ID));
			String sessionID = sessionIdAVP.queryValue();
			Utils.setMandatory_RFC3588(message);
			Utils.setMandatory_RFC4006(message);

			AVP avp = message
					.find(ProtocolConstants.DI_CC_REQUEST_TYPE);
			if (avp != null) {
				AVP_Unsigned32 result_code_u32 = null;
				try {
					result_code_u32 = new AVP_Unsigned32(avp);
				} catch (InvalidAVPLengthException e) {
					e.printStackTrace();
				}
				int type = result_code_u32.queryValue();
				if (type == ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST)
//					System.out.println("开始发送I包请求。");
				if (type == ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST) {
//					System.out.println("开始发送U包请求。");
					AVP_Unsigned64 temp = (AVP_Unsigned64) message
							.find(ProtocolConstants.DI_CC_TOTAL_OCTETS);
//					System.out.println("申请预留配额：" + "1024"
//							+ "byte(s).");
				}
				if (type == ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST) {
//					System.out.println("开始发送T包请求。");
					AVP_Unsigned64 temp = (AVP_Unsigned64) message
							.find(ProtocolConstants.DI_CC_TOTAL_OCTETS);
//					System.out.println("实际使用配额：" + "956"
//							+ "byte(s).");
				}
			}

			// Send it
			Message CCA = ssc.sendRequest(message);

			processResponse(CCA);

//			System.out.println("*****************");
		}
		// Stop the stack
		ssc.stop();
	}
	
	private static List<Message> generateOneSessionMessages(SimpleSyncClient simpleSyncClient) {
		List<Message> messages = new ArrayList<Message>();
		
		String newSessionID = simpleSyncClient.node().makeNewSessionId();
		System.out.println("生成session ID：" + newSessionID);
		MessageHeader messageHeader = new MessageHeader(); //要不要考虑那个hop-by-hop id的字段？？ Node类有生成这个id
		messageHeader.command_code = ProtocolConstants.DIAMETER_COMMAND_CC;
		messageHeader.application_id = ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL;
		messageHeader.setRequest(true);
		messageHeader.setProxiable(true);
		
		messages.add(generateInitialMassage(simpleSyncClient, newSessionID, messageHeader));
		messages.add(generateUpdateMassage(simpleSyncClient, newSessionID, messageHeader));
		messages.add(generateTerminalMassage(simpleSyncClient, newSessionID, messageHeader));
		
		return messages;
	}
	
	private static Message generateInitialMassage(SimpleSyncClient simpleSyncClient, String sessionID, MessageHeader messageHeader) {
		Message initialMessage = new Message(messageHeader);
		
		//Build Credit-Control Initial Request
		// <Credit-Control-Request> ::= < Diameter Header: 272, REQ, PXY >
		//  < Session-Id >
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, sessionID));
		//  { Origin-Host }
		//  { Origin-Realm }
		simpleSyncClient.node().addOurHostAndRealm(initialMessage);
		//  { Destination-Realm }
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,"cmcc.com"));
		//  { Auth-Application-Id }
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a lie but a minor one
		//  { Service-Context-Id }
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SERVICE_CONTEXT_ID,"gprs@cmcc.com"));
		
		//  { CC-Request-Type } 不同的request类型不同了！
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST));
		//  { CC-Request-Number } 这个字段是要逐一增加的哦~~
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_NUMBER,0));
		
		//  [ Destination-Host ]
		initialMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, "BOSS@cmcc.com"));
		//  [ User-Name ]
		//  [ Origin-State-Id ] //移动所给报文中有这个AVP
		//  [ Event-Timestamp ]
		initialMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,(int)(System.currentTimeMillis()/1000)));
		// *[ Subscription-Id ]
		initialMessage.add(new AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
									new AVP[] {new AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE, ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
											   new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA, "8613450215843")}));
		//    Multiple-Services-Indicator 表示是否支持MSCC
		initialMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR, 1));
		//	  Multiple-Services-Credit-Control
		initialMessage.add(new AVP_Grouped(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL, 
									new AVP[] {new AVP_Grouped(ProtocolConstants.DI_REQUESTED_SERVICE_UNIT, 
														new AVP[]{}  )}));
		// 	  Service-Information
		//	 	  PS-Information
		//	 	  	  3GPP-User-Location-Info
		initialMessage.add(new AVP_Grouped(ProtocolConstants._3GPP_SERVICE_INFORMATION, 
				new AVP[] {new AVP_Grouped(ProtocolConstants._3GPP_PS_INFORMATION,
									//要实现：将位置信息编码为byte[]
									new AVP[]{ new AVP_OctetString(ProtocolConstants._3GPP_USER_LOCATION_INFO, MessageUtils.string2ByteArray("广州"))}  )}));
		
		//  [ Service-Identifier ]
		//  [ Termination-Cause ] 是不是要在T包中给这个AVP?
		
		return initialMessage;
	}
	
	private static Message generateUpdateMassage(SimpleSyncClient simpleSyncClient, String sessionID, MessageHeader messageHeader) {
		Message updateMessage = new Message(messageHeader);
		
		//Build Credit-Control Initial Request
		// <Credit-Control-Request> ::= < Diameter Header: 272, REQ, PXY >
		//  < Session-Id >
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, sessionID));
		//  { Origin-Host }
		//  { Origin-Realm }
		simpleSyncClient.node().addOurHostAndRealm(updateMessage);
		//  { Destination-Realm }
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,"cmcc.com"));
		//  { Auth-Application-Id }
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a lie but a minor one
		//  { Service-Context-Id }
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SERVICE_CONTEXT_ID,"gprs@cmcc.com"));
		
		//  { CC-Request-Type } 不同的request类型不同了！
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST));
		//  { CC-Request-Number } 这个字段是要逐一增加的哦~~
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_NUMBER,0));
		
		//  [ Destination-Host ]
		updateMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, "BOSS@cmcc.com"));
		//  [ User-Name ]
		//  [ Origin-State-Id ] //移动所给报文中有这个AVP
		//  [ Event-Timestamp ]
		updateMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,(int)(System.currentTimeMillis()/1000)));
		// *[ Subscription-Id ]
		updateMessage.add(new AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
									new AVP[] {new AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE, ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
											   new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA, "8613450215843")}));
		//    Multiple-Services-Indicator 表示是否支持MSCC
		updateMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR, 1));
		//	  Multiple-Services-Credit-Control
		updateMessage.add(new AVP_Grouped(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL, 
									new AVP[] {new AVP_Grouped(ProtocolConstants.DI_REQUESTED_SERVICE_UNIT, 
														new AVP[]{ new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, 1024) }  )}));
		// 	  Service-Information
		//	 	  PS-Information
		//	 	  	  3GPP-User-Location-Info
		updateMessage.add(new AVP_Grouped(ProtocolConstants._3GPP_SERVICE_INFORMATION, 
				new AVP[] {new AVP_Grouped(ProtocolConstants._3GPP_PS_INFORMATION,
									//要实现：将位置信息编码为byte[]
									new AVP[]{ new AVP_OctetString(ProtocolConstants._3GPP_USER_LOCATION_INFO, new byte[]{})}  )}));
		
		//  [ Service-Identifier ]
		//  [ Termination-Cause ] 是不是要在T包中给这个AVP?
		
		return updateMessage;
	}
	
	private static Message generateTerminalMassage(SimpleSyncClient simpleSyncClient, String sessionID, MessageHeader messageHeader) {
		Message terminalMessage = new Message(messageHeader);
		
		//Build Credit-Control Initial Request
		// <Credit-Control-Request> ::= < Diameter Header: 272, REQ, PXY >
		//  < Session-Id >
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, sessionID));
		//  { Origin-Host }
		//  { Origin-Realm }
		simpleSyncClient.node().addOurHostAndRealm(terminalMessage);
		//  { Destination-Realm }
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM,"cmcc.com"));
		//  { Auth-Application-Id }
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL)); // a lie but a minor one
		//  { Service-Context-Id }
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_SERVICE_CONTEXT_ID,"gprs@cmcc.com"));
		
		//  { CC-Request-Type } 不同的request类型不同了！
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_TYPE,ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST));
		//  { CC-Request-Number } 这个字段是要逐一增加的哦~~
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_CC_REQUEST_NUMBER,0));
		
		//  [ Destination-Host ]
		terminalMessage.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, "BOSS@cmcc.com"));
		//  [ User-Name ]
		//  [ Origin-State-Id ] //移动所给报文中有这个AVP
		//  [ Event-Timestamp ]
		terminalMessage.add(new AVP_Time(ProtocolConstants.DI_EVENT_TIMESTAMP,(int)(System.currentTimeMillis()/1000)));
		// *[ Subscription-Id ]
		terminalMessage.add(new AVP_Grouped(ProtocolConstants.DI_SUBSCRIPTION_ID,
									new AVP[] {new AVP_Unsigned32(ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE, ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE_END_USER_E164),
											   new AVP_UTF8String(ProtocolConstants.DI_SUBSCRIPTION_ID_DATA, "8613450215843")}));
		//    Multiple-Services-Indicator 表示是否支持MSCC
		terminalMessage.add(new AVP_Unsigned32(ProtocolConstants.DI_MULTIPLE_SERVICES_INDICATOR, 1));
		//	  Multiple-Services-Credit-Control
		terminalMessage.add(new AVP_Grouped(ProtocolConstants.DI_MULTIPLE_SERVICES_CREDIT_CONTROL, 
									new AVP[] {new AVP_Grouped(ProtocolConstants.DI_REQUESTED_SERVICE_UNIT, 
														new AVP[]{  new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, 968) }  )}));
		// 	  Service-Information
		//	 	  PS-Information
		//	 	  	  3GPP-User-Location-Info
		terminalMessage.add(new AVP_Grouped(ProtocolConstants._3GPP_SERVICE_INFORMATION, 
				new AVP[] {new AVP_Grouped(ProtocolConstants._3GPP_PS_INFORMATION,
									//要实现：将位置信息编码为byte[]
									new AVP[]{ new AVP_OctetString(ProtocolConstants._3GPP_USER_LOCATION_INFO, new byte[]{})}  )}));
		
		//  [ Service-Identifier ]
		//  [ Termination-Cause ] 是不是要在T包中给这个AVP?
		
		return terminalMessage;
	}
	
	private static void processResponse(Message answer) {
		// Now look at the result
		if (answer == null) {
			System.out.println("No response");
			return;
		}
		AVP result_code = answer.find(ProtocolConstants.DI_RESULT_CODE);
		if (result_code == null) {
			System.out.println("No result code");
			return;
		}
		try {
			AVP_Unsigned32 result_code_u32 = new AVP_Unsigned32(result_code);
			int rc = result_code_u32.queryValue();
			switch (rc) {
			case ProtocolConstants.DIAMETER_RESULT_SUCCESS:
//				System.out.println("Success");
				AVP avp = answer.find(ProtocolConstants.DI_CC_REQUEST_TYPE);
				if ( avp != null ) {
					AVP_Unsigned32 result_code_u32_2 = new AVP_Unsigned32(avp);
					int type = result_code_u32_2.queryValue();
					if ( type == ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST)
						System.out.println("成功收到I包回复。------会话建立");
					if ( type == ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST)
						System.out.println("成功收到U包回复。------配额预留成功");
					if ( type == ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST)
						System.out.println("成功收到T包回复。------结束会话");
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
		} catch (InvalidAVPLengthException ex) {
			System.out.println("result-code was illformed");
			return;
		}
	}
}
