package com.ocs.bean.event;

/**
 * 用于U/T包时，CF提取U/T包中的相关信息封装为该类对象，然后传由RF进行批价
 * @author Wang Chao
 *
 */
public class DataTrafficEvent {
	public String phoneNumber;
	public String produceLocation;
	public String produceChannel;
	public double produceQuantity;
	public String produceTime_start;
	public String produceTime_end;
	
	public void printObject(){
		System.out.println(
				"DataTrafficEvent["+
				"phoneNumber:"+phoneNumber+","+
				"produceLocation:"+produceLocation+","+
				"produceChannel:"+produceChannel+","+
				"produceQuantity:"+produceQuantity+","+
				"produceTime_start:"+produceTime_start+","+
				"produceTime_end:"+produceTime_end+","+
				" ]"
				);
	}
}
