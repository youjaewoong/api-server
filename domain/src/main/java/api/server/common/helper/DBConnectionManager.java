
package api.server.common.helper;

import api.server.common.helper.cipher.KmsCipher;
import api.server.gateway.legacy.AbstractApp;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * This class is a Singleton that provides access to one or many
 * connection pools defined in a Property file. A client gets
 * access to the single instance through the static getInstance()
 * method and can then check-out and check-in connections from a pool.
 * When the client shuts down it should call the release() method
 * to close all open connections and do other clean up.
 */
public class DBConnectionManager {
    //added by kong
	/*
    private final long RELEASE_PERIOD = 4 * 60 * 60 * 1000;         //Period of releasing connection(4 hours)
    private final long EXECUTE_QUERY = 10 * 60 * 1000;              //Period of executing query for protecting disconnect(10 minutes)
    */
    //Edgar 수정 2024.04.02 
	private final long RELEASE_PERIOD = 4 * 60 * 1000;  // 4븐 
	private final long EXECUTE_QUERY = 30 * 1000;       // CheckConnection 주기 : 30초 	
    
    static private DBConnectionManager instance;                        // The single instance
    static private int clients;

    private Vector<Driver> drivers = new Vector<Driver>();
    private PrintWriter log;
    private Hashtable<String, DBConnectionPool> pools = new Hashtable<String, DBConnectionPool>();
    private KmsCipher cipher = new KmsCipher("01");

    /**
     * Returns the single instance, creating one if it's the
     * first time this method is called.
     *
     * @return DBConnectionManager The single instance.
     */
    static synchronized public DBConnectionManager getInstance(){
        if(instance == null){
            instance = new DBConnectionManager();
        }
        clients++;
        return instance;
    }

    /**
     * A private constructor since this is a Singleton
     */
    private DBConnectionManager(){
        init();
    }

    /**
     * Returns a connection to the named pool.
     *
     * @param name The pool name as defined in the properties file
     * @param con The Connection
     */
    public void freeConnection(String name, Connection con){
    	DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if(pool != null){
            pool.freeConnection(con);
        }
    }

    /**
     * Returns an open connection. If no one is available, and the max
     * number of connections has not been reached, a new connection is
     * created.
     *
     * @param name The pool name as defined in the properties file
     * @return Connection The connection or null
     */
    public Connection getConnection(String name) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            return pool.getConnection();
        }
        return null;
    }

    /**
     * 2005.05.09 added by kong
     * SQLException 시 해당 Connection close처리(DB와 Connection이 끊어진 경우)
     * @param name The pool name as defined in the properties file
     * @param con The Connection
     */
    public void closeConnection(String name, Connection con){
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            pool.closeConnection(con);
        }
    }

    /**
     * Returns an open connection. If no one is available, and the max
     * number of connections has not been reached, a new connection is
     * created. If the max number has been reached, waits until one
     * is available or the specified time has elapsed.
     *
     * @param name The pool name as defined in the properties file
     * @param time The number of milliseconds to wait
     * @return Connection The connection or null
     */
    public Connection getConnection(String name, long time){
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if(pool != null){
            return pool.getConnection(time);
        }
        return null;
    }

    /**
     * Closes all open connections and deregisters all drivers.
     */
    public synchronized void release(){
        try{
	    	// Wait until called by the last client
	        if(--clients != 0){
	            return;
	        }

	        Enumeration allPools = pools.elements();
	        while(allPools.hasMoreElements()){
	            DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
	            pool.release();
	        }
	        Enumeration allDrivers = drivers.elements();
	        while(allDrivers.hasMoreElements()){
	            Driver driver = (Driver) allDrivers.nextElement();
	            try{
	                DriverManager.deregisterDriver(driver);
	                log("[DBConnectionManager::release]Deregistered JDBC driver " + driver.getClass().getName());
	            }catch (SQLException e){
	                log(e, "[DBConnectionManager::release]Can't deregister JDBC driver: " + driver.getClass().getName());
	            }
	        }
        }catch(Exception e){
        	log("[DBConnectionManager::release]Exception : " + e);
        }finally{
        	instance = null;
        }
    }

    /**
     * Creates instances of DBConnectionPool based on the properties.
     * A DBConnectionPool can be defined with the following properties:
     * <PRE>
     * &lt;poolname&gt;.url         The JDBC URL for the database
     * &lt;poolname&gt;.user        A database user (optional)
     * &lt;poolname&gt;.password    A database user password (if user specified)
     * &lt;poolname&gt;.maxconn     The maximal number of connections (optional)
     * </PRE>
     *
     */
    //private void createPools(Properties props){
    private void createPools() {
        //Enumeration propNames = props.propertyNames();

        try {

            KmsCipher kmsCipher = new KmsCipher();

            String[] propNames = AbstractApp.g_appCfg.getDBConfigNames();

            //while(propNames.hasMoreElements()){
            for(int i=0; i<propNames.length; i++){
                String url = AbstractApp.g_appCfg.getDBUrl(propNames[i]);
                if(url == null){
                    log("[DBConnectionManager::createPools]No URL specified for " + propNames[i]);
                    continue;
                }

                //String user = AbstractApp.g_appCfg.getDBUser(propNames[i]); //props.getProperty(poolName + ".user");
                //String password = AbstractApp.g_appCfg.getDBPassword(propNames[i]); //props.getProperty(poolName + ".password");

                String user = kmsCipher.decryptProperty(AbstractApp.g_appCfg.getDBUser(propNames[i]));
                String password = kmsCipher.decryptProperty(AbstractApp.g_appCfg.getDBPassword(propNames[i]));
                String maxconn = AbstractApp.g_appCfg.getDBMaxConn(propNames[i]); //props.getProperty(poolName + ".maxconn", "0");
                //added by kong 20040910
                String initconn = AbstractApp.g_appCfg.getDBInitConn(propNames[i]); //props.getProperty(poolName + ".initconn", "0");
                //added by kong 20121119
                String limitmax = AbstractApp.g_appCfg.getDBLimitMax(propNames[i]);


                int max;
                int init;
                int limit;
                try{
                    max = Integer.valueOf(maxconn).intValue();
                }catch(NumberFormatException e){
                    log("[DBConnectionManager::createPools]Invalid maxconn value " + maxconn + " for " + propNames[i]);
                    System.out.println("[DBConnectionManager::createPools]Invalid maxconn value " + maxconn + " for " + propNames[i]);
                    max = 5;
                }
                try{
                    init = Integer.valueOf(initconn).intValue();
                }catch(NumberFormatException e){
                    log("[DBConnectionManager::createPools]Invalid initconn value " + initconn + " for " + propNames[i]);
                    init = 1;
                }
                try{
                    limit = Integer.valueOf(limitmax).intValue();
                }catch(NumberFormatException e){
                    log("[DBConnectionManager::createPools]Invalid limitmax value " + limitmax + " for " + propNames[i]);
                    limit = 0;
                }

                DBConnectionPool pool =
                        new DBConnectionPool(propNames[i], url, user, password, init, max, limit);
                pools.put(propNames[i], pool);

            }

            log("[DBConnectionManager::createPools]DBConnectionManager is started!!!");
        } catch (Exception e) {
            System.err.println((new Date().toString()).substring(0, 20)+"[setPools]Exception : " + e.toString());
        }

    }

    /**
     * Loads properties and initializes the instance with its values.
     */
    private void init(){
        loadDrivers();
        createPools();
    }

    /**
     * Loads and registers all JDBC drivers. This is done by the
     * DBConnectionManager, as opposed to the DBConnectionPool,
     * since many pools may share the same driver.
     *
     */
    //private void loadDrivers(Properties props
    private void loadDrivers(){
        String driverClasses = AbstractApp.g_appCfg.getDBDrivers();//props.getProperty("drivers");
        StringTokenizer st = new StringTokenizer(driverClasses);
        while(st.hasMoreElements()){
            String driverClassName = st.nextToken().trim();
            try{
                Driver driver = (Driver) Class.forName(driverClassName).newInstance();
                DriverManager.registerDriver(driver);
                drivers.addElement(driver);
                //log("Registered JDBC driver " + driverClassName);
            }catch(Exception e){
                log("[DBConnectionManager::loadDrivers]Can't register JDBC driver: " + driverClassName + ", Exception: " + e);
            }
        }
    }

    /**
     * Writes a message to the log file.
     */
    private void log(String msg){
        log.println(new Date() + ": " + msg);
        //System.out.println(new Date() + ": " + msg);
    }

    /**
     * Writes a message with an Exception to the log file.
     */
    private void log(Throwable e, String msg){
        log.println(new Date() + ": " + msg);
        //e.printStackTrace(log);
    }

    /**
     * This inner class represents a connection pool. It creates new
     * connections on demand, up to a max number if specified.
     * It also makes sure a connection is still open before it is
     * returned to a client.
     */
    class DBConnectionPool{
    	//사용 중인 Connection 수-->사용안함 20121119 by kong
        private int checkedOut;
        //대기 중인 Connections
        private Vector<Connection> freeConnections = new Vector<Connection>();
        //생성된 모든 Connections의 생성 시간 관리
        private Hashtable<Connection, Long> allConnections = new Hashtable<Connection, Long>();
        //임시 생성된 Connection 관리2012-11-19 7:12오전 by kong
        //임시 connection은 freeconnection시 close되나, allConnections에 add, remove하여 추가 관리한다.
        private Vector<Connection> tempConnections = new Vector<Connection>();
        
        private int maxConn;
        private int initConn;
        private int limitMax;
        private String name;
        private String password;
        private String URL;
        private String user;

        /**
         * Creates new connection pool.
         *
         * @param name The pool name
         * @param URL The JDBC URL for the database
         * @param user The database user, or null
         * @param password The database user password, or null
         * @param maxConn The maximal number of connections, or 0
         *   for no limit
         */
        public DBConnectionPool(String name, String URL, String user, String password,
            int initConn, int maxConn, int limitMax){
            this.name = name;
            this.URL = URL;
            this.user = user;
            this.password = password;
            this.maxConn = maxConn;
            this.initConn = initConn;
            this.limitMax = limitMax;

            //added by kong 20040910
            if(initConn > 0 && initConn <= maxConn){
            	for(int i=0; i<initConn; i++){
            		Connection con = newConnection();
            		freeConnections.addElement(con);
                    //allConnections.put(con, new Long(new Date().getTime()));
            		
                    //log("[DBConnectionPool::"+name+"]initConnection :" + con);
            	}
            }else{
                log("[DBConnectionPool::"+name+"]Please check init & max connections.(init:"+initConn+",max:"+maxConn+")");
            }

            log("[DBConnectionPool::"+name+"]freeConnections:"+freeConnections.toString());
            log("[DBConnectionPool::"+name+"]allConnections:"+allConnections.toString());
        }

        /**
         * Checks in a connection to the pool. Notify other Threads that
         * may be waiting for a connection.
         *
         * @param con The connection to check in
         */
        public synchronized void freeConnection(Connection con){
            // Put the connection at the end of the Vector
        	try{
                //connection 유효성 체크
                if(con != null/* && !con.isClosed()*/){
                    //connection 생성시간이 일정시간을 초과한 경우, close처리
                    long curTime = new Date().getTime();
                    long conTime = allConnections.get(con);
                    long diffTime = curTime - conTime;
                    if(diffTime > RELEASE_PERIOD){
                        //생성시간이 RELEASE_PERIOD시간을 초과한 경우, close처리, Edgar 로깅변경
                        log("[DBConnectionPool::freeConnection:"+name+"]close connection(over "+ RELEASE_PERIOD +" milliseconds): " + con + "," + (curTime - conTime));
                        closeConnection(con);
                    }else{
                        //모든 connection의 auto commit을 true로 업데이트 2012-10-04 6:00오후 by kong
                    	if(!con.getAutoCommit()){
                    		log("[DBConnectionPool::freeConnection:"+name+"]auto commit is false : " + con);
                    		con.commit();
                    		con.setAutoCommit(true);
                    	}
                        
                        //tempConnections 추가 2012-11-19 7:20오전 by kong
                        if(tempConnections.contains(con) || !allConnections.containsKey(con)){
                            //macConn을 초과하여 임시로 생성된 Connection은 close처리함
                            tempConnections.removeElement(con);
                            closeConnection(con);
                            log("[DBConnectionPool::freeConnection:"+name+"]Close temporary connection.(con:"+con+", maxConn:"+maxConn+",tempConn:"+tempConnections.size()+")");
                        }else{
                            freeConnections.addElement(con);
                        }
                        
                    }
	        	}
        	}catch(SQLException e){
        		log("[DBConnectionPool::freeConnection:"+name+"]Removed bad connection from " + name + ":" + con);
        	}            

            //freeconnection시 무조건 차감 2012-11-19 11:11오전 by kong
            //checkedOut--;    
            notifyAll();
            
            log("[DBConnectionPool::freeConnection:"+name+"]"+freeConnections.size()+"/"+allConnections.size());
        }

        /**
         * Checks out a connection from the pool. If no free connection
         * is available, a new connection is created unless the max
         * number of connections has been reached. If a free connection
         * has been closed by the database, it's removed from the pool
         * and this method is called again recursively.
         */
        public synchronized Connection getConnection(){
            Connection con = null;
            
            if(freeConnections.size() > 0){
                // Pick the first Connection in the Vector
                // to get round-robin usage
            	/*
                con = (Connection) freeConnections.firstElement();
                freeConnections.removeElementAt(0);
                */
            	// Edgar 2024.04.02 CheckConnection시와 freeConnection 가져올때 동기화되도록 변경
            	con = (Connection)freeConnections.remove(0); // 첫번째 index element를 얻어옴 
            	
                try{
                    if(con.isClosed()){
                        log("[DBConnectionPoll::getConnection:"+name+"]Removed bad connection - isClosed");
                        // Try again recursively
                        con = getConnection();
                    }else{
                    	//added by kong 20040910
                    	//checkedOut++;
                    }
                }catch(SQLException e){
                    log("[DBConnectionPoll::getConnection:"+name+"]Removed bad connection - SQLException" + e);
                    // Try again recursively
                    con = getConnection();
                }
                log("[DBConnectionPool::getConnection:"+name+"]"+freeConnections.size()+"/"+allConnections.size());
            //}else if(checkedOut < maxConn){
            }else if(allConnections.size() < maxConn){    
                con = newConnection();
                //allConnections.put(con, new Long(new Date().getTime()));
                
                //if(con != null) checkedOut++;
                log("[DBConnectionPool::getConnection:"+name+"]"+freeConnections.size()+"/"+allConnections.size());
            }else{
                //max connection 제한이 있는 경우
                //if(limitMax > 0 && checkedOut >= (maxConn * limitMax)){
                if(limitMax != 0 && (allConnections.size()) >= (maxConn * limitMax)){
                	log("[DBConnectionPool::getConnection:"+name+"]Exceeded limitation of max connection.(maxConn:"+maxConn+",limitMax:"+limitMax+",allConn:"+allConnections.size()+",tempConn:"+tempConnections.size()+")");
                	return null;
                }
                
                //모든 connection이 사용 중이고, maxConn을 초과한 경우, 임시 connection 생성(allConnections에 제외하고, freeConnection시에 close처리)
                con = newConnection();
                
                //임시 connection은 allConnection과 tempConnection에 의해 관리
                //allConnections.put(con, new Long(new Date().getTime()));
                tempConnections.addElement(con);
                
                //if(con != null) checkedOut++;                
                log("[DBConnectionPool::getConnection:"+name+"]Create temporary connection.(con:"+con+",maxConn:"+maxConn+",tempConn:"+tempConnections.size()+")");
            }

            return con;
        }

        /**
         * Checks out a connection from the pool. If no free connection
         * is available, a new connection is created unless the max
         * number of connections has been reached. If a free connection
         * has been closed by the database, it's removed from the pool
         * and this method is called again recursively.
         * <P>
         * If no connection is available and the max number has been
         * reached, this method waits the specified time for one to be
         * checked in.
         *
         * @param timeout The timeout value in milliseconds
         */
        public synchronized Connection getConnection(long timeout){
            long startTime = new Date().getTime();
            Connection con;
            while((con = getConnection()) == null){
                try{
                    wait(timeout);
                }catch(InterruptedException e){}
                if((new Date().getTime() - startTime) >= timeout){
                    // Timeout has expired
                    return null;
                }
            }
            return con;
        }
        
        public synchronized Vector<Connection> getConnections(){
            return freeConnections;
        }
        
        public synchronized Hashtable<Connection, Long> getAllConnections(){
            return allConnections;
        }
        
        public String getName(){
        	return name;
        }

        public synchronized void closeConnection(Connection con){
        	try{
        		allConnections.remove(con);
                
                con.close();
        		con = null;
        		log("[DBConnectionPool::closeConnection:"+name+"]Closed connection for pool:"+freeConnections.size() + "/" + allConnections.size());
        	}catch(Exception e){
        		log("[DBConnectionPool::closeConnection:"+name+"]Can't close connection for pool " + ":" + con);
        	}

            notifyAll();
        }
        
        public synchronized void forcedCloseConnection(Connection con){
        	try{
        		allConnections.remove(con);
        		freeConnections.remove(con);
                
                con.close();
        		con = null;
        		log("[DBConnectionPool::forcedCloseConnection:"+name+"]Forced close connection for pool:"+freeConnections.size() + "/" + allConnections.size());
        	}catch(Exception e){
        		log("[DBConnectionPool::forcedCloseConnection:"+name+"]Can't close connection for pool " + ":" + con);
        	}

            //checkedOut--;
            //log("[DBConnectionPool::forcedCloseConnection:"+name+"]Create temporary connection.(con:"+con+",maxConn:"+maxConn+",tempConn:"+tempConnections.size()+")");
        }

        /**
         * Closes all available connections.
         */
        public synchronized void release(){
            Enumeration connections = freeConnections.elements();
            while(connections.hasMoreElements()){
                Connection con = (Connection) connections.nextElement();
                try{
                    allConnections.remove(con);
                    
                    con.close();
                    log("[DBConnectionPoll::release:"+name+"]Closed connection for pool " + name + ":" + con);
                }catch(SQLException e){
                    log(e, "[DBConnectionPoll::release:"+name+"]Can't close connection for pool " + name + ":" + con);
                }
                //checkedOut--;
            }
            freeConnections.removeAllElements();
        }

        /**
         * Creates a new connection, using a userid and password
         * if specified.
         */
        private Connection newConnection(){
            Connection con = null;
            try{
                if(user == null){
                    con = DriverManager.getConnection(URL);
                }else{
                	con = DriverManager.getConnection(URL, user, password);
                }
                log("[DBConnectionPool::newConnection:"+name+"]Created a new connection in pool :" + con);
            }catch(SQLException e){
                log(e, "[DBConnectionPool::newConnection:"+name+"]Can't create a new connection for " + URL);
                return null;
            }

            return con;
        }
    }
}
