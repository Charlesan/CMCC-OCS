package com.ocs.cf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ocs.abm.accessor.ABMAccessor;
import com.ocs.bean.abm.ARQResult;
import com.ocs.bean.account.Account;
import com.ocs.bean.account.PackageInfo;
import com.ocs.bean.account.RuleUsage;
import com.ocs.bean.event.DataTrafficEvent;
import com.ocs.bean.event.RatingResult;
import com.ocs.bean.session.CFSession;
import com.ocs.dao.CFSessionDAO;
import com.ocs.dao.impl.CFSessionDAOTairImpl;
import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_UTF8String;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.AVP_Unsigned64;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.node.ConnectionKey;
import com.ocs.protocol.diameter.node.EmptyHostNameException;
import com.ocs.protocol.diameter.node.NodeManager;
import com.ocs.protocol.diameter.node.NotAnAnswerException;
import com.ocs.protocol.diameter.node.UnsupportedTransportProtocolException;
import com.ocs.rf.RatingFunction;
import com.ocs.rf.RatingFunctionImpl;
import com.ocs.utils.MessageUtils;

/**
 * ���ڴ��������ĵ���
 * @author Wang Chao
 */
public class CFWorker implements Runnable {
	
	private NodeManager nodeManager;
	private ConnectionKey connKey;
	private Message request;
	private int requestType;
	private Message answer;
	
	public CFWorker(NodeManager nodeManager, ConnectionKey connKey,
			Message request, int requestType, Message answer) {
		super();
		this.nodeManager = nodeManager;
		this.connKey = connKey;
		this.request = request;
		this.requestType = requestType;
		this.answer = answer;
	}

	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName() + "Started=================================");
		
		switch (this.requestType) {
		case ProtocolConstants.DI_CC_REQUEST_TYPE_INITIAL_REQUEST:
			processInitialMessage();
			break;
		case ProtocolConstants.DI_CC_REQUEST_TYPE_UPDATE_REQUEST:
			processUpdateMessage();
			break;
		case ProtocolConstants.DI_CC_REQUEST_TYPE_TERMINATION_REQUEST:
			processTerminalMessage();
			break;
		default:
			break;
		}
		
		System.out.println(Thread.currentThread().getName() + "Ended====================================\n");
	}
	
	private void processInitialMessage() {
		System.out.println(Thread.currentThread().getName() + "����I����...");
		
		/** ���õ��û���ID�����û��ֻ��� */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//���ش���
		}
		
		/** ����ABM����ȡ�˻���Ϣ */
		ABMAccessor abmAccessor = new ABMAccessor();
		ARQResult arqResult = null;
		// ����ARQ
		try {
			arqResult = (ARQResult)abmAccessor.sendARQ("13430321124", MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("<<<<<<<<<<<<<<<<�˻���Ϣ");
//		System.out.println(arqResult);
		
		DataTrafficEvent event = new DataTrafficEvent();
		event.setPhoneNumber(subscriptionID);
		event.setProduceChannel("All");
		event.setProduceLocation("ʡ��"); //����Ӧ���ǹ��ݣ�Ȼ��relativeLocation�ֶ��ǹ��ڣ��Ȳ�����
		event.setProduceQuantity(MessageUtils.queryRequestedServiceUnit(request));
		//��ʼʱ��ͽ���ʱ���Ȳ���
		
		Account account = new Account();
		account.setAccountID(subscriptionID);
		account.setNumberAttribution("Ԥ����");
		account.setPhoneNumber(subscriptionID);
		//�˴�Ҫ����ABM�ûص�arqResult��װΪһ��account����Ŀǰ��˵�������⣬�����Ȳ�������������ֱ�����ɣ�Ϊ����ʾ  -2014.6.11
        // Pacage - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G_19";
    	account.mainService.packageName = "���еش�3G���Ŀ�19Ԫ�ײ�";
    	account.mainService.usages = new ArrayList<RuleUsage>();
    	for(int i = 1; i <= 4; i++){
    		RuleUsage rule = new RuleUsage();
    		
    		switch(i){
    		case 1:
    			rule.ruleID = "Rule_DG3G19_1";
    			rule.ruleName = "Rule_DG3G19_M_Z";
    			//rule.remainQuantity = 1000;
    			rule.remainQuantity = 102300;
    			break;
    		case 2:
    			rule.ruleID = "Rule_DG3G19_2";
    			rule.ruleName = "Rule_DG3G19_Y_SN";
//    			rule.remainQuantity = 500.5;
    			rule.remainQuantity = 9940;
    			break;
    		case 3:
    			rule.ruleID = "Rule_DG3G19_3";
    			rule.ruleName = "Rule_DG3G19_Y_SNX";
//    			rule.remainQuantity = 1500.5;
    			rule.remainQuantity = 9940;
    			break;
    		case 4:
    			rule.ruleID = "Rule_DG3G19_4";
    			rule.ruleName = "Rule_DG3G19_BASE";
    			rule.remainQuantity = -1;
    			break;
    		default:break;
    		}
    		account.mainService.usages.add(rule);
    	}
    	
    	account.additionalServices = new ArrayList<PackageInfo>();
    	for(int i = 1; i <= 1; i++){
    			PackageInfo pi = new PackageInfo();
    			
    			switch(i){
        		case 1:
        			pi.packageID = "DJ10";
        			pi.packageName = "�������Ӱ�10Ԫ�ײ�";
        			pi.usages = new ArrayList<RuleUsage>();
        			for(int j = 1; j <= 2; j++){
        				RuleUsage rule = new RuleUsage();
                		
                		switch(i){
                		case 1:
                			rule.ruleID = "DJ10_1";
                			rule.ruleName = "Rule_DJ10_GN";
//                			rule.remainQuantity = 100;
                			rule.remainQuantity = 9940;
                			break;
                		case 2:
                			rule.ruleID = "DJ10_2";
                			rule.ruleName = "Rule_DJ10_SN";
//                			rule.remainQuantity = 50.5;
                			rule.remainQuantity = 10;
                			break;
                		default:break;
                		}
                		pi.usages.add(rule);
        			}
        			break;
        		default:break;
        		}
    			account.additionalServices.add(pi);
    	}
    	
    	RatingFunction ratingFunction = new RatingFunctionImpl();
    	System.out.println("<<<<<<<<<<<����RF���й���ƥ��");
    	RatingResult ratingResult = ratingFunction.dataTrafficRating(account, event);
    	System.out.println("<<<<<<<<<<<ƥ������");
    	ratingResult.printObject();
    	
    	//����Ӧ���ж�ƥ�䵽�Ĺ����Ƿ���Base Rule������ǵĻ���Ӧ�ÿ����������Ԥ����������ͷ��ش��� �Ȳ�����
    	if ( true ) { 
    		;
    	}
    	
    	System.out.println("<<<<<<<<<<<�û�������" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("����" + ratingResult.getRuleID() + "ʣ�ࣺ" + 9940.0 + "KB.");
    	System.out.println("���������׼���·���");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
		
    	//��Ự����
    	System.out.println("<<<<<<<<<<<�洢�Ự����.");
    	CFSession cfSession = new CFSession();
    	cfSession.setSessionID(MessageUtils.querySessionID(request) + "_" + MessageUtils.queryCCReqeustNumber(request));
    	cfSession.setDataBody(ratingResult);
    	System.out.println(cfSession);
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	if (cfSessionDAO.writeCFSession(cfSession) == 1) 
    		System.out.println("�洢�Ự���ݵ�tiar�ɹ���");
    	else
    		System.out.println("�洢�Ự���ݵ�tiarʧ�ܣ�");
    	
    	
		/** �����û�����֤����Ȩ */
		//1.�û������ڣ����ش���
		//2.���û��ܷ�ʹ�ø�ҵ�񣬲����򷵻ش���
		//3.����Ƿ���㣬�����򷵻ش���
	}
	
	private void processUpdateMessage() {
		System.out.println(Thread.currentThread().getName() + "����U����...");
		
		//����һԤ�����ݴ��ڻỰ���ݣ�Ȼ��Ŵ�ʱ����ֱ�ӿ۷���ʵ��������ģ���Ϊ�ϴ�ƥ��ʱû�п���ʱ�����䣡�� �Ȳ����ˡ�
		System.out.println("<<<<<<<<<<<<ȡ��һʱ��Ƭ�Ự����");
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	String sessionID = MessageUtils.querySessionID(request) + "_" + (MessageUtils.queryCCReqeustNumber(request)-1);
    	CFSession cfSession = cfSessionDAO.getCFSession(sessionID);
    	if (cfSession != null) {
    		System.out.println("��tiar��ȡ�ɹ���");
    		System.out.println(cfSession);
    	}
    	else
    		System.out.println("��tiar��ȡʧ�ܣ�");
		System.out.println("<<<<<<<<<<<<��һʱ��Ƭʹ�ã�1024KB.");
		
		//����ABM��ȡ��һʱ��Ƭʹ�õ�����
		System.out.println("����ABM��ȡ��һʱ��Ƭʹ�õ�����");
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<��ȡ�����ɹ���");
		
		/** ����ABM����ȡ��ȡ��������˻���Ϣ */
		System.out.println("����ABM����ȡ��ȡ��������˻���Ϣ");
//		ABMAccessor abmAccessor = new ABMAccessor();
//		ARQResult arqResult = null;
//		// ����ARQ
//		try {
//			arqResult = (ARQResult)abmAccessor.sendARQ("13430321124", MessageUtils.querySessionID(request));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		Account account = new Account();
		account.setAccountID(MessageUtils.querySubscriptionID(request));
		account.setNumberAttribution("Ԥ����");
		account.setPhoneNumber(MessageUtils.querySubscriptionID(request));
		//�˴�Ҫ����ABM�ûص�arqResult��װΪһ��account����Ŀǰ��˵�������⣬�����Ȳ�������������ֱ�����ɣ�Ϊ����ʾ  -2014.6.11
        // Pacage - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G_19";
    	account.mainService.packageName = "���еش�3G���Ŀ�19Ԫ�ײ�";
    	account.mainService.usages = new ArrayList<RuleUsage>();
    	for(int i = 1; i <= 4; i++){
    		RuleUsage rule = new RuleUsage();
    		
    		switch(i){
    		case 1:
    			rule.ruleID = "Rule_DG3G19_1";
    			rule.ruleName = "Rule_DG3G19_M_Z";
    			//rule.remainQuantity = 1000;
    			rule.remainQuantity = 102300;
    			break;
    		case 2:
    			rule.ruleID = "Rule_DG3G19_2";
    			rule.ruleName = "Rule_DG3G19_Y_SN";
//    			rule.remainQuantity = 500.5;
    			rule.remainQuantity = 8916;
    			break;
    		case 3:
    			rule.ruleID = "Rule_DG3G19_3";
    			rule.ruleName = "Rule_DG3G19_Y_SNX";
//    			rule.remainQuantity = 1500.5;
    			rule.remainQuantity = 9940;
    			break;
    		case 4:
    			rule.ruleID = "Rule_DG3G19_4";
    			rule.ruleName = "Rule_DG3G19_BASE";
    			rule.remainQuantity = -1;
    			break;
    		default:break;
    		}
    		account.mainService.usages.add(rule);
    	}
    	
    	account.additionalServices = new ArrayList<PackageInfo>();
    	for(int i = 1; i <= 1; i++){
    			PackageInfo pi = new PackageInfo();
    			
    			switch(i){
        		case 1:
        			pi.packageID = "DJ10";
        			pi.packageName = "�������Ӱ�10Ԫ�ײ�";
        			pi.usages = new ArrayList<RuleUsage>();
        			for(int j = 1; j <= 2; j++){
        				RuleUsage rule = new RuleUsage();
                		
                		switch(i){
                		case 1:
                			rule.ruleID = "DJ10_1";
                			rule.ruleName = "Rule_DJ10_GN";
//                			rule.remainQuantity = 100;
                			rule.remainQuantity = 9940;
                			break;
                		case 2:
                			rule.ruleID = "DJ10_2";
                			rule.ruleName = "Rule_DJ10_SN";
//                			rule.remainQuantity = 50.5;
                			rule.remainQuantity = 10;
                			break;
                		default:break;
                		}
                		pi.usages.add(rule);
        			}
        			break;
        		default:break;
        		}
    			account.additionalServices.add(pi);
    	}
    	System.out.println(account);
    	
		DataTrafficEvent event = new DataTrafficEvent();
		event.setPhoneNumber(MessageUtils.querySubscriptionID(request));
		event.setProduceChannel("All");
		event.setProduceLocation("ʡ��"); //����Ӧ���ǹ��ݣ�Ȼ��relativeLocation�ֶ��ǹ��ڣ��Ȳ�����
		event.setProduceQuantity(MessageUtils.queryRequestedServiceUnit(request));
		//��ʼʱ��ͽ���ʱ���Ȳ���
		
    	RatingFunction ratingFunction = new RatingFunctionImpl();
    	System.out.println("<<<<<<<<<<<����RF���й���ƥ��");
    	RatingResult ratingResult = ratingFunction.dataTrafficRating(account, event);
    	System.out.println("<<<<<<<<<<<ƥ������");
    	ratingResult.printObject();
    	
    	System.out.println("<<<<<<<<<<<�û�������" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("����" + ratingResult.getRuleID() + "ʣ�ࣺ" + 8916.0 + "KB.");
    	System.out.println("���������׼���·���");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
    	
    	//��Ự����
    	System.out.println("<<<<<<<<<<<�洢�Ự����.");
    	CFSession cfSession2 = new CFSession();
    	cfSession2.setSessionID(MessageUtils.querySessionID(request) + "_" + MessageUtils.queryCCReqeustNumber(request));
    	cfSession2.setDataBody(ratingResult);
    	System.out.println(cfSession2);
    	CFSessionDAO cfSessionDAO2 = new CFSessionDAOTairImpl();
    	if (cfSessionDAO2.writeCFSession(cfSession2) == 1) 
    		System.out.println("�洢�Ự���ݵ�tiar�ɹ���");
    	else
    		System.out.println("�洢�Ự���ݵ�tiarʧ�ܣ�");
		
	}
	
	private void processTerminalMessage() {
		System.out.println(Thread.currentThread().getName() + "����T����...");
		
		//����һԤ�����ݴ��ڻỰ���ݣ�Ȼ��Ŵ�ʱ����ֱ�ӿ۷���ʵ��������ģ���Ϊ�ϴ�ƥ��ʱû�п���ʱ�����䣡�� �Ȳ����ˡ�
				System.out.println("<<<<<<<<<<<<ȡ��һʱ��Ƭ�Ự����");
		    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
		    	String sessionID = MessageUtils.querySessionID(request) + "_" + (MessageUtils.queryCCReqeustNumber(request)-1);
		    	CFSession cfSession = cfSessionDAO.getCFSession(sessionID);
		    	if (cfSession != null) {
		    		System.out.println("��tiar��ȡ�ɹ���");
		    		System.out.println(cfSession);
		    	}
		    	else
		    		System.out.println("��tiar��ȡʧ�ܣ�");
				System.out.println("<<<<<<<<<<<<��һʱ��Ƭʹ�ã�1024KB.");
				
				//����ABM��ȡ��һʱ��Ƭʹ�õ�����
				System.out.println("����ABM��ȡ��һʱ��Ƭʹ�õ�����");
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<��ȡ�����ɹ���");
				
				/** ����ABM����ȡ��ȡ��������˻���Ϣ */
				System.out.println("����ABM����ȡ��ȡ��������˻���Ϣ");
//				ABMAccessor abmAccessor = new ABMAccessor();
//				ARQResult arqResult = null;
//				// ����ARQ
//				try {
//					arqResult = (ARQResult)abmAccessor.sendARQ("13430321124", MessageUtils.querySessionID(request));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				Account account = new Account();
				account.setAccountID(MessageUtils.querySubscriptionID(request));
				account.setNumberAttribution("Ԥ����");
				account.setPhoneNumber(MessageUtils.querySubscriptionID(request));
				//�˴�Ҫ����ABM�ûص�arqResult��װΪһ��account����Ŀǰ��˵�������⣬�����Ȳ�������������ֱ�����ɣ�Ϊ����ʾ  -2014.6.11
		        // Pacage - mainService
		    	account.mainService = new PackageInfo();
		    	account.mainService.packageID = "DG3G_19";
		    	account.mainService.packageName = "���еش�3G���Ŀ�19Ԫ�ײ�";
		    	account.mainService.usages = new ArrayList<RuleUsage>();
		    	for(int i = 1; i <= 4; i++){
		    		RuleUsage rule = new RuleUsage();
		    		
		    		switch(i){
		    		case 1:
		    			rule.ruleID = "Rule_DG3G19_1";
		    			rule.ruleName = "Rule_DG3G19_M_Z";
		    			//rule.remainQuantity = 1000;
		    			rule.remainQuantity = 102300;
		    			break;
		    		case 2:
		    			rule.ruleID = "Rule_DG3G19_2";
		    			rule.ruleName = "Rule_DG3G19_Y_SN";
//		    			rule.remainQuantity = 500.5;
		    			rule.remainQuantity = 7882;
		    			break;
		    		case 3:
		    			rule.ruleID = "Rule_DG3G19_3";
		    			rule.ruleName = "Rule_DG3G19_Y_SNX";
//		    			rule.remainQuantity = 1500.5;
		    			rule.remainQuantity = 9940;
		    			break;
		    		case 4:
		    			rule.ruleID = "Rule_DG3G19_4";
		    			rule.ruleName = "Rule_DG3G19_BASE";
		    			rule.remainQuantity = -1;
		    			break;
		    		default:break;
		    		}
		    		account.mainService.usages.add(rule);
		    	}
		    	
		    	account.additionalServices = new ArrayList<PackageInfo>();
		    	for(int i = 1; i <= 1; i++){
		    			PackageInfo pi = new PackageInfo();
		    			
		    			switch(i){
		        		case 1:
		        			pi.packageID = "DJ10";
		        			pi.packageName = "�������Ӱ�10Ԫ�ײ�";
		        			pi.usages = new ArrayList<RuleUsage>();
		        			for(int j = 1; j <= 2; j++){
		        				RuleUsage rule = new RuleUsage();
		                		
		                		switch(i){
		                		case 1:
		                			rule.ruleID = "DJ10_1";
		                			rule.ruleName = "Rule_DJ10_GN";
//		                			rule.remainQuantity = 100;
		                			rule.remainQuantity = 9940;
		                			break;
		                		case 2:
		                			rule.ruleID = "DJ10_2";
		                			rule.ruleName = "Rule_DJ10_SN";
//		                			rule.remainQuantity = 50.5;
		                			rule.remainQuantity = 10;
		                			break;
		                		default:break;
		                		}
		                		pi.usages.add(rule);
		        			}
		        			break;
		        		default:break;
		        		}
		    			account.additionalServices.add(pi);
		    	}
		    	System.out.println("<<<<<<<<<<<<<<<<<<<��ǰ�˻���Ϣ");
		    	System.out.println(account);
		    		    	
		    	//ɾ�����лỰ����
		    	System.out.println("<<<<<<<<<<<ɾ���뱾�λỰ��ص����лỰ����.");
		    	List<String> cfSessionIDs = new ArrayList<String>();
		    	for ( int i = 0; i < MessageUtils.queryCCReqeustNumber(request); i++ ) {
		    		cfSessionIDs.add(MessageUtils.querySessionID(request) + "_" + i);
		    	}
		    	CFSessionDAO cfSessionDAO2 = new CFSessionDAOTairImpl();
		    	if (cfSessionDAO2.deleteCFSession(cfSessionIDs) == 1) 
		    		System.out.println("ɾ�����лỰ���ݳɹ���");
		    	else
		    		System.out.println("ɾ�����лỰ����ʧ�ܣ�");
	}
	

	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public void setNodeManager(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	public ConnectionKey getConnKey() {
		return connKey;
	}

	public void setConnKey(ConnectionKey connKey) {
		this.connKey = connKey;
	}

	public Message getRequest() {
		return request;
	}

	public void setRequest(Message request) {
		this.request = request;
	}

	public int getRequestType() {
		return requestType;
	}

	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	public Message getAnswer() {
		return answer;
	}

	public void setAnswer(Message answer) {
		this.answer = answer;
	}
	
}
