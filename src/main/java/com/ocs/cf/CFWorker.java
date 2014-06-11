package com.ocs.cf;

import com.ocs.protocol.diameter.AVP;
import com.ocs.protocol.diameter.AVP_Grouped;
import com.ocs.protocol.diameter.AVP_UTF8String;
import com.ocs.protocol.diameter.AVP_Unsigned32;
import com.ocs.protocol.diameter.InvalidAVPLengthException;
import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;
import com.ocs.protocol.diameter.node.ConnectionKey;
import com.ocs.protocol.diameter.node.NodeManager;
import com.ocs.protocol.diameter.node.NotAnAnswerException;
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
		System.out.println(Thread.currentThread().getName() + "Started");
		
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
		
		System.out.println(Thread.currentThread().getName() + "Ended");
	}
	
	private void processInitialMessage() {
		System.out.println(Thread.currentThread().getName() + "����I����...");
		
		/** ���õ��û���ID�����û��ֻ��� */
		String subscriptionID = MessageUtils.querySubscriptionID(request);
		if ( subscriptionID == null ) {
			//���ش���
		}
		
		
		/** �����û�����֤����Ȩ */
		//1.�û������ڣ����ش���
		//2.���û��ܷ�ʹ�ø�ҵ�񣬲����򷵻ش���
		//3.����Ƿ���㣬�����򷵻ش���
	}
	
	private void processUpdateMessage() {
		System.out.println(Thread.currentThread().getName() + "����U����...");
		
	}
	
	private void processTerminalMessage() {
		System.out.println(Thread.currentThread().getName() + "����T����...");
		
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
