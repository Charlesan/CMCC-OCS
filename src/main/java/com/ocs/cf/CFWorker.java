package com.ocs.cf;

import com.ocs.protocol.diameter.Message;
import com.ocs.protocol.diameter.ProtocolConstants;

/**
 * 用于处理请求报文的类
 * @author Wang Chao
 */
public class CFWorker implements Runnable{
	
	private Message message;
	private int messageType;
	
	public CFWorker(Message message, int messageType) {
		this.message = message;
		this.messageType = messageType;
	}
	
	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName() + "Started");
		
		switch (this.messageType) {
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
		System.out.println("处理I包中...");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processUpdateMessage() {
		System.out.println("处理U包中...");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void processTerminalMessage() {
		System.out.println("处理T包中...");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	
	
}
