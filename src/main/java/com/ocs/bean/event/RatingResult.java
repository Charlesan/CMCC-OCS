package com.ocs.bean.event;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RatingResult implements Serializable {
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

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	@Override
	public String toString() {
		return "RatingResult [totalPrice=" + totalPrice + ", quantity="
				+ quantity + ", ruleID=" + ruleID + ", ruleName=" + ruleName
				+ "]";
	}
	
}
