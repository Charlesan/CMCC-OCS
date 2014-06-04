package com.ocs.bean.account;

import java.util.ArrayList;

public class Account {
	public String accountID;
	public String phoneNumber;
	public String numberAttribution;
	public PackageInfo mainService;
	public ArrayList<PackageInfo> additionalServices;
	
	public PackageInfo getMainServicce(){
		return mainService;
	}
	
	public void printObject(){
		String as = "";
		for( PackageInfo pi : additionalServices ){
			as += pi.getPrintString();
			as += "\n";
		}
		
		String s = "";
		s = "Account[\n"+
				"\taccountID:"+accountID+",\n"+
				"\tphoneNumber:"+phoneNumber+",\n"+
				"\tnumberAttribution:"+numberAttribution+",\n"+
				"\tmainService:\n\t"+mainService.getPrintString()+",\n"+
				"\tadditionalServices:[\n\t"+as+"],"+
				"\t]";
		
		System.out.println(s);
	}
}
