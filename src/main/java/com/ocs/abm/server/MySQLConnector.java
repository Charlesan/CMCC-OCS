package com.ocs.abm.server;


import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import com.ocs.bean.abm.Balance;
import com.ocs.bean.abm.Counter;
import com.ocs.bean.abm.DeductBalance;
import com.ocs.bean.abm.DeductCounter;
import com.ocs.bean.abm.ReserveBalance;



public class MySQLConnector {
	public Connection getConnection(){
		//����������
		final String driverName="com.mysql.jdbc.Driver";
//		//���ݿ��û���
//		final String userName="miu";
//		//����
//		final String userPasswd="123456";
//		//���ݿ���
//		final String dbName="ABM";
//		//�����ַ���
//		String url="jdbc:mysql://localhost/"+dbName+"?user="+userName+"&password="+userPasswd;
		
		//���ݿ��û���
		final String userName="froot";
		//���ݿ���
		final String dbName="abm";
		//����
		final String userPasswd="a304";
		//���ݿ��������ַ
		final String ipAddr = "222.200.185.54:3306";
		//�����ַ���
		String url="jdbc:mysql://"+ipAddr+"/"+dbName+"?user="+userName+"&password="+userPasswd;	
		
		try {
			Class.forName(driverName).newInstance();
			Connection connection= (Connection) DriverManager.getConnection(url);
			return connection;
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<Balance> getAccountBalance(long id){
		ArrayList<Balance> balancesList = new ArrayList<Balance>();
		
		//����
		String tableName="ACCT_BALANCE";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE ACCT_ID = '"+ id +"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Balance blc = new Balance(rs.getLong("ACCT_BALANCE_ITEM_ID"), 0, rs.getTimestamp("EXP_TIME"), rs.getDouble("AMT"));
				balancesList.add(blc);
				
				System.out.println(blc.toString());
			}
			
			//�ر�
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return balancesList;
		
	}
	
	public ArrayList<Balance> getAccountBalance(long acntId,long balanceId){
		ArrayList<Balance> balancesList = new ArrayList<Balance>();
		
		//����
		String tableName="ACCT_BALANCE";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE ACCT_ID = '"+ acntId +"' AND ACCT_BALANCE_ITEM_ID = '"+balanceId+"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Balance blc = new Balance(rs.getLong("ACCT_BALANCE_ITEM_ID"),0,rs.getTimestamp("EXP_TIME"),rs.getDouble("AMT"));
				balancesList.add(blc);
				
				System.out.println(blc.toString());
			}
			
			//�ر�
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return balancesList;
		
	}
	
	public ArrayList<Counter> getCounterByObjectID(long id){
		ArrayList<Counter> countersList = new ArrayList<Counter>();
		
		//����
		String tableName="COUNTER";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE OBJECT_ID = '"+ id +"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Counter cnt = new Counter(rs.getLong("COUNTER_ID"), 
										rs.getString("COUNTER_TYPE"), 
										rs.getTimestamp("EXP_DATE"), 
										rs.getDouble("COUNTER_VALUE"),
										rs.getDouble("COUNTER_THRESHOLD"));
				countersList.add(cnt);
				
				System.out.println(cnt.toString());
			}
			
			//�ر�
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return countersList;
		
	}
	
	public ArrayList<Counter> getCounterByCounterID(long id){
		ArrayList<Counter> countersList = new ArrayList<Counter>();
		
		//����
		String tableName="COUNTER";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE COUNTER_ID = '"+ id +"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Counter cnt = new Counter(rs.getLong("COUNTER_ID"), 
							rs.getString("COUNTER_TYPE"), 
							rs.getTimestamp("EXP_DATE"), 
							rs.getDouble("COUNTER_VALUE"),
							rs.getDouble("COUNTER_THRESHOLD"));
				countersList.add(cnt);

				System.out.println(cnt.toString());
			}
			
			//�ر�
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return countersList;
		
	}
	
	public boolean deductBalance(ArrayList<DeductBalance> dBalances){
		// Deduct Balance print infos
		System.out.println("�˱����ۼ�- "+ dBalances.size() + "����¼");
		
		// ����
		String tableName = "ACCT_BALANCE";
		Connection connection = getConnection();
		Statement statement;
		
		for( DeductBalance dBalance : dBalances){
			ArrayList<Balance> rsltBalances = getAccountBalance(dBalance.getAccountID(), dBalance.getBalanceID());
			
			if(!rsltBalances.isEmpty()){
				// log
				System.out.println("���ҵ�ƥ�����ݣ����пۼ�");
				
				Balance rsltBalance = rsltBalances.get(0);
				rsltBalance.setBalanceValue(rsltBalance.getBalanceValue()-dBalance.getChgValue());
				
				System.out.println("�˻����ۼ���id - " + rsltBalance.getBalanceID() + "  value - "+rsltBalance.getBalanceValue());
				
				try {
					statement = (Statement) connection.createStatement();
					statement.executeQuery("SET NAMES UTF8");
							
					String str = "UPDATE "+tableName
							+ " SET AMT = ?,UPDATE_TIME = ? " 
							+ "WHERE ACCT_ID = '" + dBalance.getAccountID()
							+ "' AND ACCT_BALANCE_ITEM_ID = '" +dBalance.getBalanceID() + "'";
					PreparedStatement stm = (PreparedStatement) connection.prepareStatement(str);
							
					stm.setDouble(1, rsltBalance.getBalanceValue());
					stm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
							
					stm.executeUpdate();
							
					connection.close();
					
					System.out.println("�˻����ۼ��ɹ�");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("�˻����ۼ�ʧ��");
					return false;
				}
				
			}
		}
		
		return true;
	}
	
	public boolean deductCounter(ArrayList<DeductCounter> dCounters, long acctID){
		// Deduct Counter print infos
		System.out.println("�ۻ����ۼ�- " + dCounters.size() + "����¼");
		
		//����
		String tableName="COUNTER";
		Connection connection = getConnection();
		Statement statement;
			
		for(DeductCounter dCounter : dCounters){
			ArrayList<Counter> rsltCounters = getCounterByCounterID(dCounter.getCounterID());
			
			if(!rsltCounters.isEmpty()){
				// log
				System.out.println("���ҵ�ƥ�����ݣ����пۼ�");
				
				Counter rsltCounter = rsltCounters.get(0);
				rsltCounter.setCounterValue(rsltCounter.getCounterValue()-dCounter.getChgValue());
				
				System.out.println("�ۻ����ۼ���id - " + rsltCounter.getCounterID() + "  value - "+rsltCounter.getCounterValue());
				
				try {
					statement = (Statement) connection.createStatement();
					statement.executeQuery("SET NAMES UTF8");
							
					String str = "UPDATE " + tableName 
							+ " SET COUNTER_VALUE = ?,UPDATE_TIME = ? " 
							+ "WHERE COUNTER_ID = '" + dCounter.getCounterID() 
							+ "' AND OBJECT_ID = '" + acctID + "'";
					PreparedStatement stm = (PreparedStatement) connection.prepareStatement(str);
							
					stm.setDouble(1, rsltCounter.getCounterValue());
					stm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
							
					stm.executeUpdate();
							
					connection.close();
					
					System.out.println("�ۻ����ۼ��ɹ�");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("�ۻ����ۼ�ʧ��");
					return false;
				}
			}
		}
		
		return true;
	}

	public void addReserveBalances(ArrayList<ReserveBalance> rBalances){
		// Deduct Counter print infos
		System.out.println("Ԥ��- " + rBalances.size() + "����¼");
				
		//����
		String tableName="BALANCE_RESERVE";
		Connection connection = getConnection();
		Statement statement;
					
		for(ReserveBalance rb : rBalances){			
			try {
				statement = (Statement) connection.createStatement();
				statement.executeQuery("SET NAMES UTF8");
				
				String str = "INSERT INTO " + tableName +" (SESSIONID, SERVICE_ID, SERVICE_TYPE, ACCT_ID, ACCT_ITEM_ID, AMT, RESERVE_DATE, EXPIRY_DATE, UPDATE_TIME) values(?,?,?,?,?,?,?,?,?)";
				PreparedStatement stm = (PreparedStatement) connection.prepareStatement(str);
									
				stm.setString(1, rb.getSessionID());
				stm.setLong(2, rb.getServiceID());
				stm.setLong(3, rb.getServiceType());
				stm.setLong(4, rb.getAccountID());
				stm.setLong(5, rb.getAccountID());
				stm.setDouble(6, rb.getReserveAmount());
				stm.setTimestamp(7, (Timestamp) rb.getReserveDate().getTimestamp());
				stm.setTimestamp(8, (Timestamp) rb.getExpDate().getTimestamp());
				stm.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
				
				stm.executeUpdate();
									
				connection.close();
							
				System.out.println("Ԥ���ɹ�");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Ԥ��ʧ��");
			}
					
		}
		
	}

	public boolean deleteReserveBalances(ArrayList<ReserveBalance> rBalances){
		//����
		String tableName="BALANCE_RESERVE";
		Connection connection = getConnection();
		Statement statement;
		
		for(ReserveBalance rb : rBalances){	
			try {
				statement = (Statement) connection.createStatement();
				String sql="DELETE FROM "+tableName + " WHERE " 
						+ "SESSIONID = '"+ rb.getSessionID() +"'" 
						+ " AND ACCT_ID = '" + rb.getAccountID() +"'"
						+ " AND SERVICE_ID = '" + rb.getServiceID() + "'";
				System.out.println(sql);
			
				PreparedStatement stm = (PreparedStatement) connection.prepareStatement(sql);
				stm.execute();
		
				//�ر�
				statement.close(); 
				connection.close();
			
				return true;
			} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
