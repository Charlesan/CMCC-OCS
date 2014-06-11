package com.ocs.bean.abm;

public class Subscriber {
	private int subscriberIdType;
	private String subscriberIdData;
	
	public Subscriber(){
		subscriberIdData = "";
		subscriberIdType = 0;
	}
	
	public Subscriber(int type,String data){
		subscriberIdData = data;
		subscriberIdType = type;
	}

	public int getSubscriberIdType() {
		return subscriberIdType;
	}

	public void setSubscriberIdType(int subscriberIdType) {
		this.subscriberIdType = subscriberIdType;
	}

	public String getSubscriberIdData() {
		return subscriberIdData;
	}

	public void setSubscriberIdData(String subscriberIdData) {
		this.subscriberIdData = subscriberIdData;
	}

	@Override
	public String toString() {
		return "Subscriber [subscriberIdType=" + subscriberIdType
				+ ", subscriberIdData=" + subscriberIdData + "]";
	}
	
	
	
//	public void printObject(){
//		System.out.println("-- Subscriber Object: --");
//		System.out.println("idType£º" + subscriberIdType);
//		System.out.println("id£º" + subscriberIdData);
//		System.out.println("-- *************** --");
//	}
}
