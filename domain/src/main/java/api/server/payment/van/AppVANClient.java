/*
 * AppVANClient.java
 *
 * Created on 2008년 9월 10일 (수), 오전 11:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package api.server.payment.van;


import api.server.payment.gateway.AbstractApp;
import api.server.payment.gateway.AbstractWorker;
import api.server.payment.my.MyException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public abstract class AppVANClient {
    public static int TYPE_LEN = 1;
    public static int TYPE_CHAR = 2;


    private AppVANManager vanMgr;
    private String name;
    private String id;

    protected Socket m_socket;
    protected InputStream m_is;
    protected OutputStream m_os;

    private boolean isOpen = false;

    public AppVANClient(){

    }

    public void init(AppVANManager vanMgr, String name, String id) throws MyException {
        this.vanMgr = vanMgr;
        this.name = name;
        this.id = id;

        createConnection();
    }

    public String getName(){
        return name;
    }

    public String getID(){
        return id;
    }

    public String getHost(){
        return AbstractApp.g_appCfg.getVANHost(name);
    }

    public int getPort(){
        return AbstractApp.g_appCfg.getVANPort(name);
    }

    public int getTimeout(){
        return AbstractApp.g_appCfg.getVANTimeout(name);
    }

    public void checkConnection(AbstractWorker appWorker) throws MyException{
        boolean isProblem = false;
        try{
            if(m_socket.isClosed()){
                isProblem = true;
                appWorker.errorLog("[AppVANClient::checkConnection]"+name+":"+id+" is closed!!");
            }else if(!m_socket.isConnected()){
                isProblem = true;
                appWorker.errorLog("[AppVANClient::checkConnection]"+name+":"+id+" is not connected!!");
            }else if(m_socket.isOutputShutdown()){
                isProblem = true;
                appWorker.errorLog("[AppVANClient::checkConnection]"+name+":"+id+" is outputstream shutdown!!");
            }else if(m_socket.isInputShutdown()){
                isProblem = true;
                appWorker.errorLog("[AppVANClient::checkConnection]"+name+":"+id+" is inputstream shutdown!!");
            }

            if(isProblem){
                forceReConnection(appWorker);
            }
        }catch(MyException e){
            throw e;
        }catch(Exception e){
            throw new MyException("[AppVANClient::checkConnection]", MyException.SY20, "Exception:"+e, MyException.sysErrMsg);
        }
    }

    public void forceReConnection(AbstractWorker appWorker) throws MyException{
        try{
            appWorker.errorLog("VAN Close Connection : " + name+":"+id+":"+m_socket);

            //connection재연결
            closeConnection();
            createConnection();

            appWorker.errorLog("VAN Open Connection : " + name+":"+id+":"+m_socket);
        }catch(MyException e){
            throw e;
        }catch(Exception e){
            throw new MyException("[AppVANClient::forceReConnection]", MyException.SY20, "Exception:"+e, MyException.sysErrMsg);
        }
    }


    public void createConnection() throws MyException{
        try{
            //m_socket = new Socket(AbstractApp.g_appCfg.getVANHost(name), AbstractApp.g_appCfg.getVANPort(name));
            m_socket = new Socket();
            m_socket.connect(new InetSocketAddress(AbstractApp.g_appCfg.getVANHost(name), AbstractApp.g_appCfg.getVANPort(name)), 10000);
            m_socket.setSoTimeout(AbstractApp.g_appCfg.getVANTimeout(name));
               
            m_is = m_socket.getInputStream();
            m_os = m_socket.getOutputStream();

            isOpen = true;
            System.out.println(name + " " + id + "(" + AbstractApp.g_appCfg.getVANHost(name) + ":" + AbstractApp.g_appCfg.getVANPort(name) + ") is connected!!!");
        }catch(Exception e){
            System.out.println(name + " " + id + "(" + AbstractApp.g_appCfg.getVANHost(name) + ":" + AbstractApp.g_appCfg.getVANPort(name) + ") isn't connected!!!");
            throw new MyException("[AppVANClient::createConnection]", MyException.SY20, "Exception:"+e, MyException.sysErrMsg);
        }
    }

    public void closeConnection(){
        try{
            if(m_is != null) m_is.close();
            if(m_os != null) m_os.close();
            if(m_socket != null) m_socket.close();
            System.out.println(name + " " + id + "(" + AbstractApp.g_appCfg.getVANHost(name) + ":" + AbstractApp.g_appCfg.getVANPort(name) + ") is disconnected!!!");
        }catch(Exception e){
            System.out.println("AppVANClient::closeConnection]Exception : " + e);
        }
        isOpen = false;
    }

    public abstract void sendData(byte[] dataBytes) throws Exception;
    public abstract byte[] recvData(int type) throws Exception;
}
