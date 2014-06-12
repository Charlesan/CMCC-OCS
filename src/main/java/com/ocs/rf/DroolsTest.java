package com.ocs.rf;

import java.text.ParseException;
import java.util.ArrayList;

import com.ocs.rf.RatingFunctionImpl;
import com.ocs.bean.account.Account;
import com.ocs.bean.account.PackageInfo;
import com.ocs.bean.account.RuleUsage;
import com.ocs.bean.event.DataTrafficEvent;
import com.ocs.dao.PackageDAO;
import com.ocs.dao.impl.BDBEnv;
import com.ocs.dao.impl.PackageDAOBDBImpl;

/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

    public static final void main(String[] args) throws ParseException {
    	    	
    	String number = "13412345678";
    	
    	DataTrafficEvent dte = new DataTrafficEvent();
//    	dte.phoneNumber = number;
//    	dte.produceChannel = "All";
//    	dte.produceLocation = "国内";
////    	dte.produceLocation = "省内";
//    	dte.produceQuantity = 512;
////    	// 忙时
////    	dte.produceTime_start = "11:01:00";
////    	dte.produceTime_end = "11:01:05";
//    	// 闲时
//    	dte.produceTime_start = "08:01:00";
//    	dte.produceTime_end = "08:01:55";
    	
    	Account account = new Account();
    	account.accountID = number;
    	account.numberAttribution = "预付费";
        account.phoneNumber = number;
        // Pacage - mainService
    	account.mainService = new PackageInfo();
    	account.mainService.packageID = "DG3G_19";
    	account.mainService.packageName = "动感地带3G网聊卡19元套餐";
    	account.mainService.usages = new ArrayList<RuleUsage>();
    	for(int i = 1; i <= 4; i++){
    		RuleUsage rule = new RuleUsage();
    		
    		switch(i){
    		case 1:
    			rule.ruleID = "Rule_DG3G19_1";
    			rule.ruleName = "Rule_DG3G19_M_Z";
    			//rule.remainQuantity = 1000;
    			rule.remainQuantity = 0;
    			break;
    		case 2:
    			rule.ruleID = "Rule_DG3G19_2";
    			rule.ruleName = "Rule_DG3G19_Y_SN";
//    			rule.remainQuantity = 500.5;
    			rule.remainQuantity = 0;
    			break;
    		case 3:
    			rule.ruleID = "Rule_DG3G19_3";
    			rule.ruleName = "Rule_DG3G19_Y_SNX";
//    			rule.remainQuantity = 1500.5;
    			rule.remainQuantity = 0;
    			break;
    		case 4:
    			rule.ruleID = "Rule_DG3G19_4";
    			rule.ruleName = "Rule_DG3G19_BASE";
    			rule.remainQuantity = -1;
    			break;
    		default:break;
    		}
    		account.mainService.usages.add(rule);
    	}
    	
    	account.additionalServices = new ArrayList<PackageInfo>();
    	for(int i = 1; i <= 1; i++){
    			PackageInfo pi = new PackageInfo();
    			
    			switch(i){
        		case 1:
        			pi.packageID = "DJ10";
        			pi.packageName = "流量叠加包10元套餐";
        			pi.usages = new ArrayList<RuleUsage>();
        			for(int j = 1; j <= 2; j++){
        				RuleUsage rule = new RuleUsage();
                		
                		switch(i){
                		case 1:
                			rule.ruleID = "DJ10_1";
                			rule.ruleName = "Rule_DJ10_GN";
//                			rule.remainQuantity = 100;
                			rule.remainQuantity = 0;
                			break;
                		case 2:
                			rule.ruleID = "DJ10_2";
                			rule.ruleName = "Rule_DJ10_SN";
//                			rule.remainQuantity = 50.5;
                			rule.remainQuantity = 0;
                			break;
                		default:break;
                		}
                		pi.usages.add(rule);
        			}
        			break;
        		default:break;
        		}
    			account.additionalServices.add(pi);
    	}
    	
    	System.out.println("Generate test user information >>>>>> ");
    	System.out.println("流量计费事件：");
//    	dte.printObject();
    	System.out.println(dte);
    	System.out.println("用户状态：");
//    	account.printObject();
    	System.out.println(account);
    	System.out.println("Generation end >>>>>> \n\n");
    	
    	RatingFunctionImpl rf = new RatingFunctionImpl();
    	rf.dataTrafficRating(account, dte);
    }

    public static class Message {

        public static final int HELLO = 0;
        public static final int GOODBYE = 1;

        private String message;

        private int status;

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return this.status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

    }

}
