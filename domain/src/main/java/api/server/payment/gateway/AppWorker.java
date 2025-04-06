package api.server.payment.gateway;


import api.server.common.helper.AppUtil;
import api.server.payment.my.MyException;
import api.server.payment.my.MyObjectFIFO;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class AppWorker extends AbstractWorker {
	//버전별 Worker클래스명 정의(obfuscator를 사용하지 않을때만 가능)
    private final String verClasses[] = {"com.eximbay.app.gateway.v100.VerWorker"};

    public AppData appData;

    private Socket s;
	private InputStream in;
	private OutputStream out;

    private boolean bDisconnect = false;

	public AppWorker(int workerPriority, MyObjectFIFO idleWorkers, int workerID, boolean isProg, String serverName){
		super(workerPriority, idleWorkers, workerID, isProg, serverName);
	}

    public AppWorker(){
        super();
    }

    public void initWorker(){

	}

    public boolean isDisconnect(){
		return bDisconnect;
	}

    /* (non-Javadoc)
	 * @see com.mcpay.app.ExtractWorker#runTransaction(java.io.InputStream, java.io.OutputStream)
	 */
	public void runTransaction(Socket s, InputStream in, OutputStream out) throws Exception{
        bDisconnect = false;
        this.s = s;
		this.in = in;
		this.out = out;

        Map<String, String> dataInfo ;
		byte[] sendBytes = null;
		byte[] recvBytes = null;

		//////////////////////////////////////////////////////////////////////////
		// 요청데이터 수신
		//////////////////////////////////////////////////////////////////////////
		try{
			// 제롬 테스트
			recvBytes = recvData(in);

			if(recvBytes == null){
				System.out.println("제롬 Received data is null");
				throw new NullPointerException("Received data is null");
			}
		}catch(Exception e){
			System.out.println("제롬 Exception : " + e);
			throw e;
		}
        String type = new String(recvBytes,  8,  4);     
         if (type.equals("0100") || type.equals("0101") || type.equals("0121") || type.equals("0180") || type.equals("0181") ||type.equals("0182") || type.equals("0184") ) {
            byte[] mark_recvBytes = null;
            if (type.equals("0100") || type.equals("0101") || type.equals("0121") || type.equals("0181") ||type.equals("0182") || type.equals("0184") ) {
                mark_recvBytes = AppUtil.getMarkByte(recvBytes, "******", 115, "000", 134, "**********", 138, "**********", 244, "**********", 269); // 카드번호 MARK 시작 위치
            } else {
                mark_recvBytes = AppUtil.getMarkByte(recvBytes, "******", 115, "000", 134); // 카드번호 MARK 시작 위치
            }
            accessLog(RG, mark_recvBytes);
        //accessLog(RG);
            
        } else {
            accessLog(RG, recvBytes);
        }
        //accessLog(RG, recvBytes);
        
        //accessLog(RG);

		//////////////////////////////////////////////////////////////////////////
		// 헤더전문 분석 및 유효성 체크
		//////////////////////////////////////////////////////////////////////////
        try{
            dataInfo = parseHeader(recvBytes);
        }catch(MyException e){
            throw e;
        }catch(Exception e){
			throw e;
		}

		//최대 쓰레드 수를 초과한 경우, 에러 처리
		if(!m_isProg){
			sendErrRes(new MyException("[AppWorker::runWorker]", MyException.SY00, m_workerID+" is can not be handled.", MyException.sysErrMsg));
			return;
		}/*else{
			Thread.sleep(60 * 1000);

			LogData logData = new LogData();
			try{
	        	//에러 데이터
	    		logData.setErrorBytes(makeLog("Test", "N", "E", "E", "Test".getBytes()));
	    	}catch(Exception e){
	    		printConsole(E, "[AppWorker::writeLog]Make Data Exception:"+e);
	    	}
		}*/

 		//////////////////////////////////////////////////////////////////////////
		// 전문버전별 클래스 로딩
		//////////////////////////////////////////////////////////////////////////
        String ver = (String)dataInfo.get("RH01");
        String className = verClasses[Integer.parseInt(ver)-1000];
        AbstractVer worker = null;
        try{
			Class cls = null;
			cls = Class.forName(className);
            worker = (AbstractVer)cls.newInstance();
            worker.setAppWorker(this);
		}catch(Exception e){
            System.out.println("Exception : " + e);
			sendErrRes(new MyException("[AppWorker::runWorker:createClass]", MyException.SY01, "Exception:"+e, MyException.sysErrMsg));
			return;
		}

        //////////////////////////////////////////////////////////////////////////
		// 전문버전별 전문분석
		//////////////////////////////////////////////////////////////////////////
		dataInfo = appData.parseData(recvBytes);


        //////////////////////////////////////////////////////////////////////////
		// 전문버전별 처리
		//////////////////////////////////////////////////////////////////////////
		worker.procTransaction();


		//////////////////////////////////////////////////////////////////////////
		// 응답데이터 구성
		//////////////////////////////////////////////////////////////////////////
		sendBytes = appData.makeData();


		//////////////////////////////////////////////////////////////////////////
		// 응답데이터 전송
		//////////////////////////////////////////////////////////////////////////
		try{
			//write(out, sendBytes);
             if (type.equals("0100") || type.equals("0101") || type.equals("0121") || type.equals("0180") || type.equals("0181") ||type.equals("0182") || type.equals("0184") ) {
                byte[] mark_sendBytes = null;
                mark_sendBytes = AppUtil.getMarkByte(sendBytes, "******", 119, "**********", 137); // 카드번호 MARK 시작 위치
                accessLog(RG, mark_sendBytes);
            //accessLog(RG);

            } else {
                accessLog(SG, sendBytes);
            }        
            //accessLog(SG, sendBytes);
            //accessLog(SG);
            sendData(out, sendBytes);
		}catch(Exception e){
			throw e;
		}

	}


    private Hashtable parseHeader(byte[] recvBytes) throws MyException{
        Hashtable<String, String> dataInfo = new Hashtable<String, String>();

        try{

            //헤더 파싱
            String field01 = new String(recvBytes,  0,  4); dataInfo.put("RH01", field01);	//전문버전
            String field02 = new String(recvBytes,  4,  4); dataInfo.put("RH02", field02);  //Msg ID
            String field03 = new String(recvBytes,  8,  4); dataInfo.put("RH03", field03);  //Message Type
            String field04 = new String(recvBytes, 12,  4); dataInfo.put("RH04", field04);  //응답코드
            String field05 = new String(recvBytes, 16, 14); dataInfo.put("RH05", field05);  //전송일시

        }catch(Exception e){
            throw new MyException("[AppWorker::parseHeader]", MyException.SY02, "Exception:"+e.toString(), MyException.sysErrMsg);
        }

        return dataInfo;
    }


    //에러전송(에러로그)
    private void sendErrRes(MyException myExp){
    	byte[] sendBytes = null;

    	String code = myExp.getCode();
    	//String msg = myExp.getLoc()+myExp.getMessage();

    	errorLog(myExp.getLoc()+":"+code+":"+myExp.getMessage());
    	try{

            sendBytes = appData.makeErrData(myExp.getCode(), myExp.getDispMsg(), AppUtil.checkNull(myExp.getTraceno()));
        
    	}catch(Exception e){
    		printConsole(ER, "[AppWorker::sendErrRes]Make Data Exception:"+e);
    	}

    	try{
    		//write(out, sendBytes);
            accessLog("SE", sendBytes);
            sendData(out, sendBytes);
            bDisconnect = true;     //연결해제
	    }catch(Exception e){
	    	printConsole(ER, "[AppWorker::sendErrRes]Send Data Exception:"+e);
	    }
 	}
}


