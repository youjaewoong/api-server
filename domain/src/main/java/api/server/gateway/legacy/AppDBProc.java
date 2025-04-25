package api.server.gateway.legacy;



import api.server.common.helper.DBConnectionManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class AppDBProc {
	protected DBConnectionManager dbcm;
	
	protected Connection conn;
	protected PreparedStatement pstmt;
	protected CallableStatement cstmt;
	protected ResultSet rs;
	protected StringBuffer sbQuery;
	
	public AppDBProc(DBConnectionManager dbcm){
		this.dbcm = dbcm;
	}
	
    
    

}