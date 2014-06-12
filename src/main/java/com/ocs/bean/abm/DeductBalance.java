package com.ocs.bean.abm;

import java.security.PublicKey;

public class DeductBalance {
	private long accountID;
	private long balanceID;
	private long balanceType;
	private long accountItemType;
	private double chgValue;
	private int clearReserveIndicator;
	
	@Override
	public String toString() {
		return "DeductBalance [accountID=" + accountID + ", balanceID="
				+ balanceID + ", balanceType=" + balanceType
				+ ", accountItemType=" + accountItemType + ", chgValue="
				+ chgValue + ", clearReserveIndicator=" + clearReserveIndicator
				+ "]";
	}
	public long getAccountID() {
		return accountID;
	}
	public void setAccountID(long accountID) {
		this.accountID = accountID;
	}
	public long getBalanceID() {
		return balanceID;
	}
	public void setBalanceID(long balanceID) {
		this.balanceID = balanceID;
	}
	public long getBalanceType() {
		return balanceType;
	}
	public void setBalanceType(long balanceType) {
		this.balanceType = balanceType;
	}
	public long getAccountItemType() {
		return accountItemType;
	}
	public void setAccountItemType(long accountItemType) {
		this.accountItemType = accountItemType;
	}
	public double getChgValue() {
		return chgValue;
	}
	public void setChgValue(double chgValue) {
		this.chgValue = chgValue;
	}
	public int getClearReserveIndicator() {
		return clearReserveIndicator;
	}
	public void setClearReserveIndicator(int clearReserveIndicator) {
		this.clearReserveIndicator = clearReserveIndicator;
	}
	

}
