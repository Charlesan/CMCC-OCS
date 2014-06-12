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
 * 用于处理请求报文的类
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
		System.out.println(Thread.currentThread().getName() + "处理I包中...");
		
		/** 先拿到用户的ID，即用户手机号 */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//返回错误
		}
		
		/** 访问ABM，获取账户信息 */
		ABMAccessor abmAccessor = new ABMAccessor();
		ARQResult arqResult = null;
		// 调用ARQ
		try {
			arqResult = (ARQResult)abmAccessor.sendARQ("13430321124", MessageUtils.querySessionID(request));
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("<<<<<<<<<<<<<<<<账户信息");
//		System.out.println(arqResult);
		
		DataTrafficEvent event = new DataTrafficEvent();
		event.setPhoneNumber(subscriptionID);
		event.setProduceChannel("All");
		event.setProduceLocation("省内"); //这里应该是广州，然后relativeLocation字段是国内，先不改了
		event.setProduceQuantity(MessageUtils.queryRequestedServiceUnit(request));
		//开始时间和结束时间先不用
		
		Account account = new Account();
		account.setAccountID(subscriptionID);
		account.setNumberAttribution("预付费");
		account.setPhoneNumber(subscriptionID);
		//此处要将从ABM拿回的arqResult封装为一个account对象，目前来说存在问题，所以先不这样做！！先直接生成，为了演示  -2014.6.11
        // Pacage - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G_19";
    	account.mainService.packageName = "动感地带3G网聊卡19元套餐";
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
        			pi.packageName = "流量叠加包10元套餐";
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
    	System.out.println("<<<<<<<<<<<调用RF进行规则匹配");
    	RatingResult ratingResult = ratingFunction.dataTrafficRating(account, event);
    	System.out.println("<<<<<<<<<<<匹配结果：");
    	ratingResult.printObject();
    	
    	//这里应该判断匹配到的规则是否是Base Rule，如果是的话，应该看余额，余额充足就预留余额，不充足就返回错误。 先不理！！
    	if ( true ) { 
    		;
    	}
    	
    	System.out.println("<<<<<<<<<<<用户请求配额：" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("规则" + ratingResult.getRuleID() + "剩余：" + 9940.0 + "KB.");
    	System.out.println("请求配额批准，下发。");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
		
    	//存会话数据
    	System.out.println("<<<<<<<<<<<存储会话数据.");
    	CFSession cfSession = new CFSession();
    	cfSession.setSessionID(MessageUtils.querySessionID(request) + "_" + MessageUtils.queryCCReqeustNumber(request));
    	cfSession.setDataBody(ratingResult);
    	System.out.println(cfSession);
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	if (cfSessionDAO.writeCFSession(cfSession) == 1) 
    		System.out.println("存储会话数据到tiar成功！");
    	else
    		System.out.println("存储会话数据到tiar失败！");
    	
    	
		/** 进行用户的认证和授权 */
		//1.用户不存在，返回错误
		//2.该用户能否使用该业务，不能则返回错误
		//3.余额是否充足，不足则返回错误
	}
	
	private void processUpdateMessage() {
		System.out.println(Thread.currentThread().getName() + "处理U包中...");
		
		//将上一预留数据存在会话数据，然后才此时拿来直接扣费其实是有问题的，因为上次匹配时没有考虑时间区间！！ 先不管了。
		System.out.println("<<<<<<<<<<<<取上一时间片会话数据");
    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
    	String sessionID = MessageUtils.querySessionID(request) + "_" + (MessageUtils.queryCCReqeustNumber(request)-1);
    	CFSession cfSession = cfSessionDAO.getCFSession(sessionID);
    	if (cfSession != null) {
    		System.out.println("从tiar获取成功！");
    		System.out.println(cfSession);
    	}
    	else
    		System.out.println("从tiar获取失败！");
		System.out.println("<<<<<<<<<<<<上一时间片使用：1024KB.");
		
		//调用ABM扣取上一时间片使用的流量
		System.out.println("调用ABM扣取上一时间片使用的流量");
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<扣取流量成功！");
		
		/** 访问ABM，获取扣取后的最新账户信息 */
		System.out.println("访问ABM，获取扣取后的最新账户信息");
//		ABMAccessor abmAccessor = new ABMAccessor();
//		ARQResult arqResult = null;
//		// 调用ARQ
//		try {
//			arqResult = (ARQResult)abmAccessor.sendARQ("13430321124", MessageUtils.querySessionID(request));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		Account account = new Account();
		account.setAccountID(MessageUtils.querySubscriptionID(request));
		account.setNumberAttribution("预付费");
		account.setPhoneNumber(MessageUtils.querySubscriptionID(request));
		//此处要将从ABM拿回的arqResult封装为一个account对象，目前来说存在问题，所以先不这样做！！先直接生成，为了演示  -2014.6.11
        // Pacage - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G_19";
    	account.mainService.packageName = "动感地带3G网聊卡19元套餐";
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
        			pi.packageName = "流量叠加包10元套餐";
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
		event.setProduceLocation("省内"); //这里应该是广州，然后relativeLocation字段是国内，先不改了
		event.setProduceQuantity(MessageUtils.queryRequestedServiceUnit(request));
		//开始时间和结束时间先不用
		
    	RatingFunction ratingFunction = new RatingFunctionImpl();
    	System.out.println("<<<<<<<<<<<调用RF进行规则匹配");
    	RatingResult ratingResult = ratingFunction.dataTrafficRating(account, event);
    	System.out.println("<<<<<<<<<<<匹配结果：");
    	ratingResult.printObject();
    	
    	System.out.println("<<<<<<<<<<<用户请求配额：" + MessageUtils.queryRequestedServiceUnit(request) + "KB.");
    	System.out.println("规则" + ratingResult.getRuleID() + "剩余：" + 8916.0 + "KB.");
    	System.out.println("请求配额批准，下发。");
    	answer.add(new AVP_Grouped(ProtocolConstants.DI_GRANTED_SERVICE_UNIT, 
						new AVP[] {new AVP_Unsigned64(ProtocolConstants.DI_CC_TOTAL_OCTETS, MessageUtils.queryRequestedServiceUnit(request))}));
    	System.out.println("<<<<<<<<<<<<<<<<<<<");
    	
    	//存会话数据
    	System.out.println("<<<<<<<<<<<存储会话数据.");
    	CFSession cfSession2 = new CFSession();
    	cfSession2.setSessionID(MessageUtils.querySessionID(request) + "_" + MessageUtils.queryCCReqeustNumber(request));
    	cfSession2.setDataBody(ratingResult);
    	System.out.println(cfSession2);
    	CFSessionDAO cfSessionDAO2 = new CFSessionDAOTairImpl();
    	if (cfSessionDAO2.writeCFSession(cfSession2) == 1) 
    		System.out.println("存储会话数据到tiar成功！");
    	else
    		System.out.println("存储会话数据到tiar失败！");
		
	}
	
	private void processTerminalMessage() {
		System.out.println(Thread.currentThread().getName() + "处理T包中...");
		
		//将上一预留数据存在会话数据，然后才此时拿来直接扣费其实是有问题的，因为上次匹配时没有考虑时间区间！！ 先不管了。
				System.out.println("<<<<<<<<<<<<取上一时间片会话数据");
		    	CFSessionDAO cfSessionDAO = new CFSessionDAOTairImpl();
		    	String sessionID = MessageUtils.querySessionID(request) + "_" + (MessageUtils.queryCCReqeustNumber(request)-1);
		    	CFSession cfSession = cfSessionDAO.getCFSession(sessionID);
		    	if (cfSession != null) {
		    		System.out.println("从tiar获取成功！");
		    		System.out.println(cfSession);
		    	}
		    	else
		    		System.out.println("从tiar获取失败！");
				System.out.println("<<<<<<<<<<<<上一时间片使用：1024KB.");
				
				//调用ABM扣取上一时间片使用的流量
				System.out.println("调用ABM扣取上一时间片使用的流量");
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<扣取流量成功！");
				
				/** 访问ABM，获取扣取后的最新账户信息 */
				System.out.println("访问ABM，获取扣取后的最新账户信息");
//				ABMAccessor abmAccessor = new ABMAccessor();
//				ARQResult arqResult = null;
//				// 调用ARQ
//				try {
//					arqResult = (ARQResult)abmAccessor.sendARQ("13430321124", MessageUtils.querySessionID(request));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				Account account = new Account();
				account.setAccountID(MessageUtils.querySubscriptionID(request));
				account.setNumberAttribution("预付费");
				account.setPhoneNumber(MessageUtils.querySubscriptionID(request));
				//此处要将从ABM拿回的arqResult封装为一个account对象，目前来说存在问题，所以先不这样做！！先直接生成，为了演示  -2014.6.11
		        // Pacage - mainService
		    	account.mainService = new PackageInfo();
		    	account.mainService.packageID = "DG3G_19";
		    	account.mainService.packageName = "动感地带3G网聊卡19元套餐";
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
		        			pi.packageName = "流量叠加包10元套餐";
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
		    	System.out.println("<<<<<<<<<<<<<<<<<<<当前账户信息");
		    	System.out.println(account);
		    		    	
		    	//删除所有会话数据
		    	System.out.println("<<<<<<<<<<<删除与本次会话相关的所有会话数据.");
		    	List<String> cfSessionIDs = new ArrayList<String>();
		    	for ( int i = 0; i < MessageUtils.queryCCReqeustNumber(request); i++ ) {
		    		cfSessionIDs.add(MessageUtils.querySessionID(request) + "_" + i);
		    	}
		    	CFSessionDAO cfSessionDAO2 = new CFSessionDAOTairImpl();
		    	if (cfSessionDAO2.deleteCFSession(cfSessionIDs) == 1) 
		    		System.out.println("删除所有会话数据成功！");
		    	else
		    		System.out.println("删除所有会话数据失败！");
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
