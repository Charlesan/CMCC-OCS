package com.ocs.dao;

import com.ocs.bean.session.CFSession;

/**
 * ��ȡCF�����ĻỰ���ݵĽӿ�
 * @author Wang Chao
 *
 */
public interface CFSessionDAO {
	public int writeCFSession(CFSession session);

	public int deleteCFSession(); 
}
