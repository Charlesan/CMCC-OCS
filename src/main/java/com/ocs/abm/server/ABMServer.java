package com.ocs.abm.server;

import java.util.ArrayList;

import com.ocs.bean.abm.Balance;
import com.ocs.bean.abm.Counter;
import com.ocs.bean.abm.DeductBalance;
import com.ocs.bean.abm.DeductCounter;
import com.ocs.bean.abm.ReserveBalance;
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
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.Utils;
import com.ocs.protocol.diameter.node.Capability;
import com.ocs.protocol.diameter.node.ConnectionKey;
import com.ocs.protocol.diameter.node.InvalidSettingException;
import com.ocs.protocol.diameter.node.NodeManager;
import com.ocs.protocol.diameter.node.NodeSettings;
import com.ocs.protocol.diameter.node.NotAnAnswerException;
import com.ocs.protocol.diameter.node.Peer;


public class ABMServer extends NodeManager{
	public ABMServer(NodeSettings nodeSettings) {
		super(nodeSettings);
	}

	public static final void main(String args[]) throws Exception {
//		if (args.length < 2) {
//			System.out.println("Usage: <host-id> <realm> [<port>]");
//			return;
//		}

//		String host_id = "172.22.192.84";
//		String host_id = "192.168.1.133";
		String host_id = "172.22.192.62";
		String realm = "cmcc.com";
		int port = 3868;
//		if (args.length >= 3)
//			port = Integer.parseInt(args[2]);
//		else
//			port = 3868;

		Capability capability = new Capability();
		capability.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);

		NodeSettings node_settings;
		try {
			node_settings = new NodeSettings(host_id, realm, 99999, // vendor-id
					capability, port, "cc_test_abm", 0x01000000);
		} catch (InvalidSettingException e) {
			System.out.println(e.toString());
			return;
		}

		ABMServer tss = new ABMServer(node_settings);
		tss.start();

		System.out.println("Hit enter to terminate server");
		System.in.read();

		tss.stop(50); // Stop but allow 50ms graceful shutdown
	}

	protected void handleRequest(Message request, ConnectionKey connkey, Peer peer) {
		System.out.println("ABM - handleRequest");
		// this is not the way to do it, but fine for a lean-and-mean test
		
		switch (request.hdr.command_code) {
		case ProtocolConstants._3GPP_COMMAND_AR:
			handleARQ(request, connkey, peer);
			break;
		case ProtocolConstants._3GPP_COMMAND_AO:
			try {
				handleAOQ(request, connkey, peer);
			} catch (InvalidAVPLengthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
		

	}

	private void handleARQ(Message request, ConnectionKey connkey, Peer peer) {
		System.out.println("----- get ARQ -----");
		
		// server
		Message answer = new Message();
		answer.prepareResponse(request);
		
		AVP avp;
		
		// < Session-ID >
		avp = request.find(ProtocolConstants.DI_SESSION_ID);
		if (avp != null)
			answer.add(avp);
		
		// { Origin-Host }
		// { Origin-Realm }
		node().addOurHostAndRealm(answer);
		
		// { Subscription-ID }
		avp = request.find(ProtocolConstants.DI_SUBSCRIPTION_ID);
		Subscriber subscribers = new Subscriber();
		if(avp != null){
			AVP[] avps;
			try {
				avps = new AVP_Grouped(avp).queryAVPs();
				for(AVP tmpAVP : avps ){
					int code = tmpAVP.code;
					switch (code) {
					case ProtocolConstants.DI_SUBSCRIPTION_ID_TYPE: 
						try {
							subscribers.setSubscriberIdType(new AVP_Unsigned32(tmpAVP).queryValue());
						} catch (InvalidAVPLengthException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case ProtocolConstants.DI_SUBSCRIPTION_ID_DATA:
						subscribers.setSubscriberIdData(new AVP_UTF8String(tmpAVP).queryValue());
						break;
					default:
						break;
					}
				}
				System.out.println(subscribers.toString());
				
				answer.add(avp);
			} catch (InvalidAVPLengthException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		// { Subscriber-information }
		// 用户信息，暂时未能查询
		
		
		MySQLConnector c = new MySQLConnector();
		ArrayList<Balance> balances = c.getAccountBalance(Long.parseLong(subscribers.getSubscriberIdData()));
		ArrayList<Counter> counters = c.getCounterByObjectID(Long.parseLong(subscribers.getSubscriberIdData()));
		
		// *[ Balance ]
		System.out.println(">>>>> There is "+balances.size()+" Balance item(s)");
		for(Balance b : balances){
			System.out.println(b.toString());
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_BALANCE,
					new AVP[]{
						new AVP_Unsigned64(ProtocolConstants._3GPP_DI_BALANCE_ID, b.getBalanceID()),
						new AVP_Integer32(ProtocolConstants._3GPP_DI_BALANCE_TYPE, b.getBalanceType()),
						new AVP_Time(ProtocolConstants._3GPP_DI_BALANCE_EXPIRY_DATE, b.getBalanceExpDate()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_BALANCE_VALUE, (float)b.getBalanceValue())
			} ));
		}
		
		// *[ Counter ]
		for(Counter cnt : counters){
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_COUNTER,
					new AVP[]{
						new AVP_Unsigned64(ProtocolConstants._3GPP_DI_COUNTER_ID, cnt.getCounterID()),
						new AVP_Integer64(ProtocolConstants._3GPP_DI_COUNTER_TYPE, cnt.getCounterType()),
						new AVP_Time(ProtocolConstants._3GPP_DI_COUNTER_EXPIRY_DATE, cnt.getCounterExpTime()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_VALUE, (float)cnt.getCounterValue()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_THRESHOLD, (float)cnt.getCounterThreshold())
			}));
		}
		
		// { Result-Code }
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
				ProtocolConstants.DIAMETER_RESULT_SUCCESS));

		Utils.setMandatory_RFC3588(answer);

		try {
			answer(answer, connkey);
		} catch (NotAnAnswerException ex) {
		}
	}
	
	private void handleAOQ(Message request, ConnectionKey connkey, Peer peer) throws InvalidAVPLengthException {
		System.out.println("----- get AOQ -----");
		
		MySQLConnector connector = new MySQLConnector();
		
		// server
		Message answer = new Message();
		answer.prepareResponse(request);
		
		AVP avp;
		
		// < Session-ID >
		avp = request.find(ProtocolConstants.DI_SESSION_ID);
		if (avp != null)
			answer.add(avp);
		
		// { Origin-Host }
		// { Origin-Realm }
		node().addOurHostAndRealm(answer);
		
		// get subscriber-id
		avp = request.find(ProtocolConstants._3GPP_DI_SUBSCRIBER_ID);
		long subscriber_id = Long.parseLong(new AVP_UTF8String(avp).queryValue());
		System.out.println("subscriber id : " + subscriber_id);
		
		// get account-id
		avp = request.find(ProtocolConstants._3GPP_DI_ACCOUNT_ID);
		long account_id = Long.parseLong(new AVP_UTF8String(avp).queryValue());
		System.out.println("account id : " + account_id);
		
		// get deduct-operation
		int service_identifier = 0;
		ArrayList<DeductCounter> dcList = new ArrayList<DeductCounter>();
		ArrayList<DeductBalance> dbList = new ArrayList<DeductBalance>();
		avp = request.find(ProtocolConstants._3GPP_DI_MULTIPLE_DEDUCT_OPERATION);
		if(avp != null){
			AVP[] do_avps = new AVP_Grouped(avp).queryAVPs();
			for(AVP tmpAVP : do_avps){
				int code = tmpAVP.code;
				switch (code) {
				case ProtocolConstants.DI_SERVICE_IDENTIFIER: 
					service_identifier = new AVP_Unsigned32(tmpAVP).queryValue();
					break;
				case ProtocolConstants._3GPP_DI_BALANCE:
					parseDeductBalances(account_id,new AVP_Grouped(tmpAVP), dbList);
					
					System.out.println("扣减账本请求--");
					for( DeductBalance db : dbList )
						System.out.println(db.toString());
					System.out.println("扣减账本请求--END");
					break;
				case ProtocolConstants._3GPP_DI_COUNTER:
					DeductCounter dc = parseDeductCounter(new AVP_Grouped(tmpAVP));
					dcList.add(dc);
					break;
				default:
					break;
				}
			}
		}
			
		System.out.println("扣减累积量请求--");
		for( DeductCounter dc : dcList )
			System.out.println(dc.toString());
		System.out.println("扣减累积量请求--END");	
		
		// 进行扣减
		boolean deductBalanceRslt = connector.deductBalance(dbList);
		boolean deductCounterRslt = connector.deductCounter(dcList, account_id);
		boolean deductResult = deductBalanceRslt && deductCounterRslt;
		// * [ Multiple-Deduct-Operation ]
		// 		{ Service-Identifier }
		//      [ Check-Time ]   不知道是什么参数？
		//      { Result-Code }
			
		if(deductResult){
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_MULTIPLE_DEDUCT_OPERATION,
					new AVP[]{
					new AVP_Unsigned32(ProtocolConstants.DI_SERVICE_IDENTIFIER,service_identifier),
					new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,ProtocolConstants.DIAMETER_RESULT_SUCCESS)
			}));
		}
		
		// get reserve-operation
		ArrayList<ReserveBalance> rb = new ArrayList<ReserveBalance>();
		avp = request.find(ProtocolConstants._3GPP_DI_MULTIPLE_RESERVE_OPERATION);
		if(avp != null){
			AVP[] do_avps = new AVP_Grouped(avp).queryAVPs();
			for(AVP tmpAVP : do_avps){
				int code = tmpAVP.code;
				switch (code) {
				case ProtocolConstants.DI_SERVICE_IDENTIFIER: 
					service_identifier = new AVP_Unsigned32(tmpAVP).queryValue();
					break;
				case ProtocolConstants._3GPP_DI_BALANCE:
					parseDeductBalances(account_id,new AVP_Grouped(tmpAVP), dbList);
					break;
				default:
					break;
				}
			}
		}
		
		// 进行预留
		
	
		
		// { Subscriber-information }
		// 用户信息，暂时未能查询
		
		
		// 查询扣减后的余额
		ArrayList<Balance> balances = connector.getAccountBalance(account_id);
		ArrayList<Counter> counters = connector.getCounterByObjectID(account_id);
		
		// *[ Balance ]
		System.out.println(">>>>> There is "+balances.size()+" Balance item(s)");
		for(Balance b : balances){
			System.out.println(b.toString());
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_BALANCE,
					new AVP[]{
						new AVP_Unsigned64(ProtocolConstants._3GPP_DI_BALANCE_ID, b.getBalanceID()),
						new AVP_Integer32(ProtocolConstants._3GPP_DI_BALANCE_TYPE, b.getBalanceType()),
						new AVP_Time(ProtocolConstants._3GPP_DI_BALANCE_EXPIRY_DATE, b.getBalanceExpDate()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_BALANCE_VALUE, (float)b.getBalanceValue())
			} ));
		}
		
		// *[ Counter ]
		for(Counter cnt : counters){
			answer.add(new AVP_Grouped(ProtocolConstants._3GPP_DI_COUNTER,
					new AVP[]{
						new AVP_Unsigned64(ProtocolConstants._3GPP_DI_COUNTER_ID, cnt.getCounterID()),
						new AVP_Integer64(ProtocolConstants._3GPP_DI_COUNTER_TYPE, cnt.getCounterType()),
						new AVP_Time(ProtocolConstants._3GPP_DI_COUNTER_EXPIRY_DATE, cnt.getCounterExpTime()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_VALUE, (float)cnt.getCounterValue()),
						new AVP_Float32(ProtocolConstants._3GPP_DI_COUNTER_THRESHOLD, (float)cnt.getCounterThreshold())
			}));
		}
		
		// { Result-Code }
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
				ProtocolConstants.DIAMETER_RESULT_SUCCESS));

		Utils.setMandatory_RFC3588(answer);

		try {
			answer(answer, connkey);
		} catch (NotAnAnswerException ex) {
		}
	}
	
	
	void answerError(Message answer, ConnectionKey connkey, int result_code, AVP[] error_avp) {
		answer.hdr.setError(true);
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,
				result_code));
		for (AVP avp : error_avp)
			answer.add(avp);
		try {
			answer(answer, connkey);
		} catch (NotAnAnswerException ex) {
		}
	}

	private DeductCounter parseDeductCounter(AVP_Grouped avp) throws InvalidAVPLengthException {
		DeductCounter dcounter = new DeductCounter();
		
		AVP[] avps = avp.queryAVPs();
		for(AVP tmpAVP : avps ){
			int code = tmpAVP.code;
			switch (code) {
			case ProtocolConstants._3GPP_DI_COUNTER_ID: 
				dcounter.setCounterID(new AVP_Unsigned64(tmpAVP).queryValue());
				break;
			case ProtocolConstants._3GPP_DI_CHANGE_VALUE:
				dcounter.setChgValue(new AVP_Float32(tmpAVP).queryValue());
				break;
			default:
				break;
			}
		}
		
		return dcounter;
	}

	
	private void parseDeductBalances(long account_id,AVP_Grouped avp,ArrayList<DeductBalance> list) throws InvalidAVPLengthException{
	
		long balance_id = 0;
		long balance_type = 0;
		boolean clear_indicator = false;
		
		for(AVP bavp : avp.queryAVPs()){
			int bcode = bavp.code;
			switch (bcode){
			case ProtocolConstants._3GPP_DI_BALANCE_ID:
				balance_id = new AVP_Unsigned64(bavp).queryValue();
				break;
			case ProtocolConstants._3GPP_DI_BALANCE_TYPE:
				balance_type = new AVP_Integer32(bavp).queryValue();
				break;
			case ProtocolConstants._3GPP_DI_CLEAR_RESERVE_INDICATOR:
				int c = (int) new AVP_Unsigned64(bavp).queryValue();
				if(c==1)
					clear_indicator = true;
				break;
			default:
				break;
			}
		}
		
		for(AVP bavp : avp.queryAVPs() ){
			if(bavp.code == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM){
				DeductBalance db = new DeductBalance();
				db.setAccountID(account_id);
				db.setBalanceID(balance_id);
				db.setBalanceType(balance_type);
				db.setClearReserveIndicator(clear_indicator);
				
				for( AVP aiavp : new AVP_Grouped(bavp).queryAVPs()){
					int aiCode = aiavp.code;
					if(aiCode == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM)
						db.setAccountItemType(new AVP_Unsigned64(aiavp).queryValue());
					if(aiCode == ProtocolConstants._3GPP_DI_CHANGE_VALUE)
						db.setChgValue(new AVP_Float32(aiavp).queryValue());
				}
				
				list.add(db);
			}
		}
	}
	
	private void parserReserveBalances(String session_id,long service_id,long account_id,AVP_Grouped avp,ArrayList<ReserveBalance> list) throws InvalidAVPLengthException{
		
		long balance_id = 0;
		long balance_type = 0;
		
		for(AVP bavp : avp.queryAVPs()){
			int bcode = bavp.code;
			switch (bcode){
			case ProtocolConstants._3GPP_DI_BALANCE_ID:
				balance_id = new AVP_Unsigned64(bavp).queryValue();
				break;
			case ProtocolConstants._3GPP_DI_BALANCE_TYPE:
				balance_type = new AVP_Integer32(bavp).queryValue();
				break;
			default:
				break;
			}
		}
		
		for(AVP bavp : avp.queryAVPs() ){
			if(bavp.code == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM){
				ReserveBalance rb= new ReserveBalance();
				rb.setAccountID(account_id);
				rb.setSessionID(session_id);
				rb.setServiceID(service_id);
//				rb.reserveDate = new Timestamp(System.currentTimeMillis());
//				rb.expDate //填什么？
				rb.setServiceType(0);
				
				for( AVP aiavp : new AVP_Grouped(bavp).queryAVPs()){
					int aiCode = aiavp.code;
					if(aiCode == ProtocolConstants._3GPP_DI_ACCOUNT_ITEM)
						rb.setAccountItemID(new AVP_Unsigned64(aiavp).queryValue());
					if(aiCode == ProtocolConstants._3GPP_DI_CHANGE_VALUE)
						rb.setReserveAmount(new AVP_Float32(aiavp).queryValue());
				}
				
				list.add(rb);
			}
		}
		
	}
	
}

