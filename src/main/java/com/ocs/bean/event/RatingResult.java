package com.ocs.bean.event;

public class RatingResult {
	public double totalPrice;
	public double quantity;
	public String ruleID;
	public String ruleName;
	
	public void printObject(){
		System.out.println(
				"RatingResult["+
				"totalPrice:"+totalPrice+","+
				"quantity:"+quantity+","+
				"ruleID:"+ruleID+","+
				"ruleName:"+ruleName+","+
				" ]"
				);
	}
	
}
