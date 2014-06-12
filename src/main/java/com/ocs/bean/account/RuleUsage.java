package com.ocs.bean.account;

public class RuleUsage {
	public String ruleID;
	public String ruleName;
	public double remainQuantity; // unit : k
	
	public String getPrintString(){
		String s = "RuleUsage["+
				"ruleID:"+ruleID+","+
				"ruleName:"+ruleName+","+
				"remainQuantity:"+remainQuantity+","+
				" ]";
		return s;
	}
	
	public void printObject(){
		System.out.println(getPrintString());
	}

	@Override
	public String toString() {
		return "RuleUsage [ruleID=" + ruleID + ", ruleName=" + ruleName
				+ ", remainQuantity=" + remainQuantity + "]";
	}
	
	
}
