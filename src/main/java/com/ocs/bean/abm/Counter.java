package com.ocs.bean.abm;

import java.sql.Timestamp;


public class Counter {
	private long counterID;
	private long counterType;
	private Timestamp counterExpTime;
	private double counterValue;
	private double counterThreshold;

	public Counter(){
		this.counterID = 0;
		this.counterType = 0;
		this.counterExpTime = null;
		this.counterValue = 0;
		this.counterThreshold = 0;
	}
	
	public Counter(long counterID, long counterType, Timestamp counterExpTime,
			double counterValue, double counterThreshold) {
		this.counterID = counterID;
		this.counterType = counterType;
		this.counterExpTime = counterExpTime;
		this.counterValue = counterValue;
		this.counterThreshold = counterThreshold;
	}

	@Override
	public String toString() {
		return "Counter [counterID=" + counterID + ", counterType="
				+ counterType + ", counterExpTime=" + counterExpTime
				+ ", counterValue=" + counterValue + ", counterThreshold="
				+ counterThreshold + "]";
	}

	public long getCounterID() {
		return counterID;
	}

	public void setCounterID(long counterID) {
		this.counterID = counterID;
	}

	public long getCounterType() {
		return counterType;
	}

	public void setCounterType(long counterType) {
		this.counterType = counterType;
	}

	public Timestamp getCounterExpTime() {
		return counterExpTime;
	}

	public void setCounterExpTime(Timestamp counterExpTime) {
		this.counterExpTime = counterExpTime;
	}

	public double getCounterValue() {
		return counterValue;
	}

	public void setCounterValue(double counterValue) {
		this.counterValue = counterValue;
	}

	public double getCounterThreshold() {
		return counterThreshold;
	}

	public void setCounterThreshold(double counterThreshold) {
		this.counterThreshold = counterThreshold;
	}
}
