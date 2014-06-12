package com.ocs.bean.abm;

import java.util.ArrayList;

public class AOQParams {
	private String sessionID;
	private String subscriberID;
	private String accountID;
	private ArrayList<DeductBalance> deductBalances;
	private ArrayList<DeductCounter> deductCounters;
	private ArrayList<ReserveBalance> reserveBalances;
	
	public AOQParams(){
		this.sessionID = "";
		this.subscriberID = "";
		this.accountID = "";
		this.deductBalances = new ArrayList<DeductBalance>();
		this.deductCounters = new ArrayList<DeductCounter>();
		this.reserveBalances = new ArrayList<ReserveBalance>();
	}
	
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public String getSubscriberID() {
		return subscriberID;
	}
	public void setSubscriberID(String subscriberID) {
		this.subscriberID = subscriberID;
	}
	public String getAccountID() {
		return accountID;
	}
	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}
	public ArrayList<DeductBalance> getDeductBalances() {
		return deductBalances;
	}
	public void setDeductBalances(ArrayList<DeductBalance> deductBalances) {
		this.deductBalances = deductBalances;
	}
	public ArrayList<DeductCounter> getDeductCounters() {
		return deductCounters;
	}
	public void setDeductCounters(ArrayList<DeductCounter> deductCounters) {
		this.deductCounters = deductCounters;
	}
	public ArrayList<ReserveBalance> getReserveBalances() {
		return reserveBalances;
	}
	public void setReserveBalances(ArrayList<ReserveBalance> reserveBalances) {
		this.reserveBalances = reserveBalances;
	}
	@Override
	public String toString() {
		return "AOQParams [sessionID=" + sessionID + ", subscriberID="
				+ subscriberID + ", accountID=" + accountID
				+ ", deductBalances=" + deductBalances + ", deductCounters="
				+ deductCounters + ", reserveBalances=" + reserveBalances + "]";
	}
	
	
}
