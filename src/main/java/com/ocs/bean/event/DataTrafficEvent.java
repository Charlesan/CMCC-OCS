package com.ocs.bean.event;

/**
 * ����U/T��ʱ��CF��ȡU/T���е������Ϣ��װΪ�������Ȼ����RF��������
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
