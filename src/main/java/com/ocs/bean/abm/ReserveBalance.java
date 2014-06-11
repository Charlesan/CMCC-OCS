package com.ocs.bean.abm;

import java.security.Timestamp;

public class ReserveBalance {
	private String sessionID;
	private long serviceID;
	private long serviceType;
	private long accountID;
	private long accountItemID;
	private double reserveAmount;
	private Timestamp reserveDate;
	private Timestamp expDate;
	private Timestamp updateTime;
	
	@Override
	public String toString() {
		return "ReserveBalance [sessionID=" + sessionID + ", serviceID="
				+ serviceID + ", serviceType=" + serviceType + ", accountID="
				+ accountID + ", accountItemID=" + accountItemID
				+ ", reserveAmount=" + reserveAmount + ", reserveDate="
				+ reserveDate + ", expDate=" + expDate + ", updateTime="
				+ updateTime + "]";
	}
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public long getServiceID() {
		return serviceID;
	}
	public void setServiceID(long serviceID) {
		this.serviceID = serviceID;
	}
	public long getServiceType() {
		return serviceType;
	}
	public void setServiceType(long serviceType) {
		this.serviceType = serviceType;
	}
	public long getAccountID() {
		return accountID;
	}
	public void setAccountID(long accountID) {
		this.accountID = accountID;
	}
	public long getAccountItemID() {
		return accountItemID;
	}
	public void setAccountItemID(long accountItemID) {
		this.accountItemID = accountItemID;
	}
	public double getReserveAmount() {
		return reserveAmount;
	}
	public void setReserveAmount(double reserveAmount) {
		this.reserveAmount = reserveAmount;
	}
	public Timestamp getReserveDate() {
		return reserveDate;
	}
	public void setReserveDate(Timestamp reserveDate) {
		this.reserveDate = reserveDate;
	}
	public Timestamp getExpDate() {
		return expDate;
	}
	public void setExpDate(Timestamp expDate) {
		this.expDate = expDate;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
