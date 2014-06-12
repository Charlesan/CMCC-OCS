package com.ocs.rf;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.ocs.bean.account.Account;
import com.ocs.bean.account.PackageInfo;
import com.ocs.bean.account.RuleUsage;
import com.ocs.bean.event.DataTrafficEvent;
import com.ocs.bean.event.RatingResult;

public class RatingFunctionImpl implements RatingFunction {

	@Override
	public RatingResult dataTrafficRating(Account account,
			DataTrafficEvent event) {
		// TODO Auto-generated method stub
        try {
            // load up the knowledge base
	        KieServices ks = KieServices.Factory.get();
    	    KieContainer kContainer = ks.getKieClasspathContainer();
        	KieSession kSession = kContainer.newKieSession("ksession-rules");

            // go !
//            Message message = new Message();
//            message.setMessage("Hello World");
//            message.setStatus(Message.HELLO);
//            kSession.insert(message);
//            kSession.fireAllRules();
        
        	
        	kSession.insert(event);
        	PackageInfo mainSer = account.getMainService();
        	for(RuleUsage ru : mainSer.usages){
        		kSession.insert(ru);
        	}
        	
        	for(PackageInfo addSer : account.getAdditionalServices()){
        		for(RuleUsage ru : addSer.usages){
            		kSession.insert(ru);
            	}
        	}
        	
        	RatingResult rr = new RatingResult();
        	kSession.setGlobal("result", rr);
      
        	System.out.println("Start to match rules >>>>>>> ");
        	
        	kSession.fireAllRules();
        	
        	rr = (RatingResult) kSession.getGlobal("result");
        	kSession.dispose();
        	
        	//  ²éÑ¯×Ê·Ñ~~~~
        	double fee = 0;
        	if(rr.ruleID.equals("Rule_DG3G19_4"))
        		fee = 0.5;
        	rr.quantity = event.getProduceQuantity();
        	rr.totalPrice = rr.quantity*fee/1024;
        	
//        	System.out.println("\n-- Matching Result : ");
//        	rr.printObject();
//        	System.out.println();
//        	
//        	System.out.println("end >>>>>>> ");
        	
        	return rr;
        } catch (Throwable t) {
            t.printStackTrace();
        }
		
		return null;
	}

}
