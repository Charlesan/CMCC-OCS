package com.ocs.dao.impl;

import com.ocs.bean.packagerule.Package;
import com.ocs.bean.packagerule.Package.ServiceType;
import com.ocs.dao.PackageDAO;

public class TestBDB {
    public static final void main(String[] args){
    	
//    	System.out.println(Class.class.getClass().getResource("/").getPath());
//    	System.out.println(System.getProperty("user.dir"));
    	
    	Package pkg = TestPackageDataGenerator.generateDG3G19();
//    	System.out.println(pkg);
    	BDBEnv env = new BDBEnv();
    	env.setup(false);
    	PackageDAO dao = new PackageDAOBDBImpl(env);
//    	dao.putPackage(pkg);
    	
//    	System.out.println(dao.getPackage(pkg.getPackageID()));
    	
    	System.out.println(dao.queryTariff(pkg.getPackageID(), ServiceType.DATA_TRAFFIC, "Rule_DG3G19_BASE"));
    }

}
