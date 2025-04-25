package api.server.gateway.legacy;

import api.server.common.helper.AppUtil;
import api.server.gateway.legacy.my.MyObjectFIFO;
import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Slf4j
public abstract class AbstractWorker {
public final static String CG = "CG";     //Connect
public final static String DG = "DG";     //Disconnect
public final static String RG = "RG";	  //Receive Data
public final static String SG = "SG";	  //Send Normal Data
public final static String SE = "SE";     //Send Error Data
public final static String ER = "ER";	  //Error
public final static String LG = "LG";	  //Log
public final static String CV = "CV";     //Connect to VAN
public final static String DV = "DV";     //Disconnect to VAN
public final static String SV = "SV";     //Send to VAN
public final static String RV = "RV";     //Receive from VAN

protected MyObjectFIFO m_idleWorkers;
protected MyObjectFIFO m_handoffBox;
protected String m_appNm;
protected int m_workerID;
protected boolean m_isProg;
protected String m_serverName;

protected Thread m_internalThread;
protected volatile boolean m_noStopRequested;

protected String logKey;  //access로그와 error로그 연결을 위해

public AbstractWorker(int workerPriority, MyObjectFIFO idleWorkers, int workerID, boolean isProg, String serverName){

        m_appNm = AbstractApp.g_appCfg.getAppName();
        m_idleWorkers = idleWorkers;
        m_workerID = workerID;
        m_isProg = isProg;
        m_serverName = serverName;
     
        m_handoffBox = new MyObjectFIFO(1); // only one slot

        //Just before returning, the thread should be created and started.
        m_noStopRequested = true;
        
        Runnable r = new Runnable(){
                public void run(){
                        try{
                                runWork();
                        }catch(Exception e){
                                System.out.println("ExtractWorker::run]Exception : " + e.toString());
                        }
                }
        };

        m_internalThread = new Thread(r);
        m_internalThread.setPriority(workerPriority);
        m_internalThread.start();
}

    public AbstractWorker(){}

    public int getWorkerID(){
            return m_workerID;
    }

    public void processRequest(Socket s) throws InterruptedException{
            m_handoffBox.add(s);
    }

    public void stopRequest(){
            m_noStopRequested = false;
            m_internalThread.interrupt();
    }

    public boolean isAlive(){
            return m_internalThread.isAlive();
    }

    private void runWork(){
                    Socket s = null;
                    InputStream in = null;
                    OutputStream out = null;
                    long processingTime = 0;

                    while(m_noStopRequested){
                            try{
                                    //Worker is ready to receive new service requests,
                                    //so it add itself to the idle worker queue.
                                    //임시 쓰레드는 쓰레드 풀에서 제외하며, 카운트만 한다.
                                    if(m_workerID == 99) m_idleWorkers.addTmpObjects();
                                    else  m_idleWorkers.add(this);
                                    

                                    //Wait here until the server puts a request into the handoff box.
                                    s = (Socket)m_handoffBox.remove();
                                    //Set read timeout

                                    logKey = AppUtil.getDateTime();
                                    
                                    String msg = "Connect Access IP : " + s.getInetAddress().toString()+":" +  s.getPort();
                                    accessLog(CG, msg.getBytes());

                                    try{
                                            // 제롬 테스트
                                            in = s.getInputStream();
                                            out = s.getOutputStream();
                                    }catch(IOException e){
                                            System.out.println("[AbstractWorker::runWork]IOException : " + e.toString());
                                            throw e;
                                    }

                            //processingTime = System.currentTimeMillis();

                            //while(true){
                            //	try{

                                    // 제롬 테스트 (소켓 응답데이터)
                                    runTransaction(s, in, out);
                            //		if(isDisconnect() || m_workerID == 99) break;
            //    }catch(MyException e){
            //        String msg = "Disconnect Socket("+e.getLoc()+e.getCode()+":"+e.getMessage()+")";
                            //		accessLog(DG, msg.getBytes());
                            //	}catch(Exception e){
            //        //System.out.println(new Date() + "[ExtractWorker::runWork]Disconnect Socket("+e+")");
            //        String msg = "Disconnect Socket("+e+")";
                            //		accessLog(DG, msg.getBytes());
                            //		break;
                            //	}
                            //}

                            }catch(InterruptedException e){
                                    System.out.println("[AbstractWorker::runWork]InterruptedException : " + e.toString());
                                    //re-assert the interrupt
                                    Thread.currentThread().interrupt();
                            }catch(Exception e){
                                    System.out.println("[AbstractWorker::runWork]Exception : " + e.toString());
                                    //e.printStackTrace();
                            }finally {
                                    //Try to close everything, ignoring any IOExceptions that might occur.
                                    try{
                                            if(in != null) in.close();
                                            if(out != null) out.close();
                                            if(s != null) s.close();
                                    }catch(IOException e){
                                    }finally{
                                            in = null;
                                            out = null;
                                            s = null;
                                    }

                                    accessLog(DG);
                            }

                            //임시 쓰레드 종료
                            if(m_workerID == 99){
                                    //AbstractApp.g_evtLis.onEvent(new MyEvent(null, 300, "[AbstractWorker::runWork]Temporary worker finish the work.\r\n"));
                                    m_idleWorkers.removeTmpObjects();
                                    break;
                            }
                    }
    }

    protected void printConsole(byte[] data){
        printConsole(LG, new String(data));
    }

    protected void printConsole(String type, byte[] data){
        printConsole(type, new String(data));
    }

    protected void printConsole(String type, String data){
        StringBuffer logBuf = new StringBuffer();
        logBuf.append("[Worker");
        logBuf.append(AppUtil.formatData(m_workerID, AppUtil.RIGHT, 2, '0'));
        logBuf.append(":");
        logBuf.append(AppUtil.formatData(logKey, AppUtil.LEFT, 14, ' '));
        logBuf.append(":");
        logBuf.append(type);
        logBuf.append("]");
        logBuf.append(data);

        System.out.println(logBuf.toString());
    }

	public int read(InputStream in, byte[] b, int off, int len) throws Exception{
		return in.read(b, off, len);
	}

    public int read(InputStream in) throws Exception{
        return in.read();
    }

    public int available(InputStream in) throws Exception{
        return in.available();
    }

    public void write(OutputStream out, byte data) throws Exception{
        out.write(data);
    }

	public void write(OutputStream out, byte[] data) throws Exception{
		out.write(data, 0, data.length);
	}

    public void write(OutputStream out, byte[] b, int off, int len) throws IOException{
		out.write(b, off, len);
	}

    public byte[] recvData(InputStream in) throws Exception {
        final int HEADER_LENGTH = 4; // Fixed length for the header

        try {
            // Read the header (data length)
            byte[] lengthBytes = readBytes(in, HEADER_LENGTH);
            if (lengthBytes == null) {
                return null;
            }

            int dataLength = Integer.parseInt(new String(lengthBytes).trim());

            // Validation for parsed data length
            if (dataLength <= 0 || dataLength > 1_000_000) { // Adjust max size as appropriate
                throw new IllegalArgumentException("Invalid data length: " + dataLength);
            }

            byte[] receivedData = new byte[dataLength];
            int totalBytesRead = 0;

            // Read the actual data
            while (totalBytesRead < dataLength) {
                int bytesToRead = dataLength - totalBytesRead;
                byte[] buffer = readBytes(in, bytesToRead);

                if (buffer == null) {
                    throw new EOFException("Unexpected end of stream while reading data");
                }

                System.arraycopy(buffer, 0, receivedData, totalBytesRead, buffer.length);
                totalBytesRead += buffer.length;
            }

            return totalBytesRead == dataLength ? receivedData : null;

        } catch (SocketTimeoutException e) {
            System.err.println("[AbstractWorker::recvData] SocketTimeoutException: " + e);
            throw e;
        } catch (Exception e) {
            System.err.println("[AbstractWorker::recvData] Exception: " + e);
            throw e;
        }
    }

    private byte[] readBytes(InputStream in, int length) throws Exception {
        byte[] buffer = new byte[length];
        int bytesRead = read(in, buffer, 0, length);
        return bytesRead == -1 ? null : buffer;
    }

	public void sendData(OutputStream out, byte[] data) throws Exception{
		try{
                    
			write(out, data, 0, data.length);
		}catch(Exception e){
			//System.out.println("[AbstractWorker::sendData]Exception : " + e);
			throw e;
		}
	}

    public void accessLog(String acType){
        accessLog(acType, null);
    }

    public void accessLog(String acType, byte data){
        byte[] bData = new byte[1];
        bData[0] = data;

        accessLog(acType, bData);
    }

    public void accessLog(String acType, byte[] data){
        StringBuffer logBuf = new StringBuffer();
        logBuf.append("[");
        logBuf.append(AppUtil.formatData(m_serverName, AppUtil.LEFT, 4, ' '));
        logBuf.append(":");
        logBuf.append("Worker");
        logBuf.append(AppUtil.formatData(m_workerID, AppUtil.RIGHT, 2, '0'));
        logBuf.append(":");
        logBuf.append(AppUtil.formatData(logKey, AppUtil.LEFT, 14, ' '));
        logBuf.append(":");
        logBuf.append(acType);
        logBuf.append("]");

        log.info(logBuf.toString(), data);
    }

    public void errorLog(String msg){
        StringBuffer logBuf = new StringBuffer();
        logBuf.append("[");
        logBuf.append(AppUtil.formatData(m_serverName, AppUtil.LEFT, 4, ' '));
        logBuf.append(":");
        logBuf.append("Worker");
        logBuf.append(AppUtil.formatData(m_workerID, AppUtil.RIGHT, 2, '0'));
        logBuf.append(":");
        logBuf.append(AppUtil.formatData(logKey, AppUtil.LEFT, 14, ' '));
        logBuf.append(":ER]");

        log.info(logBuf.toString(), msg);
    }

    public abstract void initWorker();
    public abstract boolean isDisconnect();
    public abstract void runTransaction(Socket s, InputStream in, OutputStream out) throws Exception;
}
