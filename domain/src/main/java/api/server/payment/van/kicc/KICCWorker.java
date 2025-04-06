/*
 * KSNETWorker.java
 */

package api.server.payment.van.kicc;

import api.server.common.helper.AppUtil;
import api.server.payment.gateway.AbstractApp;
import api.server.payment.gateway.AbstractWorker;
import api.server.payment.gateway.AppData;
import api.server.payment.gateway.AppWorker;
import api.server.payment.my.MyException;
import api.server.payment.van.AppVAN;
import api.server.payment.van.AppVANClient;
import api.server.payment.van.AppVANManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.*;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */

public class KICCWorker implements AppVAN {
    private Properties csProps;
    private final String VAN_NAME = "KICC";

    private AppWorker appWorker;
    private KICCData vanData;
    private KICCDBProc vanDBProc;

    private String transType;
    private boolean bExcept;        

    /** Creates a new instance of KICCWorker */
    public KICCWorker(AppWorker appWorker) {
        this.appWorker = appWorker;

        vanData = new KICCData();
        vanDBProc = new KICCDBProc(AbstractApp.g_dbcm);
    }
    
    public void procVAN(Map<String, String> dataInfo) throws MyException {
       
           
        transType = AppUtil.checkNull(dataInfo.get("RH03"));
        
         // 시스템점검 확인 /////////////////////////////////////////////////////
        if ( !transType.equals("0102") && !transType.equals("0202") && !transType.equals("0204")  && !transType.equals("0206") ) // 통신하지 않는 것은 제외....
            procCheckSYSTEM();

        AppVANClient vanConn = null;

        Map<String, String> vanReq = new HashMap<>();
        Map<String, String> vanRes = new HashMap<>();

        byte[] vanReqBytes = null;
	    byte[] vanResBytes = null;

        bExcept = false;

        //////////////////////////////////////////////////////////////////////////
        // VAN 가맹점 아이디, 터미널 아이디, 사업자 번호 추출
        //////////////////////////////////////////////////////////////////////////
        if(!transType.equals("0200") && !transType.equals("0202") && !transType.equals("0204") && !transType.equals("0206") && !transType.equals("0125")){ //2013.06.11
            // 가맹점 정보 조회
            // vanDBProc.selectMerchant(dataInfo);

            // Edgar 2024.10.23 추가 (에러로그에 warnnig정보 기록)
            if(dataInfo.get("VD_TMP_WARN") != null) {
                appWorker.errorLog("[WARN]" + dataInfo.get("VD_TMP_WARN"));
            }

        }

        //////////////////////////////////////////////////////////////////////////
        // 요청데이터 셋팅 및 저장
        //////////////////////////////////////////////////////////////////////////
        try{
            if(transType.equals("0100") || transType.equals("0102") || transType.equals("0122") || transType.equals("0120") || transType.equals("0140") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184") 
                /*|| transType.equals("0400")*/){   //환급거래 주문번호 확인 안함 20130311
                //동일 주문번호 확인, BIN 데이터 확인(해외카드 전용)
                // vanDBProc.selectTransact(dataInfo);
                //하나카드 요청정보 수집
                // vanDBProc.selectRiskInfo(dataInfo);
            }else if(transType.equals("0200") || transType.equals("0202")){
                //원거래 데이터 추출
                vanReq.put("VS_FIELD05", getCurrency(AppUtil.checkNull(dataInfo.get("RB03"))));
                vanDBProc.selectOrgTransact(vanReq, dataInfo);
            }else if(transType.equals("0204") || transType.equals("0206") ) {
                vanReq.put("VS_FIELD05", getCurrency(AppUtil.checkNull(dataInfo.get("RB03"))));
                vanDBProc.selectOrgPartTransact(vanReq, dataInfo);
            }else if(transType.equals("0125")){
                //승인일 포함 20일 이내 거래인지 확인(거래구분 및 매입상태 확인) --> 정상인 경우 매입요청으로 변경
                vanDBProc.checkAuthTransact(dataInfo);
                dataInfo.put("VD_RESCODE", "0000");
                dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(dataInfo.get("VR_FIELD13")).trim());            //승인번호
                dataInfo.put("VD_TRADEDT",  dataInfo.get("VD_DBTRADEDT"));
                //dataInfo.put("VD_TRADEDT", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));                    //거래일자(한국시간 기준)
                return;
            }
            //데이터 포맷팅
            formatData(vanReq, dataInfo);

            //요청 데이터 저장
            // vanDBProc.insertTransact(vanReq, dataInfo);
           
            dataInfo.put("VD_SEQNO", AppUtil.checkNull(vanReq.get("VD_SEQNO")));
        }catch(MyException e){
            throw e;
        }
       //취소거래는 매입전 취소 또는 MCP 거래(cardtraceno 필요함)만 통신 --> 매입후 취소로 반송 건이 많아 관리 어려움.
        if(transType.equals("0100") || transType.equals("0120") || transType.equals("0160") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184") 
                || (transType.equals("0200") && AppUtil.checkNull(dataInfo.get("VD_REALCANCELYN")).equals("Y"))){
            //////////////////////////////////////////////////////////////////////////
            // 요청데이터 구성
            //////////////////////////////////////////////////////////////////////////
            try{
                vanReqBytes = vanData.makeData(Integer.parseInt((String)dataInfo.get("RH00")), vanReq);
            }catch(MyException e){
                throw e;
            }
            
                      
                //////////////////////////////////////////////////////////////////////////
                // VAN 호스트 연결
                //////////////////////////////////////////////////////////////////////////
                long startTime = System.currentTimeMillis();
                try{

                    //vanConn = AbstractApp.g_van.getConnection(VAN_NAME, appWorker);
                    vanConn = new AppVANManager().getConnection(VAN_NAME, appWorker);
                    appWorker.accessLog(AbstractWorker.CV, (VAN_NAME+"|"+vanConn.getID()).getBytes());
               }catch(MyException e){
                    throw e;
                }catch(Exception e){
                    bExcept = true;
                    throw new MyException("[KICCWorker::procVAN:Connect]", MyException.SY20, "Exception:"+e, MyException.sysErrMsg);
                }finally{
                    if(bExcept){
                        AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
                        appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
                    }
                }

                //////////////////////////////////////////////////////////////////////////
                // 요청데이터 전송
                //////////////////////////////////////////////////////////////////////////
                try{
                    byte[] mark_vanReqBytes = null;
                    
                    // Edgar 2024.10.16 cvc 4자리(Amex) 값 마스킹 처리위한 수정 
                    //mark_vanReqBytes = AppUtil.getMarkByte(vanReqBytes, "******", 138, "000", 405); // 카드번호 MARK 시작 위치
                    //mark_vanReqBytes = vanReqBytes; //Edgar 로그 확인위한 임시 
                    if (AppUtil.checkNull(dataInfo.get("RB11")).trim().length() > 3) {  // cvc 4자리 
                    	mark_vanReqBytes = AppUtil.getMarkByte(vanReqBytes, "******", 138, "****", 415); // 카드번호 MARK 시작 위치
                    }else {
                    	mark_vanReqBytes = AppUtil.getMarkByte(vanReqBytes, "******", 138, "***", 405); // 카드번호 MARK 시작 위치
                    }
                                        
                    appWorker.accessLog(AbstractWorker.SV, mark_vanReqBytes);
                    //appWorker.accessLog(AbstractWorker.SV);
                    vanConn.sendData(vanReqBytes);
                }catch(SocketException e){
                    bExcept = true;
                    throw new MyException("[KICCWorker::procVAN:Send]", MyException.SY22, "SocketException:"+e, MyException.sysErrMsg);
                }catch(Exception e){
                    bExcept = true;
                    throw new MyException("[KICCWorker::procVAN:Send]", MyException.SY22, "Exception:"+e, MyException.sysErrMsg);
                }finally{
                    if(bExcept){
                        AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
                        appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
                    }
                }

                //////////////////////////////////////////////////////////////////////////
                // 응답데이터 수신
                //////////////////////////////////////////////////////////////////////////
                try{
                    vanResBytes = vanConn.recvData(AppVANClient.TYPE_LEN);
                    byte[] mark_vanResBytes = null;
                    mark_vanResBytes = AppUtil.getMarkByte(vanResBytes, "******", 158, "", 0);  // 카드번호 MARK 시작 위치
                    appWorker.accessLog(AbstractWorker.RV, mark_vanResBytes);
                    appWorker.accessLog(AbstractWorker.LG, (transType+ " Process Time : "+(System.currentTimeMillis()-startTime) + "ms").getBytes());
                }catch(SocketTimeoutException e){
                    bExcept = true;
                    throw new MyException("[KICCWorker::procVAN:Receive]", MyException.SY23, "SocketTimeoutException:"+e, MyException.sysErrMsg);
                }catch(SocketException e){
                    bExcept = true;
                    throw new MyException("[KICCWorker::procVAN:Receive]", MyException.SY23, "SocketException:"+e, MyException.sysErrMsg);
                }catch(Exception e){
                    bExcept = true;
                    throw new MyException("[KICCWorker::procVAN:Receive]", MyException.SY23, "Exception:"+e, MyException.sysErrMsg);
                }finally{
                    AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
                    appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
                }

                //////////////////////////////////////////////////////////////////////////
                // 응답데이터 분석
                //////////////////////////////////////////////////////////////////////////
                try{
                    vanRes = vanData.parseData(vanResBytes, AppUtil.checkNull(vanReq.get("VS_FIELD01")));
                    
                    // VAN 응답 RFIELD12 가 "1" 이면 Downgrade 승인 이므로 망상취소를 위한 구분값으로 Header RH02 에 "0420" 코드를 셋팅한다.
                   if ( "1".equals(AppUtil.checkNull(vanRes.get("VR_FIELD12")).trim()) && AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O") ) {
                       dataInfo.put("RH02", "0420") ;
                   }        
                }catch(MyException e){
                    throw e;
                }
         //   }
        } else if( transType.equals("0102") || transType.equals("0122") ) {
            if (AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("01") || AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("99"))
                vanRes.put("VR_FIELD02", "BCUPOP");
            else if (dataInfo.get("RB72").trim().equals("03"))
                vanRes.put("VR_FIELD02", "HanaUPOP");
            else if (dataInfo.get("RB72").trim().equals("09"))
                vanRes.put("VR_FIELD02", "LotteUPOP");
            else if (dataInfo.get("RB72").trim().equals("04"))
                vanRes.put("VR_FIELD02", "SSUPOP");
            else if (dataInfo.get("RB72").trim().equals("05"))
                vanRes.put("VR_FIELD02", "ShinHanUPOP");
            
            vanRes.put("VR_FIELD03", "O");
            vanRes.put("VR_FIELD04", dataInfo.get("RB70").substring(2, 14));         // 거래일시
            vanRes.put("VR_FIELD09", "");
            vanRes.put("VR_FIELD10", "OK:" + dataInfo.get("RB71").trim());
            if (AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("01") || AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("99"))
                vanRes.put("VR_FIELD11", "BCUPOP");
            else if (dataInfo.get("RB72").trim().equals("03"))
                vanRes.put("VR_FIELD11", "HanaUPOP");
            else if (dataInfo.get("RB72").trim().equals("09"))
                vanRes.put("VR_FIELD11", "LotteUPOP");
            else if (dataInfo.get("RB72").trim().equals("04"))
                vanRes.put("VR_FIELD11", "SSUPOP");
            else if (dataInfo.get("RB72").trim().equals("05"))
                vanRes.put("VR_FIELD11", "ShinHanUPOP");
            
            vanRes.put("VR_FIELD12", "");
            vanRes.put("VR_FIELD13", dataInfo.get("RB71").trim());                   // 승인번호
            if (dataInfo.get("RB72").trim().equals("01")  || AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("99"))
                vanRes.put("VR_FIELD14", "BCUPOP");
            else if (dataInfo.get("RB72").trim().equals("03"))
                vanRes.put("VR_FIELD14", "HanaUPOP");
            else if (dataInfo.get("RB72").trim().equals("09"))
                vanRes.put("VR_FIELD14", "LotteUPOP");
            else if (dataInfo.get("RB72").trim().equals("04"))
                vanRes.put("VR_FIELD14", "SSUPOP");
            else if (dataInfo.get("RB72").trim().equals("05"))
                vanRes.put("VR_FIELD14", "ShinHanUPOP");
            
            vanRes.put("VR_FIELD15", "25");                                            // 발급사코드
            vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("RB72")).trim());  // 매입사코드
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VD_CARDMID")));   // 가맹점 번호
            // 삼성UPOP의 경우 매거래(승인,취소) 마다 새로운 TRACENO로 생성되므로 수신받은 TraceNO로 셋팅해야 한다.
            vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("RB73")));
        } //else if( (transType.equals("0200") && !AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")) ||
          //         (transType.equals("0200") &&  AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")  && !AppUtil.checkNull(dataInfo.get("VD_TODAYYN")).equals("Y"))  ) { 
          else if( transType.equals("0200") && !AppUtil.checkNull(dataInfo.get("VD_REALCANCELYN")).equals("Y")) { // 매입후 취소거래.. 은련제외..
            //////////////////////////////////////////////////////////////////////////
            // 매입 후 취소 (정상취소로 응답)
            //////////////////////////////////////////////////////////////////////////
            
            vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
            vanRes.put("VR_FIELD03", AppUtil.checkNull(dataInfo.get("VR_FIELD03")));
            vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
            vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
            vanRes.put("VR_FIELD10", "EDI REFUND");
            vanRes.put("VR_FIELD11", AppUtil.checkNull(dataInfo.get("VR_FIELD11")));
            vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
            vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
            vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
            vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
            vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
        } else if( transType.equals("0202")){
            vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
            vanRes.put("VR_FIELD03", AppUtil.checkNull(dataInfo.get("VR_FIELD03")));
            vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
            vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
            vanRes.put("VR_FIELD10", "EDI REFUND");
            vanRes.put("VR_FIELD11", AppUtil.checkNull(dataInfo.get("VR_FIELD11")));
            vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
            vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
            vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
            vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
            vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
            // 삼성UPOP의 경우 매거래(승인,취소) 마다 새로운 TRACENO로 생성되므로 수신받은 TraceNO로 셋팅해야 한다.
            vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("RB73")));
        } else if (transType.equals("0204")) { 
            
            //////////////////////////////////////////////////////////////////////////
            // 부분취소는 매입후 취소 처리한다.
            //////////////////////////////////////////////////////////////////////////
 
            vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
            vanRes.put("VR_FIELD03", "O");
            vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
            vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
            vanRes.put("VR_FIELD10", "EDI REFUND");
            vanRes.put("VR_FIELD11", "부분 취소");
            vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
            vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
            vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
            vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
            vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
            vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("VR_FIELD31")));
        } else if (transType.equals("0206")) { 
            
            //////////////////////////////////////////////////////////////////////////
            // 부분취소는 매입후 취소 처리한다.
            //////////////////////////////////////////////////////////////////////////
 
            vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
            vanRes.put("VR_FIELD03", "O");
            vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
            vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
            vanRes.put("VR_FIELD10", "EDI REFUND");
            vanRes.put("VR_FIELD11", "부분 취소");
            vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
            vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
            vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
            vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
            vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
            // 삼성UPOP의 경우 매거래(승인,취소) 마다 새로운 TRACENO로 생성되므로 수신받은 TraceNO로 셋팅해야 한다.
            vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("RB73")));
        } else if(transType.equals("0400")){
            
            //////////////////////////////////////////////////////////////////////////
            // 취소완료 거래로 업데이트
            //////////////////////////////////////////////////////////////////////////
            
            vanRes.put("VR_FIELD02", "");
            vanRes.put("VR_FIELD03", "O");
            vanRes.put("VR_FIELD04", AppUtil.getDateTime().substring(2, 14));
            vanRes.put("VR_FIELD09", "");
            vanRes.put("VR_FIELD10", "EDI REFUND");
            vanRes.put("VR_FIELD11", "환급 거래");
            vanRes.put("VR_FIELD12", "");
            vanRes.put("VR_FIELD13", "");
            vanRes.put("VR_FIELD14", "");
            vanRes.put("VR_FIELD15", "25");
            vanRes.put("VR_FIELD16", "03");
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VD_CARDMID")));
        }

        //////////////////////////////////////////////////////////////////////////
        // 응답데이터 저장
        //////////////////////////////////////////////////////////////////////////
        try{
            //승인취소 실패 시에 field16, 17을 원거래로 셋팅        	
        	//승인취소 실패 시 매입 취소로 처리 되도록 요청(카카오모빌리티 요청) KICC만 일단 작업? 2023.12.14 walter
            //승인 취소 거절시 매입취소 진행 로직에서 직가맹 거래의 경우 제외. walter 2024.01.11
            if(transType.equals("0200") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O") && !AppUtil.checkNull(dataInfo.get("ORG_STATUS")).equals("T003")){            	
        	//if(transType.equals("0200") && "99110241".equals(AppUtil.checkNull(dataInfo.get("RB51")).trim()) && !AppUtil.checkNull(dataInfo.get("ORG_STATUS")).equals("T003")){
            	appWorker.errorLog(AbstractWorker.ER+":"+AppUtil.checkNull(dataInfo.get("RB02"))+":"+AppUtil.checkNull(vanRes.get("VR_FIELD13"))+":"+AppUtil.checkNull(vanRes.get("VR_FIELD09")));
            	//승인취소 실패 시 매입 취소로 처리 되도록 요청(카카오모빌리티 요청) KICC만 일단 작업? 기존꺼 주석 2023.12.14 walter
            	//매입 취소 데이터 셋팅 하고 VD_REALCANCELYN N으로 재셋팅  2023.12.14 walter 
                //vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                //vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));  	
                vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
                vanRes.put("VR_FIELD03", AppUtil.checkNull(dataInfo.get("VR_FIELD03")));
                vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
                vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
                vanRes.put("VR_FIELD10", "EDI REFUND");
                vanRes.put("VR_FIELD11", AppUtil.checkNull(dataInfo.get("VR_FIELD11")));
                vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
                vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
                vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
                vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
                dataInfo.put("VD_REALCANCELYN", "N");                
            }

            //응답 데이터 저장 (승인취소 거래는 mcpstatus유지되도록 함)
            vanDBProc.updateTransact(vanRes, dataInfo);

            if(transType.equals("0200") || transType.equals("0202")){
                // 매입전이면서 밴사취소를 해야하고(승인상태)탔고 당일 거래시(CUP) 원거래 취소거래로 변경
                // CUP거래 이외의 거래는 승인 상태 취소시 모두 VD_ORGCANCELYN이 "Y" 이다.
                if( AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00") && AppUtil.checkNull(dataInfo.get("VD_REALCANCELYN")).equals("Y") ){
                    
                    if(AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){
                        //////////////////////////////////////////////////////////////////////////
                        // 매입 전 취소로 원거래를 "취소된 거래"로 업데이트
                        //////////////////////////////////////////////////////////////////////////
                        vanDBProc.updateOrgTransact(dataInfo);

                    }
                }else{
                    
                    //////////////////////////////////////////////////////////////////////////
                    // 매입 후 취소로 취소거래를 "매입취소"대상으로 업데이트
                    //////////////////////////////////////////////////////////////////////////
                    vanDBProc.updateEDICancel(dataInfo);
                   
                    //////////////////////////////////////////////////////////////////////////
                    // 정상거래로 처리
                    //////////////////////////////////////////////////////////////////////////
                    if(!AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O"))
                        vanRes.put("VR_FIELD03", "O");
                }
                System.out.println(vanRes.get("VR_FIELD03"));
            } else if ( transType.equals("0204") || transType.equals("0206") ) { 
                /********************************************************************************
                 * 부분 취소는 원거래를 취소 처리 하면 안된다.
                 ********************************************************************************/
                vanDBProc.updateEDIPartCancel(dataInfo);
                
            } else if(transType.equals("0400")){
                /********************************************************************************
                 * 환급요청은 매입취소로 업데이트
                 ********************************************************************************/
                vanDBProc.updateEDIRefund(dataInfo);
            }
        }catch(MyException e){
            throw e;
        }

        //////////////////////////////////////////////////////////////////////////
        // 응답데이터 Copy
        //////////////////////////////////////////////////////////////////////////
        if(AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){    //알파벳 대문자 O
            dataInfo.put("VD_RESCODE", "0000");                     //응답코드
        }else{
            dataInfo.put("VD_RESCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());                     //응답코드
            dataInfo.put("VD_RESMSG", AppUtil.checkNull(vanRes.get("VR_FIELD09")).trim() + " "+ AppUtil.checkNull(vanRes.get("VR_FIELD10")).trim() + " "+ AppUtil.checkNull(vanRes.get("VR_FIELD11")).trim() + " "+ AppUtil.checkNull(vanRes.get("VR_FIELD12")).trim());
        }

        dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());            //승인번호
        dataInfo.put("VD_TRADEDT", AppUtil.checkNull(dataInfo.get("VD_DBTRADEDT")));
        dataInfo.put("VD_TRADEDT_AUTH", "20"+AppUtil.checkNull(vanRes.get("VR_FIELD04")));          //매입사에서 전송한 승인시간
        dataInfo.put("VD_DCCINFO", AppUtil.checkNull(vanRes.get("VR_FIELD30")));                    //DCC 결제정보
        dataInfo.put("VD_FOREIGNCURRENCY", AppUtil.checkNull(vanRes.get("VR_FIELD20")));            //Foreign Amount
        dataInfo.put("VD_EXCHANGERATE", AppUtil.checkNull(vanRes.get("VR_FIELD21")));               //Exchange Rate
        dataInfo.put("VD_INVERTEDRATE", AppUtil.checkNull(vanRes.get("VR_FIELD22")));               //Inverted Rate
        dataInfo.put("VD_CURRENCYCODE", AppUtil.checkNull(vanRes.get("VR_FIELD23")));               //외환 영문코드
        dataInfo.put("VD_MARKUP", AppUtil.checkNull(vanRes.get("VR_FIELD24")));                     //Mark up percent
        dataInfo.put("VD_COMMISSIONP", AppUtil.checkNull(vanRes.get("VR_FIELD25")));                //Commission Percent
        dataInfo.put("VD_COMMISSIONV", AppUtil.checkNull(vanRes.get("VR_FIELD26")));                //Commission Value
        

    }
    
    public void procVANTest(Map<String, String> dataInfo) throws MyException {
System.out.println("[KICCWorker::procVANTest]Start");
//System.out.println("[KICCWorker::dataInfo] : " + dataInfo);  

        
        transType = AppUtil.checkNull(dataInfo.get("RH03"));

         // 시스템점검 확인 /////////////////////////////////////////////////////
        if ( !transType.equals("0102") && !transType.equals("0202") && !transType.equals("0204")  && !transType.equals("0206") ) // 통신하지 않는 것은 제외....
            procCheckSYSTEM();
        
        AppVANClient vanConn = null;

        Map<String, String> vanReq = new HashMap<>();
        Map<String, String> vanRes = new HashMap<>();

        byte[] vanReqBytes = null;
	byte[] vanResBytes = null;

        bExcept = false;

        //////////////////////////////////////////////////////////////////////////
        // VAN 가맹점 아이디, 터미널 아이디, 사업자 번호 추출
        //////////////////////////////////////////////////////////////////////////
          if(!transType.equals("0200") && !transType.equals("0202") && !transType.equals("0204") && !transType.equals("0206") && !transType.equals("0125")){ //2013.06.11
            try{
                vanDBProc.selectMerchant(dataInfo);
                
                // Edgar 2024.10.23 추가 (에러로그에 warnnig정보 기록)
                if(dataInfo.get("VD_TMP_WARN") != null) {
                	appWorker.errorLog("[WARN]" + dataInfo.get("VD_TMP_WARN"));
                }
                
            }catch(MyException e){
                throw e;
            }
        }

        //////////////////////////////////////////////////////////////////////////
		// 요청데이터 셋팅 및 저장
		//////////////////////////////////////////////////////////////////////////
        try{
            if(transType.equals("0100") || transType.equals("0120") || transType.equals("0102") || transType.equals("0122") || transType.equals("0140") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184")){
                //동일 주문번호 확인, BIN 데이터 확인(해외카드 전용)
                vanDBProc.selectTransact(dataInfo);
                //하나카드 요청정보 수집
                vanDBProc.selectRiskInfo(dataInfo);
            }else if(transType.equals("0200") || transType.equals("0202")){
                //원거래 데이터 추출
                vanReq.put("VS_FIELD05", getCurrency(AppUtil.checkNull(dataInfo.get("RB03"))));
                vanDBProc.selectOrgTransact(vanReq, dataInfo);
            }else if(transType.equals("0204") || transType.equals("0206") ) {
                vanReq.put("VS_FIELD05", getCurrency(AppUtil.checkNull(dataInfo.get("RB03"))));
                vanDBProc.selectOrgPartTransact(vanReq, dataInfo);
            }else if(transType.equals("0125")){
                //승인일 포함 20일 이내 거래인지 확인(거래구분 및 매입상태 확인) --> 정상인 경우 매입요청으로 변경
                vanDBProc.checkAuthTransact(dataInfo);
                dataInfo.put("VD_RESCODE", "0000");
                dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(dataInfo.get("VR_FIELD13")).trim());            //승인번호
                dataInfo.put("VD_TRADEDT",  dataInfo.get("VD_DBTRADEDT"));
                //dataInfo.put("VD_TRADEDT", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));               //거래일자(한국시간 기준)
                return;
            }

            //데이터 포맷팅
            formatData(vanReq, dataInfo);

            //요청 데이터 저장
            vanDBProc.insertTransact(vanReq, dataInfo);
            dataInfo.put("VD_SEQNO", AppUtil.checkNull(vanReq.get("VD_SEQNO")));
        }catch(MyException e){
            throw e;
        }

        //////////////////////////////////////////////////////////////////////////
		// 응답데이터 저장
		//////////////////////////////////////////////////////////////////////////
        try{
            //처리시간
            //Thread.sleep(2 * 1000);
            
            if(transType.equals("0100") || transType.equals("0120") || transType.equals("0160") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184") 
               || (transType.equals("0200") && AppUtil.checkNull(dataInfo.get("VD_REALCANCELYN")).equals("Y"))){
                //테스트카드 번호 체크
                String cardNo = AppUtil.checkNull(dataInfo.get("RB09")).trim();
                String expiryDT = AppUtil.checkNull(dataInfo.get("RB10")).trim();
                String cvv = AppUtil.checkNull(dataInfo.get("RB11")).trim();
                if(cardNo.equals("4434310000000001") || cardNo.equals("5466160000000001") || cardNo.equals("4658580000000001") || cardNo.equals("4773760000000001") 
                        || cardNo.equals("5425500000000001") || cardNo.equals("4272290000000001") || cardNo.equals("5520370000000001") || cardNo.equals("5309700000000001")
                        || cardNo.equals("5191230000000001") || cardNo.equals("5131410000000001") || cardNo.equals("4477570000000001") || cardNo.equals("5310111111111111")
                        || cardNo.equals("4111111111111111") || cardNo.equals("4190771111111111") || cardNo.equals("3540931111111111")
                        || cardNo.equals("3529331111111111") || cardNo.equals("5222741111111111") /*2014.10.21 추가*/
                        || cardNo.equals("4028921111111111") || cardNo.equals("5187101111111111") || cardNo.equals("4773761111111111") || cardNo.equals("5452401111111111") 
                        || cardNo.equals("4239531111111111") || cardNo.equals("4214941111111111") || cardNo.equals("4980001111111111") || cardNo.equals("4888931111111111") 
                        || cardNo.equals("5250731111111111") || cardNo.equals("5428531111111111")
                        // 20160801 CyberSource 3D 테스트용카드 등록(김영하 요청)
                        || cardNo.equals("4000000000000002") || cardNo.equals("4000000000000010") || cardNo.equals("4000000000000127") || cardNo.equals("4000000000000036")
                        || cardNo.equals("4000000000000069") || cardNo.equals("4000000000000093") || cardNo.equals("4000000000000051") || cardNo.equals("4000000000000044")
                        || cardNo.equals("4000000000000085") || cardNo.equals("5200000000000007") || cardNo.equals("5200000000000015") || cardNo.equals("5200000000000122")
                        || cardNo.equals("5200000000000031") || cardNo.equals("5200000000000064") || cardNo.equals("5200000000000098") || cardNo.equals("5200000000000056")
                        || cardNo.equals("5200000000000049") || cardNo.equals("5200000000000080") 
                        || cardNo.equals("3569990010083722") || cardNo.equals("3569990010083748") || cardNo.equals("3569960010083758") || cardNo.equals("3541599998103643")
                        || cardNo.equals("3569990110083721") || cardNo.equals("3541599999103865") || cardNo.equals("3541599999103881") || cardNo.equals("3569970010083724")
                        || cardNo.equals("3569980010083723") || cardNo.equals("3541599969103614") || cardNo.equals("4000000000000000") || cardNo.equals("6250947000000014")
                        || cardNo.equals("5200000000001096")
                        // 20160906 KTIS 아멕스카드(박상귀 요청)  
                        || cardNo.equals("377222222211009") 
                        
                        // 20161010 아시아나 김영하 요청
                        || cardNo.equals("4012999999999999") || cardNo.equals("4444333322221111") || cardNo.equals("4028220019082910") 
                        || cardNo.equals("370000000000002")  || cardNo.equals("370745454545486")  || cardNo.equals("371690024301003")
                        
                        // 20180221 3DS 김영하 요청  
                        || cardNo.equals("4000000000000002") || cardNo.equals("4000000000000101") || cardNo.equals("4000000000000127") || cardNo.equals("4000180000000002") || cardNo.equals("4000260000000002") 
                        || cardNo.equals("5200000000000007") || cardNo.equals("5200000000000122") || cardNo.equals("5200000000000106") || cardNo.equals("5200180000000007") || cardNo.equals("5200260000000007")
                        || cardNo.equals("3569990010083722") || cardNo.equals("3569960010083758")  
                        
                        // 20180424 아시아나요청(김영한)
                        || cardNo.equals("5500000000000004") 
                          
                        // 20190214 Cafe24테스트용(김영하요청)
                        || cardNo.equals("4364200000000000")  
                        // 20190228 VND 테스트용 카드 등록(김종민요청)
                        || cardNo.equals("4041521111111111")    
                        // 20200702 DMPLUS 테스트용 카드 등록(김종민요청)
                        || cardNo.equals("5187101111111111") || cardNo.equals("4003581111111111") || cardNo.equals("4231791111111111") 
                        || cardNo.equals("378282246310005")  || cardNo.equals("3566111111111117") || cardNo.equals("5555555555554444") 
                        || cardNo.equals("5466160275885158") || cardNo.equals("5187108302545517") || cardNo.equals("4231798820001776") 
                        || cardNo.equals("5466168554595158") || cardNo.equals("5187109392955517") || cardNo.equals("4231794438111776") 
                        //2020.07.24 DMPLUS 테스트용 카드 추가 등록
                        || cardNo.equals("4129763365384636")
                        //2020.09.07 베트남 eWallete 테스트용 카드 추가 등록
                        || cardNo.equals("9100409900000001")
                        //2020.10.08 디스커버/다이너스 테스트 카등  추가 등록
                        || cardNo.equals("6510000000000174") || cardNo.equals("6510000000000182") || cardNo.equals("6510000000000208") || cardNo.equals("36070500001038")
                        //2021.01.05 Downgrade 테스트 카드 추가 등록
                        || cardNo.equals("4111111111111112")
                        //2021.03.26 3ds 2.0 관련 테스트 카드번호 추가, 2021.11.03 미르카드 테스트 자체 승인 카드 하나 추가.
                        || cardNo.equals("1654310400000000") || cardNo.equals("165430270000463") || cardNo.equals("2204990000000000")
                        //2023.02.16 3ds 2.0 관련 테스트 카드번호 추가(
                        || cardNo.equals("5122969999999996") || cardNo.equals("3562737046057080") || cardNo.equals("3569990012296085")
                        || cardNo.equals("375987000126600") || cardNo.equals("375987000126700") 
                        //2023.03.08 ssg siteforcur 관련 테스트 카드 번호 추가 
                        || cardNo.equals("4073851111111111") || cardNo.equals("4188598111111111") || cardNo.equals("4188320111111111") || cardNo.equals("4172882111111111")
                        // 2024.05.07 NexBe 서버 연동 테스트 카드 번호 추가
                        || cardNo.equals("4012000000001006") || cardNo.equals("4012000000001097")
                        // 2024.05.08 Stripe에서 제공한 VISA, MASTER 테스트 카드번호 추가
                        || cardNo.equals("4000056655665556") || cardNo.equals("5200828282828210")
                        // 2024.12.03 Unionpay 19자리 테스트 추가등록 
                        || cardNo.startsWith("6250947000000014") 
                        )
                {


                    //if(expiryDT.equals("1501") && cvv.equals("467")){
                        //승인 결과를 성공으로 고정(for test)
                        vanRes.put("VR_FIELD03", "O");
                        vanRes.put("VR_FIELD13", String.valueOf(Math.random() * 1000000).substring(0, 6).replaceAll("\\.", "1"));
                        if (cardNo.substring(0,1).equals("4")) {
                            vanRes.put("VR_FIELD14", "해외비자카드    ");
                        } else if (cardNo.substring(0,1).equals("5")) {
                            vanRes.put("VR_FIELD14", "해외마스터카드    ");
                        } else if (cardNo.substring(0,1).equals("3")) {
                            if (cardNo.substring(0,2).equals("37")) {
                                vanRes.put("VR_FIELD14", "아멕스카드  "); 
                            } else {
                                vanRes.put("VR_FIELD14", "JCB카드    "); 
                            }    
                        } else if (cardNo.substring(0,1).equals("6")) {
                            vanRes.put("VR_FIELD14", "BCUPOP    "); 
                        } else if (cardNo.substring(0,1).equals("9")) {
                            vanRes.put("VR_FIELD14", "eWallete  "); 
                        } else {
                            vanRes.put("VR_FIELD14", "해외비자카드    ");
                        }
                        vanRes.put("VR_FIELD15", "25");
                        if (cardNo.substring(0,1).equals("6")) {
                            vanRes.put("VR_FIELD16", "01");
                        } else {
                            vanRes.put("VR_FIELD16", "03");
                        }
                        vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VD_MID")));
                        
                        
                        // Downgrade 카드일경우...
                        if (cardNo.equals("4111111111111112")) {
                             vanRes.put("VR_FIELD12", "1");
                             dataInfo.put("RH02", "0420") ; 
                        }
                    /*}else{
                        vanRes.put("VR_FIELD03", "X");
                        vanRes.put("VR_FIELD09", "Decline");
                        vanRes.put("VR_FIELD11", "expiry dt:01/15");
                        vanRes.put("VR_FIELD12", "cvv:467");
                    }  */              
                }else{
                    vanRes.put("VR_FIELD03", "X");
                    vanRes.put("VR_FIELD09", "Decline");
                    vanRes.put("VR_FIELD11", "Card number");
                    vanRes.put("VR_FIELD13", "8037");
                }
            } else if( transType.equals("0102") || transType.equals("0122") ) {
                if (AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("01") || AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("99"))
                    vanRes.put("VR_FIELD02", "BCUPOP");
                else if (dataInfo.get("RB72").trim().equals("03"))
                    vanRes.put("VR_FIELD02", "HanaUPOP");
                else if (dataInfo.get("RB72").trim().equals("09"))
                    vanRes.put("VR_FIELD02", "LotteUPOP");
                else if (dataInfo.get("RB72").trim().equals("04"))
                    vanRes.put("VR_FIELD02", "SSUPOP");
                else if (dataInfo.get("RB72").trim().equals("05"))
                vanRes.put("VR_FIELD02", "ShinHanUPOP");
                
            
                vanRes.put("VR_FIELD03", "O");
                vanRes.put("VR_FIELD04", dataInfo.get("RB70").substring(2, 14));         // 거래일시
                vanRes.put("VR_FIELD09", "");
                vanRes.put("VR_FIELD10", "OK:" + dataInfo.get("RB71").trim());
                if (AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("01") || AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("99"))
                    vanRes.put("VR_FIELD11", "BCUPOP");
                else if (dataInfo.get("RB72").trim().equals("03"))
                    vanRes.put("VR_FIELD11", "HanaUPOP");
                else if (dataInfo.get("RB72").trim().equals("09"))
                    vanRes.put("VR_FIELD11", "LotteUPOP");
                else if (dataInfo.get("RB72").trim().equals("04"))
                    vanRes.put("VR_FIELD11", "SSUPOP");
                else if (dataInfo.get("RB72").trim().equals("05"))
                vanRes.put("VR_FIELD11", "ShinHanUPOP");
            
                vanRes.put("VR_FIELD12", "");
                vanRes.put("VR_FIELD13", dataInfo.get("RB71").trim());                   // 승인번호
                if (dataInfo.get("RB72").trim().equals("01")  || AppUtil.checkNull(dataInfo.get("RB72")).trim().equals("99"))
                    vanRes.put("VR_FIELD14", "BCUPOP");
                else if (dataInfo.get("RB72").trim().equals("03"))
                    vanRes.put("VR_FIELD14", "HanaUPOP");
                else if (dataInfo.get("RB72").trim().equals("09"))
                    vanRes.put("VR_FIELD14", "LotteUPOP");
                else if (dataInfo.get("RB72").trim().equals("04"))
                    vanRes.put("VR_FIELD14", "SSUPOP");
                else if (dataInfo.get("RB72").trim().equals("05"))
                vanRes.put("VR_FIELD14", "ShinHanUPOP");

                vanRes.put("VR_FIELD15", "25");                                            // 발급사코드
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("RB72")).trim());  // 매입사코드
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VD_CARDMID")));   // 가맹점 번호
                // 삼성UPOP의 경우 매거래(승인,취소) 마다 새로운 TRACENO로 생성되므로 수신받은 TraceNO로 셋팅해야 한다.
                vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("RB73")));
            } //else if( (transType.equals("0200") && !AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")) ||
          //         (transType.equals("0200") &&  AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")  && !AppUtil.checkNull(dataInfo.get("VD_TODAYYN")).equals("Y"))  ) { 
                else if( transType.equals("0200") && !AppUtil.checkNull(dataInfo.get("VD_REALCANCELYN")).equals("Y")) { // 매입후 취소거래.. 은련제외..
            //////////////////////////////////////////////////////////////////////////
            // 매입 후 취소 (정상취소로 응답)
            //////////////////////////////////////////////////////////////////////////
            
                vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
                vanRes.put("VR_FIELD03", AppUtil.checkNull(dataInfo.get("VR_FIELD03")));
                vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
                vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
                vanRes.put("VR_FIELD10", "EDI REFUND");
                vanRes.put("VR_FIELD11", AppUtil.checkNull(dataInfo.get("VR_FIELD11")));
                vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
                vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
                vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
                vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
            } else if( transType.equals("0202")){
                vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
                vanRes.put("VR_FIELD03", AppUtil.checkNull(dataInfo.get("VR_FIELD03")));
                vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
                vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
                vanRes.put("VR_FIELD10", "EDI REFUND");
                vanRes.put("VR_FIELD11", AppUtil.checkNull(dataInfo.get("VR_FIELD11")));
                vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
                vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
                vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
                vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
                // 삼성UPOP의 경우 매거래(승인,취소) 마다 새로운 TRACENO로 생성되므로 수신받은 TraceNO로 셋팅해야 한다.
                vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("RB73")));
            } else if (transType.equals("0204")) { 
            
                //////////////////////////////////////////////////////////////////////////
                // 부분취소는 매입후 취소 처리한다.
                //////////////////////////////////////////////////////////////////////////

                vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
                vanRes.put("VR_FIELD03", "O");
                vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
                vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
                vanRes.put("VR_FIELD10", "EDI REFUND");
                vanRes.put("VR_FIELD11", "부분 취소");
                vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
                vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
                vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
                vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
                vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("VR_FIELD31")));
            } else if (transType.equals("0206")) { 
            
                //////////////////////////////////////////////////////////////////////////
                // 부분취소는 매입후 취소 처리한다.
                //////////////////////////////////////////////////////////////////////////

                vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
                vanRes.put("VR_FIELD03", "O");
                vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
                vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
                vanRes.put("VR_FIELD10", "EDI REFUND");
                vanRes.put("VR_FIELD11", "부분 취소");
                vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
                vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
                vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
                vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
                // 삼성UPOP의 경우 매거래(승인,취소) 마다 새로운 TRACENO로 생성되므로 수신받은 TraceNO로 셋팅해야 한다.
                vanRes.put("VR_FIELD31", AppUtil.checkNull(dataInfo.get("RB73")));
            } else if(transType.equals("0400")){
            
                //////////////////////////////////////////////////////////////////////////
                // 취소완료 거래로 업데이트
                //////////////////////////////////////////////////////////////////////////

                vanRes.put("VR_FIELD02", "");
                vanRes.put("VR_FIELD03", "O");
                vanRes.put("VR_FIELD04", AppUtil.getDateTime().substring(2, 14));
                vanRes.put("VR_FIELD09", "");
                vanRes.put("VR_FIELD10", "EDI REFUND");
                vanRes.put("VR_FIELD11", "환급 거래");
                vanRes.put("VR_FIELD12", "");
                vanRes.put("VR_FIELD13", "");
                vanRes.put("VR_FIELD14", "");
                vanRes.put("VR_FIELD15", "25");
                vanRes.put("VR_FIELD16", "03");
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VD_CARDMID")));
           } else{
                    //취소 결과를 성공으로 고정(for test)
                    vanRes.put("VR_FIELD03", "O");
                    vanRes.put("VR_FIELD13", String.valueOf(Math.random() * 1000000).substring(0, 6));
           }
            vanRes.put("VR_FIELD04", AppUtil.getDateTime().substring(2, 14));
            
            
System.out.println("[SMARTROWorker::procVANTest]승인 결과 고정 ==> " + AppUtil.checkNull(vanRes.get("VR_FIELD03")) + "," + AppUtil.checkNull(vanRes.get("VR_FIELD13")));                
            
            //승인취소 실패 시에 field16, 17을 원거래로 셋팅
        	//승인취소 실패 시 매입 취소로 처리 되도록 요청(카카오모빌리티 요청) KICC만 일단 작업? 2023.12.14 walter
            if(transType.equals("0200") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){            	
        	//if(transType.equals("0200") && "263616".equals(AppUtil.checkNull(dataInfo.get("RB51")).trim())){
            	appWorker.errorLog(AbstractWorker.ER+":"+AppUtil.checkNull(dataInfo.get("RB02"))+":"+AppUtil.checkNull(vanRes.get("VR_FIELD13"))+":"+AppUtil.checkNull(vanRes.get("VR_FIELD09")));
            	//승인취소 실패 시 매입 취소로 처리 되도록 요청(카카오모빌리티 요청) KICC만 일단 작업? 기존꺼 주석 2023.12.14 walter
            	//매입 취소 데이터 셋팅 하고 VD_REALCANCELYN N으로 재셋팅  2023.12.14 walter 
                //vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                //vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));  	
                vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
                vanRes.put("VR_FIELD03", AppUtil.checkNull(dataInfo.get("VR_FIELD03")));
                vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
                vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
                vanRes.put("VR_FIELD10", "EDI REFUND");
                vanRes.put("VR_FIELD11", AppUtil.checkNull(dataInfo.get("VR_FIELD11")));
                vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
                vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
                vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
                vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
                dataInfo.put("VD_REALCANCELYN", "N");                
            }
            
            
            
            //응답 데이터 저장 (승인취소 거래는 mcpstatus유지되도록 함)
            vanDBProc.updateTransact(vanRes, dataInfo);

            if(transType.equals("0200") || transType.equals("0202")){
                // 매입전이면서 밴사취소를 해야하고(승인상태)탔고 당일 거래시(CUP) 원거래 취소거래로 변경
                // CUP거래 이외의 거래는 승인 상태 취소시 모두 VD_ORGCANCELYN이 "Y" 이다.
                if( AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00") && AppUtil.checkNull(dataInfo.get("VD_REALCANCELYN")).equals("Y") ){
                    
                    if(AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){
                        //////////////////////////////////////////////////////////////////////////
                        // 매입 전 취소로 원거래를 "취소된 거래"로 업데이트
                        //////////////////////////////////////////////////////////////////////////
                        vanDBProc.updateOrgTransact(dataInfo);

                    }
                }else{
                    
                    //////////////////////////////////////////////////////////////////////////
                    // 매입 후 취소로 취소거래를 "매입취소"대상으로 업데이트
                    //////////////////////////////////////////////////////////////////////////
                    vanDBProc.updateEDICancel(dataInfo);
                   
                    //////////////////////////////////////////////////////////////////////////
                    // 정상거래로 처리
                    //////////////////////////////////////////////////////////////////////////
                    if(!AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O"))
                        vanRes.put("VR_FIELD03", "O");
                }
 System.out.println(vanRes.get("VR_FIELD03"));
            } else if ( transType.equals("0204") || transType.equals("0206") ) { 
                /********************************************************************************
                 * 부분 취소는 원거래를 취소 처리 하면 안된다.
                 ********************************************************************************/
                vanDBProc.updateEDIPartCancel(dataInfo);
                
            } else if(transType.equals("0400")){
                /********************************************************************************
                 * 환급요청은 매입취소로 업데이트
                 ********************************************************************************/
                vanDBProc.updateEDIRefund(dataInfo);
            }
        }catch(MyException e){
            throw e;
        }catch(Exception e){}

        //////////////////////////////////////////////////////////////////////////
        // 응답데이터 Copy
        //////////////////////////////////////////////////////////////////////////
        if(AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){    //알파벳 대문자 O
            dataInfo.put("VD_RESCODE", "0000");                     //응답코드
        }else{
            dataInfo.put("VD_RESCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());                     //응답코드
            dataInfo.put("VD_RESMSG", AppUtil.checkNull(vanRes.get("VR_FIELD09")).trim() + " "+ AppUtil.checkNull(vanRes.get("VR_FIELD10")).trim() + " "+ AppUtil.checkNull(vanRes.get("VR_FIELD11")).trim() + " "+ AppUtil.checkNull(vanRes.get("VR_FIELD12")).trim());
        }

        dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());            //승인번호
        dataInfo.put("VD_TRADEDT", AppUtil.checkNull(dataInfo.get("VD_DBTRADEDT")));
        dataInfo.put("VD_TRADEDT_AUTH", AppUtil.checkNull(dataInfo.get("VD_DBTRADEDT")));          //매입사에서 전송한 승인시간
        dataInfo.put("VD_DCCINFO", AppUtil.checkNull(vanRes.get("VR_FIELD30")));                    //DCC 결제정보
        dataInfo.put("VD_FOREIGNCURRENCY", AppUtil.checkNull(vanRes.get("VR_FIELD20")));            //Foreign Amount
        dataInfo.put("VD_EXCHANGERATE", AppUtil.checkNull(vanRes.get("VR_FIELD21")));               //Exchange Rate
        dataInfo.put("VD_INVERTEDRATE", AppUtil.checkNull(vanRes.get("VR_FIELD22")));               //Inverted Rate
        dataInfo.put("VD_CURRENCYCODE", AppUtil.checkNull(vanRes.get("VR_FIELD23")));               //외환 영문코드
        dataInfo.put("VD_MARKUP", AppUtil.checkNull(vanRes.get("VR_FIELD24")));                     //Mark up percent
        dataInfo.put("VD_COMMISSIONP", AppUtil.checkNull(vanRes.get("VR_FIELD25")));                //Commission Percent
        dataInfo.put("VD_COMMISSIONV", AppUtil.checkNull(vanRes.get("VR_FIELD26")));                //Commission Value

    }
    
    
    
    
   /**
    * Edgar 2024.03.15 부하테스트용 함수 
    */
    @Override
	public void procVANStressTest(Map<String, String> dataInfo) throws MyException {
        transType = AppUtil.checkNull(dataInfo.get("RH03"));
        
        // 시스템점검 확인 /////////////////////////////////////////////////////
        procCheckSYSTEM();

       AppVANClient vanConn = null;

       Map<String, String> vanReq = new HashMap<>();
       Map<String, String> vanRes = new HashMap<>();

       byte[] vanReqBytes = null;
       byte[] vanResBytes = null;

       bExcept = false;

       //////////////////////////////////////////////////////////////////////////
       // VAN 가맹점 아이디, 터미널 아이디, 사업자 번호 추출
       //////////////////////////////////////////////////////////////////////////
       try{
    	   vanDBProc.selectMerchant(dataInfo);
       }catch(MyException e){
    	   // throw e;   예외 무시 
    	   //appWorker.accessLog(AbstractWorker.LG, ("ERROR: "+e.getMessage()).getBytes());
    	   if(e.getMessage().contains("NullPointerException")) {
    		   throw e;
    	   }
       }

    //////////////////////////////////////////////////////////////////////////
	// 요청데이터 셋팅 및 저장
	//////////////////////////////////////////////////////////////////////////
       try{
           //동일 주문번호 확인, BIN 데이터 확인(해외카드 전용)
           vanDBProc.selectTransact(dataInfo);
       
       }catch(Exception e) {
    	   //appWorker.accessLog(AbstractWorker.LG, ("ERROR: "+e.getMessage()).getBytes());
    	   if(e.getMessage().contains("NullPointerException")) {
    		   throw e;
    	   }
       } 
       
       try {
           //하나카드 요청정보 수집
           vanDBProc.selectRiskInfo(dataInfo);
       }catch(Exception e) {
    	   //appWorker.accessLog(AbstractWorker.LG, ("ERROR: "+e.getMessage()).getBytes());
    	   if(e.getMessage().contains("NullPointerException")) {
    		   throw e;
    	   }
       }
           
       try { 
           //데이터 포맷팅
           formatData(vanReq, dataInfo);
       }catch(Exception e) {}

       try {
           //요청 데이터 저장
           vanDBProc.insertTransact(vanReq, dataInfo);
           dataInfo.put("VD_SEQNO", AppUtil.checkNull(vanReq.get("VD_SEQNO")));
       }catch(MyException e){
    	   //e.printStackTrace();
           //throw e; 예외 무시 
    	   //appWorker.accessLog(AbstractWorker.LG, ("ERROR: "+e.getMessage()).getBytes());
    	   if(e.getMessage().contains("NullPointerException")) {
    		   throw e;
    	   }
       }

      //////////////////////////////////////////////////////////////////////////
      // 요청데이터 구성
      //////////////////////////////////////////////////////////////////////////
       try{
           vanReqBytes = vanData.makeData(Integer.parseInt((String)dataInfo.get("RH00")), vanReq);
       }catch(MyException e){
           throw e;
       }
           
                     
      //////////////////////////////////////////////////////////////////////////
      // VAN 호스트 연결
      //////////////////////////////////////////////////////////////////////////
       long startTime = System.currentTimeMillis();
       try{
    	   /*
           vanConn = AbstractApp.g_van.getConnection(VAN_NAME, appWorker);
           appWorker.accessLog(AbstractWorker.CV, (VAN_NAME+"|"+vanConn.getID()).getBytes());
           */

       }catch(Exception e){
           bExcept = true;
           throw new MyException("[KICCWorker::procVANStressTest:Connect]", MyException.SY20, "Exception:"+e, MyException.sysErrMsg);
       }finally{
    	   /*
           if(bExcept){
               AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
               appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
           }
           */
       }

       //////////////////////////////////////////////////////////////////////////
       // 요청데이터 전송
       //////////////////////////////////////////////////////////////////////////
       try{
    	   /*
           byte[] mark_vanReqBytes = null;
           mark_vanReqBytes = AppUtil.getMarkByte(vanReqBytes, "******", 138, "000", 405); // 카드번호 MARK 시작 위치
           appWorker.accessLog(AbstractWorker.SV, mark_vanReqBytes);
           */
           
           //vanConn.sendData(vanReqBytes);
            Thread.sleep(940); // 940 밀리세컨드 지연 
           
       }catch(Exception e){
    	   /*
           bExcept = true;
           throw new MyException("[KICCWorker::procVANStressTest:Send]", MyException.SY22, "Exception:"+e, MyException.sysErrMsg);
           */
       }finally{
    	   /*
           if(bExcept){
               AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
               appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
           }
           */
       }

       //////////////////////////////////////////////////////////////////////////
       // 응답데이터 수신
       //////////////////////////////////////////////////////////////////////////
       try{
    	   /*
           vanResBytes = vanConn.recvData(AppVANClient.TYPE_LEN);
           byte[] mark_vanResBytes = null;
           mark_vanResBytes = AppUtil.getMarkByte(vanResBytes, "******", 158, "", 0);  // 카드번호 MARK 시작 위치
           */
           appWorker.accessLog(AbstractWorker.RV, "Receive From VAN".getBytes());
           appWorker.accessLog(AbstractWorker.LG, (transType+ " Process Time : "+(System.currentTimeMillis()-startTime) + "ms").getBytes());

       }catch(Exception e){
           bExcept = true;
           throw new MyException("[KICCWorker::procVANStressTest:Receive]", MyException.SY23, "Exception:"+e, MyException.sysErrMsg);
       }finally{
    	   /*
           AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
           appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
           */
       }

       //////////////////////////////////////////////////////////////////////////
       // 응답데이터 분석
       //////////////////////////////////////////////////////////////////////////
       try{
           //vanRes = vanData.parseData(vanResBytes, AppUtil.checkNull(vanReq.get("VS_FIELD01")));
           
           // VAN 응답 RFIELD12 가 "1" 이면 Downgrade 승인 이므로 망상취소를 위한 구분값으로 Header RH02 에 "0420" 코드를 셋팅한다.
          if ( "1".equals(AppUtil.checkNull(vanRes.get("VR_FIELD12")).trim()) && AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O") ) {
              dataInfo.put("RH02", "0420") ;
          }        
       }catch(Exception e){
           throw e;
       }


    //////////////////////////////////////////////////////////////////////////
	// 응답데이터 저장
	//////////////////////////////////////////////////////////////////////////
       try{

           //응답 데이터 저장 (승인취소 거래는 mcpstatus유지되도록 함)
           vanDBProc.updateTransact(vanRes, dataInfo);

       }catch(MyException e){
    	   //e.printStackTrace();
           //throw e;  에외 무시
    	   //appWorker.accessLog(AbstractWorker.LG, ("ERROR: "+e.getMessage()).getBytes());
    	   if(e.getMessage().contains("NullPointerException")) {
    		   throw e;
    	   }
       }

    //////////////////////////////////////////////////////////////////////////
	// 응답데이터 Copy
	//////////////////////////////////////////////////////////////////////////
       dataInfo.put("VD_RESCODE", "0000");                     //응답코드
       dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());            //승인번호
       dataInfo.put("VD_TRADEDT", AppUtil.checkNull(dataInfo.get("VD_DBTRADEDT")));
       dataInfo.put("VD_TRADEDT_AUTH", "20"+AppUtil.checkNull(vanRes.get("VR_FIELD04")));          //매입사에서 전송한 승인시간
       dataInfo.put("VD_DCCINFO", AppUtil.checkNull(vanRes.get("VR_FIELD30")));                    //DCC 결제정보
       dataInfo.put("VD_FOREIGNCURRENCY", AppUtil.checkNull(vanRes.get("VR_FIELD20")));            //Foreign Amount
       dataInfo.put("VD_EXCHANGERATE", AppUtil.checkNull(vanRes.get("VR_FIELD21")));               //Exchange Rate
       dataInfo.put("VD_INVERTEDRATE", AppUtil.checkNull(vanRes.get("VR_FIELD22")));               //Inverted Rate
       dataInfo.put("VD_CURRENCYCODE", AppUtil.checkNull(vanRes.get("VR_FIELD23")));               //외환 영문코드
       dataInfo.put("VD_MARKUP", AppUtil.checkNull(vanRes.get("VR_FIELD24")));                     //Mark up percent
       dataInfo.put("VD_COMMISSIONP", AppUtil.checkNull(vanRes.get("VR_FIELD25")));                //Commission Percent
       dataInfo.put("VD_COMMISSIONV", AppUtil.checkNull(vanRes.get("VR_FIELD26")));                //Commission Value
		
	}
    
    

	private void formatData(Map<String, String> vanReq, Map<String, String> dataInfo) throws MyException{

        try{
        	System.out.println("[vanReq] : " + vanReq);
            String nowDT = AppUtil.getDateTime();

            if(transType.equals("0100") || transType.equals("0102") || transType.equals("0120") || transType.equals("0122") || transType.equals("0181") || transType.equals("0184") || 
               transType.equals("0200") || transType.equals("0202") || transType.equals("0204") || transType.equals("0206") || transType.equals("0400") || transType.equals("9999")){
                //승인, 취소
                dataInfo.put("VD_MSGTYPE", getMsgType(Integer.parseInt((String)dataInfo.get("RH00"))));           //MsgType

                if(transType.equals("0200") || transType.equals("0202") || transType.equals("0400") || transType.equals("0204") || transType.equals("0206")){
                    dataInfo.put("VD_STATUS", "T001");
                    
                    if(transType.equals("0400")){
                        dataInfo.put("VD_ORGTRANSNO", "0");                             
                    }
                }else if(transType.equals("0120") || transType.equals("0122") ||  transType.equals("0184")){
                    //KEB Authorization(미매입)
                    dataInfo.put("VD_STATUS", "T003");                                                              //status
                    dataInfo.put("VD_ORGTRANSNO", "0");
                }else{
                    dataInfo.put("VD_STATUS", "T000");                                                              //status
                    dataInfo.put("VD_ORGTRANSNO", "0");                                                             //원거래transno
                }
                
                //VAN 전문구성===============================================
                //VAN TID
                vanReq.put("VS_FIELD00", AppUtil.checkNull(dataInfo.get("VD_KICCTID")));
                //전문구분 (전문번호, 기본통화, 자국통화, MCPYN)
                vanReq.put("VS_FIELD01", getField01(Integer.parseInt((String)dataInfo.get("RH00")), AppUtil.checkNull(dataInfo.get("RB03")),  AppUtil.checkNull(dataInfo.get("RB21")), AppUtil.checkNull(dataInfo.get("VD_MCPYN"))));
                /***************************************************************
                 * KICC는 기본통화 USD-KRW-HKD-THD-JPY-CNY 만을 지원한다.
                 **************************************************************/
                if (vanReq.get("VS_FIELD01").trim().equals(""))
                    throw new MyException("[KICCWorker::formatData]", MyException.SY21, "KICC Unsupported Base Currency", "KICC Unsupported Base Currency");
                
                //POS Entry Mode
                vanReq.put("VS_FIELD02", "K");
                //카드번호=유효기간
                String trkData = AppUtil.checkNull(dataInfo.get("RB09")).trim() + "=" + AppUtil.checkNull(dataInfo.get("RB10"));
                /****************************************************************************
                 * eWallete 2020.09.03.
                 * eWallete의 경우에는 인증시 넘오 왔던 track2data를 그대로 넘긴다.
                 * eWallete 구분은 3D구분필드가 'V' 일경우이며 track2data는 xid 필드에 셋팅된다.
                 ****************************************************************************/
                if(AppUtil.checkNull(dataInfo.get("RB14")).equals("V")){ 
                    vanReq.put("VS_FIELD03", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB16")), AppUtil.LEFT, 37, ' ')); 
                    // 카드번호=유효기간을 뺀 나머지로만 새로 셋팅..
                    dataInfo.put("RB16", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB16")), AppUtil.LEFT, 37, ' ').substring(21)); 
                } else {
                    vanReq.put("VS_FIELD03", AppUtil.formatData(trkData, AppUtil.LEFT, 37, ' '));  //trk data
                }
                //할부개월수
                vanReq.put("VS_FIELD04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부
                if(transType.equals("0181") || transType.equals("0184") || (transType.equals("0200") && AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y"))){
                    //MCP통신전문에 사용
                    //통화구분-MCP는 무조건 원화금액
                    vanReq.put("VS_FIELD05_1", getCurrency("KRW"));
                    //통화소숫점자릿수
                    vanReq.put("VS_FIELD06_1", "0");
                    //총금액-MCP는 무조건 원화금액
                    vanReq.put("VS_FIELD07_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB26")), AppUtil.RIGHT, 12, '0'));
                }
                //DB저장에 사용
                //통화구분 
                vanReq.put("VS_FIELD05", getCurrency(AppUtil.checkNull(dataInfo.get("RB03"))));
                /***************************************************************
                 * KICC는 기본통화 USD-KRW-HKD-THD-JPY-CNY 만을 지원한다.
                 **************************************************************/
                if (vanReq.get("VS_FIELD05").trim().equals(""))
                    throw new MyException("[KICCWorker::formatData]", MyException.SY21, "KICC Unsupported Base Currency", "KICC Unsupported Base Currency");
                
                //통화소숫점자릿수
                if(AppUtil.checkNull(dataInfo.get("RB03")).equals("KRW") || AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY") || AppUtil.checkNull(dataInfo.get("RB03")).equals("THB") ){
                    vanReq.put("VS_FIELD06", "0");
                }else{
                    vanReq.put("VS_FIELD06", "2");
                }
                //일본 엔화와 타이 바트의 경우
                if(AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY") || AppUtil.checkNull(dataInfo.get("RB03")).equals("THB")){
                    //JPY금액 * 100, THB금액 * 100
                    if ( transType.equals("0204") ) { // 부분취소의 경우 취소금액 저장
                        String jpyAmt = AppUtil.checkNull(dataInfo.get("RB50")) + "00";
                        vanReq.put("VS_FIELD07", AppUtil.formatData(jpyAmt, AppUtil.RIGHT, 12, '0'));
                    } else {
                        String jpyAmt = AppUtil.checkNull(dataInfo.get("RB04")) + "00";
                        vanReq.put("VS_FIELD07", AppUtil.formatData(jpyAmt, AppUtil.RIGHT, 12, '0'));
                    }    
                }else{ 
                    if ( transType.equals("0204") ||  transType.equals("0206") ) // 부분취소의 경우 취소금액 저장
                        vanReq.put("VS_FIELD07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB50")), AppUtil.RIGHT, 12, '0'));
                    else
                        vanReq.put("VS_FIELD07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));
                }
                
                //Edgar Test 코드 
                /*
                if("4176669999000104".equals(AppUtil.checkNull(dataInfo.get("RB09")).trim())) {
                	vanReq.put("VS_FIELD07", AppUtil.formatData("0", AppUtil.RIGHT, 12, '0'));
                }
                */
                
                //봉사료
                vanReq.put("VS_FIELD08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));
                //세금
                vanReq.put("VS_FIELD09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));  
                if(transType.equals("0200") || transType.equals("0202") || transType.equals("0204") || transType.equals("0206")){
                    //승인번호
                    vanReq.put("VS_FIELD10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB51")), AppUtil.LEFT, 12, ' '));

// yunsu 20160927 취소시 원거래 일자 확인으로 인한 주석처리
//                    vanReq.put("VS_FIELD11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB52")), AppUtil.RIGHT, 6, ' '));
                    //거래일자
                    vanReq.put("VS_FIELD11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_AUTHDT")), AppUtil.RIGHT, 6, ' '));                   
                }else{
                    //승인번호
                    vanReq.put("VS_FIELD10", AppUtil.formatData("", AppUtil.LEFT, 12, ' '));
                    //거래일자
                    vanReq.put("VS_FIELD11", AppUtil.formatData("", AppUtil.LEFT, 6, ' '));
                }
                //Working Key Index
                vanReq.put("VS_FIELD12", "AA");
                //비밀번호
                vanReq.put("VS_FIELD13", AppUtil.formatData("", AppUtil.LEFT, 16, '0'));
                //상품코드(MCP 사업자구분 코드로 변경 2012-10-18 by kong)
                //vanReq.put("VS_FIELD14", AppUtil.formatData("D", AppUtil.LEFT, 6, ' '));
                vanReq.put("VS_FIELD14", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_MCPCODE")), AppUtil.LEFT, 6, ' '));
                //주민등록번호 또는 사업자번호
                vanReq.put("VS_FIELD15", AppUtil.formatData("", AppUtil.LEFT, 10, ' '));
                //전자상거래보안등급
                vanReq.put("VS_FIELD16_1", "7");
                //도메인
                vanReq.put("VS_FIELD16_2", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SITEURL")), AppUtil.LEFT, 40, ' '));
                //서버 IP
                vanReq.put("VS_FIELD16_3", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SERVERIP")), AppUtil.LEFT, 20, ' '));
                //몰사업자구분
                vanReq.put("VS_FIELD16_4", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_BIZNO")), AppUtil.LEFT, 10, ' '));
                //카드정보전송구분
                vanReq.put("VS_FIELD17", "1");
                //가맹점사용ID 
                vanReq.put("VS_FIELD18", "  ");
                //가맹점사용필드
                //3DS2.0일경우에는 가맹점 사용필드에 3DS를 추가하여 1.0과 2.0을 구분한다.(KICC 요청),  UPI관련 07의 경우 Z(secureap, 셋팅 필요), UPI 10의 경우 어떻게?
                //2022.04.22 KICC 개발기 및 테스트기 통신 고날 mid 분기 추가 walter. case 3번 할 경우.
                System.out.println("[KICCWorker::formatData] 4091 3ds 2.0 RB01 : " + AppUtil.checkNull(dataInfo.get("RB01")));                         
//                if("47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01")))){
//                	dataInfo.put("RB14", "Z");
//                }
//                
                if (AppUtil.checkNull(dataInfo.get("RB14")).equals("Z")) {
                    vanReq.put("VS_FIELD19", AppUtil.formatData("3DS", AppUtil.LEFT, 30, ' '));
                } else {

                   	vanReq.put("VS_FIELD19", AppUtil.formatData("", AppUtil.LEFT, 30, ' '));       	
                    
                }
                //수표조회
                //VISA && BC 서브몰 사업자 관련 필드 추가 - 2022-10-20 ayaan
                if(AppUtil.checkNull(dataInfo.get("RB08")).equals("C000") && AppUtil.checkNull(dataInfo.get("VD_PURCHASECODE")).trim().equals("CD00")) {
                	vanReq.put("VS_FIELD20", AppUtil.formatData("10077007BI", AppUtil.LEFT, 40, ' '));
                }else {
                	vanReq.put("VS_FIELD20", AppUtil.formatData("", AppUtil.LEFT, 40, ' '));
                }
                
                //CVV2
                /******************************************************************
                 * 카드코드 C026 에 UPOPTYPE 이 "E"의 경우는 신한 비인증 온라인거래
                 */
                if ( (AppUtil.checkNull(dataInfo.get("RB08")).equals("C026") || AppUtil.checkNull(dataInfo.get("RB08")).equals("C008")) && !AppUtil.checkNull(dataInfo.get("VD_UPOPTYPE")).trim().equals("E"))  { // 비씨 은련의 경우 CVC데이터는 스페이스로 넘긴다 CVC인증은 아직 미개발되었음
                    // 개발 되었는지.. 여기에 비씨 은련 인증/비인증 구분한다. 2022-01-07
                    if (AppUtil.checkNull(dataInfo.get("RB14")).equals("Z") && !AppUtil.checkNull(dataInfo.get("RB11")).trim().equals("") ) {
                        vanReq.put("VS_FIELD21_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB11")), AppUtil.LEFT, 3, ' '));
                        
                    } else { 
                        vanReq.put("VS_FIELD21_1",  AppUtil.formatData("", AppUtil.LEFT, 3, ' '));
                    }
                } else { 
                	// Edgar 2024.10.4 Amex 전문 처리를 위한 수정 
                	//vanReq.put("VS_FIELD21_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB11")), AppUtil.LEFT, 3, ' '));
                	if (AppUtil.checkNull(dataInfo.get("RB11")).trim().length() > 3) {
                		// CVC 값이 3자리 이상이면 빈값으로 셋팅
                		vanReq.put("VS_FIELD21_1",  AppUtil.formatData("", AppUtil.LEFT, 3, ' '));
                	}else {
                		vanReq.put("VS_FIELD21_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB11")), AppUtil.LEFT, 3, ' '));
                	}
                }
             
                //CVV예비
                /******************************************************************
                 * 카드코드 C026 에 UPOPTYPE 이 "E"의 경우는 신한 비인증 온라인거래
                 */
                if(!AppUtil.checkNull(dataInfo.get("RB11")).trim().equals("")) {
                    if (( AppUtil.checkNull(dataInfo.get("RB08")).equals("C026")  || AppUtil.checkNull(dataInfo.get("RB08")).equals("C008"))  && !AppUtil.checkNull(dataInfo.get("VD_UPOPTYPE")).trim().equals("E") ) { // 비씨 은련 거래 추가 8번째에 CU 추가
                        vanReq.put("VS_FIELD21_2", AppUtil.formatData("CVCDATACU", AppUtil.LEFT, 27, ' '));
                    } else {    
                    	// Edgar 2024.10.4 Amex 전문 처리를 위한 수정 
                        //vanReq.put("VS_FIELD21_2", AppUtil.formatData("CVCDATA", AppUtil.LEFT, 27, ' '));
                    	if (dataInfo.get("RB11").trim().length() > 3) {
                    		vanReq.put("VS_FIELD21_2", AppUtil.formatData("CVCDATA"+dataInfo.get("RB11"), AppUtil.LEFT, 27, ' '));
                    	}else {
                    		vanReq.put("VS_FIELD21_2", AppUtil.formatData("CVCDATA", AppUtil.LEFT, 27, ' '));
                    	}
                    }
                } else {
                    if (( AppUtil.checkNull(dataInfo.get("RB08")).equals("C026")  || AppUtil.checkNull(dataInfo.get("RB08")).equals("C008")) && !AppUtil.checkNull(dataInfo.get("VD_UPOPTYPE")).trim().equals("E")) { // 은련은 비인증 거래(CVC 옵션)에 항상 CVCDATA를 입력한다. BC카드로 비인증 FLAG가 따로 전송되므로(KICC에서)
                        vanReq.put("VS_FIELD21_2", AppUtil.formatData("CVCDATACU", AppUtil.LEFT, 27, ' '));
                    } else {
                        vanReq.put("VS_FIELD21_2", AppUtil.formatData("", AppUtil.LEFT, 27, ' '));
                    }
                    //2022.10.20 3ds2.0 amex 테스트 walter
//                    if("47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01")))) {
//                    	vanReq.put("VS_FIELD21_2", AppUtil.formatData("CVCDATA", AppUtil.LEFT, 27, ' '));	
//                    }
                    	
                    
                }
                //선택통화 금액
                vanReq.put("VS_FIELD22_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB22")), AppUtil.RIGHT, 12, '0'));
                //선택통화 환율정보
                vanReq.put("VS_FIELD22_2", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB24")), AppUtil.RIGHT, 10, '0'));
                
                /******** 중요 KICC는 마이너 유신 8과 4만을 허용하므로 변경하여야 한다 ********/
                if (AppUtil.checkNull(dataInfo.get("VD_CARDMID")).length() > 3 ) { 
                    if (AppUtil.checkNull(dataInfo.get("VD_CARDMID")).trim().substring(0,3).equals("009")) {  // 하나(외환)만 적용.
                        if( !vanReq.get("VS_FIELD22_2").substring(0,1).equals("8") && !vanReq.get("VS_FIELD22_2").substring(0,1).equals("4") && !vanReq.get("VS_FIELD22_2").substring(0,1).equals("0")) {
                            String unit = vanReq.get("VS_FIELD22_2").substring(0,1);
                            String rate = vanReq.get("VS_FIELD22_2").substring(1, 9 - Integer.parseInt(unit) + 1) + "." + vanReq.get("VS_FIELD22_2").substring(9 - Integer.parseInt(unit) + 1);
                            // 기본통화 410 이고 자국통화 704 일경우 마이너 유닛이 4일경우는 100 기준이므로..환율에 100을 곱해서 준다.
                            if (AppUtil.checkNull(dataInfo.get("RB21")).equals("704") && AppUtil.checkNull(dataInfo.get("RB25")).equals("410") ) {
                                rate = String.valueOf(Double.valueOf(rate) * 100);
                            }
                            DecimalFormat min;
                            min = new DecimalFormat("#############.0000");  // 마이너 유닛 4로 만든다.

                            String tmp_rate = "4" + AppUtil.formatData(min.format(Double.valueOf(rate)).replace(".", "") , AppUtil.RIGHT, 9, '0');
                            vanReq.put("VS_FIELD22_2", tmp_rate);
                        }
                    }
                }
                //선택통화 코드
                vanReq.put("VS_FIELD22_3", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB21")), AppUtil.RIGHT,  3, '0'));
                //선택통화 Minor Unit
                vanReq.put("VS_FIELD22_4", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB23")), AppUtil.RIGHT,  1, '0'));
                //BASE통화 코드
                vanReq.put("VS_FIELD22_5", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB25")), AppUtil.RIGHT,  3, '0'));
                //BASE통화 금액
                vanReq.put("VS_FIELD22_6", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT,  9, '0'));
                
                // 2018-08-23 UPOP 리커링의 경우는 공인인증 구분 스펙에 "R"로 설정한다.(UPOP의경우 비인증만 지원)
                if(AppUtil.checkNull(dataInfo.get("VD_UPOPTYPE")).equals("R") && (AppUtil.checkNull(dataInfo.get("RB08")).equals("C026")  || AppUtil.checkNull(dataInfo.get("RB08")).equals("C008")) ) {
                	System.out.println("[KICCWorker::formatData] UPOP 리커링");
                    vanReq.put("VS_FIELD23_1", "R");
                    vanReq.put("VS_FIELD23_2", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_MPIMODULE")), AppUtil.LEFT, 1, ' '));
                    vanReq.put("VS_FIELD23_3", "N");
                } else {
                    
                    // 3DS 1.0 : Y,   3DS 2.0 : Z
                    // 3DS 2.0의 경우 xid가 36byte
                    if(AppUtil.checkNull(dataInfo.get("RB14")).equals("Y") || AppUtil.checkNull(dataInfo.get("RB14")).equals("Z")){
                    	System.out.println("[KICCWorker::formatData] MPI");
                        //MPI 거래
                        vanReq.put("VS_FIELD23_1", "M");
                        vanReq.put("VS_FIELD23_2", "C");
                        vanReq.put("VS_FIELD23_3", "N");

                        String cavv = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB15")), AppUtil.LEFT, 40, ' ');
                        String xid = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB16")), AppUtil.LEFT, 40, ' ');
                        String eci = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB18")), AppUtil.LEFT, 2, ' ');
//                        String cavv = AppUtil.formatData(AppUtil.checkNull("BQEXJgAAMGlHODgAWZBpiEhnAAA=            "), AppUtil.LEFT, 40, ' ');
//                        String xid = AppUtil.formatData(AppUtil.checkNull("257cd35a-bfb5-40ff-a6a9-19b4d65081c3    "), AppUtil.LEFT, 40, ' ');
//                        String eci = AppUtil.formatData(AppUtil.checkNull("07"), AppUtil.LEFT, 2, ' ');
                        
                        // 하나카드 요청
                        String accessip = "";
                        String sessionid = "";
                        if (!AppUtil.checkNull(dataInfo.get("VD_ACCESSIP")).trim().equals("")) {
                            if (!AppUtil.checkNull(dataInfo.get("VD_RISKACCESSIP")).trim().equals("")) {
                                accessip = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_RISKACCESSIP")).trim(), AppUtil.LEFT, 20, ' ');
                            } else {    
                                accessip = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_ACCESSIP")).trim(), AppUtil.LEFT, 20, ' ');
                            }
                            sessionid = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_RISKSESSIONID")).trim(), AppUtil.LEFT, 88, ' ');
                            vanReq.put("VS_FIELD23_2", "F");
                        }
                        
                        //2022.04.22 KICC 개발기 및 테스트기 통신 고날 mid 분기 추가 walter.. case 3번 할 경우.
//                        System.out.println("[KICCWorker::formatData] RB01 : " + AppUtil.checkNull(dataInfo.get("RB01")));                         
//                        if("47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01")))){
//                        	eci = "07"; //case 3
//                        	//eci = "10";//case 4 3ds 거래가 아닌데, eci값을 넘긴다? ㅡㅡㅋ
//                        }
  
                        vanReq.put("VS_FIELD23_4", cavv+xid+eci);
                        vanReq.put("VS_FIELD23_5", accessip+sessionid);
                        

                    } else if (AppUtil.checkNull(dataInfo.get("RB14")).equals("I")){
                    	System.out.println("[KICCWorker::formatData] BC UPOT");
                        // BC UPOT
                        vanReq.put("VS_FIELD23_1", "I");
                        vanReq.put("VS_FIELD23_2", "C");
                        vanReq.put("VS_FIELD23_3", "N");

                    } else if (AppUtil.checkNull(dataInfo.get("RB14")).equals("V")) {
                    	 System.out.println("[KICCWorker::formatData] eWallete");
                        /****************************************************************************
                        * eWallete 2020.09.03.
                        * eWallete의 경우에는 인증시 넘오 왔던 cavv를 그대로 넘긴다.
                        * eWallete 구분은 3D구분필드가 'V' 일경우이며 cavv를 cavv를 필드에 셋팅된다.
                        ****************************************************************************/ 
                        vanReq.put("VS_FIELD23_1", "V");
                        vanReq.put("VS_FIELD23_2", "C");
                        vanReq.put("VS_FIELD23_3", "N");
                        
                        String cavv = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB15")), AppUtil.LEFT, 40, ' ');
                        String xid = AppUtil.formatData("", AppUtil.LEFT, 40, ' ');
                        String eci = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB18")), AppUtil.LEFT, 2, ' ');
                        vanReq.put("VS_FIELD23_4", cavv+xid+eci);
                        
                    } else{
                    	 System.out.println("[KICCWorker::formatData] 비인증");
                        //오프라인 거래
                        vanReq.put("VS_FIELD23_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_MPI")), AppUtil.LEFT, 1, ' '));
                        vanReq.put("VS_FIELD23_2", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_MPIMODULE")), AppUtil.LEFT, 1, ' '));
                        vanReq.put("VS_FIELD23_3", "N");

                        // 인증실패이면서 ECI 값만 내려오는 경우...
                        if(!AppUtil.checkNull(dataInfo.get("RB18")).trim().equals("")) {
                            
                            //if (AppUtil.checkNull(dataInfo.get("RB08")).equals("C008")) {  // 유니온페이 MPI의 경우는 인증 실패했을 경우 ECI값 이외에 3DS를 강제로 셋팅해 준다..<<중요>>
                            //     vanReq.put("VS_FIELD19", AppUtil.formatData("3DS", AppUtil.LEFT, 30, ' ')); 
                            //} 
                            String cavv = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB15")), AppUtil.LEFT, 40, ' ');
                            String xid = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB16")), AppUtil.LEFT, 40, ' ');
                            String eci = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB18")), AppUtil.LEFT, 2, ' ');
                            // 하나카드 요청 
                            String accessip = "";
                            String sessionid = "";
                            if (!AppUtil.checkNull(dataInfo.get("VD_ACCESSIP")).trim().equals("")) {
                                 if (!AppUtil.checkNull(dataInfo.get("VD_RISKACCESSIP")).trim().equals("")) {
                                    accessip = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_RISKACCESSIP")).trim(), AppUtil.LEFT, 20, ' ');
                                 } else {    
                                    accessip = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_ACCESSIP")).trim(), AppUtil.LEFT, 20, ' ');
                                 }
                                sessionid = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_RISKSESSIONID")).trim(), AppUtil.LEFT, 88, ' ');
                                vanReq.put("VS_FIELD23_2", "F");
                            }
                            
                            //2022.04.22 KICC 개발기 및 테스트기 통신 고날 mid 분기 추가 walter.
//	                          System.out.println("[VerWorker::procF0100] 4091 RB01 : " + AppUtil.checkNull(dataInfo.get("RB01")));                         
//	                          if("47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01")))){
//	                          	eci = "07";
//	                          }
                            vanReq.put("VS_FIELD23_4", cavv+xid+eci);
                            vanReq.put("VS_FIELD23_5", accessip+sessionid);
                        } else {
                            String cavv = AppUtil.formatData("", AppUtil.LEFT, 40, ' ');
                            String xid = AppUtil.formatData("", AppUtil.LEFT, 40, ' ');
                            String eci = AppUtil.formatData("", AppUtil.LEFT, 2, ' ');
                            // 하나카드 요청
                            String accessip = "";
                            String sessionid = "";
                            if (!AppUtil.checkNull(dataInfo.get("VD_ACCESSIP")).trim().equals("")) {
                                if (!AppUtil.checkNull(dataInfo.get("VD_RISKACCESSIP")).trim().equals("")) {
                                    accessip = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_RISKACCESSIP")).trim(), AppUtil.LEFT, 20, ' ');
                                } else {    
                                    accessip = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_ACCESSIP")).trim(), AppUtil.LEFT, 20, ' ');
                                }
                                sessionid = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_RISKSESSIONID")).trim(), AppUtil.LEFT, 88, ' ');
                                vanReq.put("VS_FIELD23_2", "F");
                            }
                            //2022.04.22 KICC 개발기 및 테스트기 통신 고날 mid 분기 추가 walter.
	                          System.out.println("[KICCWorker::formatData] 4091 else RB01 : " + AppUtil.checkNull(dataInfo.get("RB01")));                         
//	                          if("47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01")))){
//	                               //cavv = AppUtil.formatData(AppUtil.checkNull("BQEXJgAAMGlHODgAWZBpiEhnAAA=            "), AppUtil.LEFT, 40, ' ');
//	                               //xid = AppUtil.formatData(AppUtil.checkNull("257cd35a-bfb5-40ff-a6a9-19b4d65081c3    "), AppUtil.LEFT, 40, ' ');
//	                               //eci = AppUtil.formatData(AppUtil.checkNull("07"), AppUtil.LEFT, 2, ' ');
//	                        	   cavv = AppUtil.formatData("", AppUtil.LEFT, 40, ' ');//case 4 비인증
//	                        	   xid = AppUtil.formatData("", AppUtil.LEFT, 40, ' ');//case 4 비인증
//	                               eci = "10"; //case 4 비인증
//	                               
//	                          }
                            vanReq.put("VS_FIELD23_4", cavv+xid+eci);
                            vanReq.put("VS_FIELD23_5", accessip+sessionid);
                            
                        }
                       
                        
                    } 
                }
                
                System.out.println("[KICCWorker::formatData] clientip check : " + AppUtil.checkNull(vanReq.get("VS_FIELD23_5")));
                
                if(transType.equals("0200") || transType.equals("0202") || transType.equals("0204") || transType.equals("0206"))
                    vanReq.put("VD_MCPYN", AppUtil.checkNull(dataInfo.get("VD_MCPYN")));
                else if(transType.equals("0181") || transType.equals("0184"))
                    vanReq.put("VD_MCPYN", "Y");
                else vanReq.put("VD_MCPYN", "N");

            }else throw new MyException("[KICCWorker::formatData]", MyException.SY21, "Undefined MsgType", MyException.sysErrMsg);
        }catch(Exception e){
            throw new MyException("[KICCWorker::formatData]", MyException.SY21, "Exception:"+e, MyException.sysErrMsg);
        }

    }

    private String getMsgType(int format){
        if(format == AppData.F0200 || format == AppData.F0202 || format == AppData.F0400) return "1210";
        else if (format == AppData.F0204) return "0204";
        else if (format == AppData.F0206) return "0206";
        else return "1130";
    }

    private String getField01(int format, String basecurrency, String homecurrency, String mcpYN){
        switch(format){
           case AppData.F0100:
           case AppData.F0102:
           case AppData.F0120: //승인(수동매입)
               if(basecurrency.equals("USD") || basecurrency.equals("KRW") || basecurrency.equals("HKD") || basecurrency.equals("THB")  || basecurrency.equals("JPY") || basecurrency.equals("CNY")) return "1130";
               else return "    ";              //MCA USD, KRW, HKD, THB, JPY, CNY 이외통화
           case AppData.F0122: //승인(수동매입) bc 유니온페이 authorize 관련 추가 walter 2021.12.22
               if(basecurrency.equals("USD") || basecurrency.equals("KRW") || basecurrency.equals("HKD") || basecurrency.equals("THB")  || basecurrency.equals("JPY") || basecurrency.equals("CNY")) return "1130";
               else return "    ";              //MCA USD, KRW, HKD, THB, JPY, CNY 이외통화
           case AppData.F0181:                  //MCP 승인
           case AppData.F0184:                  //MCP Auth
               if(basecurrency.equals("USD") && !homecurrency.equals("840")) return "7750";
               else if(basecurrency.equals("KRW") && !homecurrency.equals("410")) return "7720";
               else if(basecurrency.equals("JPY")) return "    ";
               else return "1130";   // 일반 승인으로..
           case AppData.F0200:       //취소
           case AppData.F0202:
           case AppData.F0204:
           case AppData.F0206:    
           case AppData.F0400:
               if((format == AppData.F0200 && mcpYN.equals("N")) || (format == AppData.F0204 && mcpYN.equals("N")) || (format == AppData.F0202 && mcpYN.equals("N")) || (format == AppData.F0206 && mcpYN.equals("N")) ||  format == AppData.F0400){                   
                   if(basecurrency.equals("USD") || basecurrency.equals("KRW")  || basecurrency.equals("HKD") || basecurrency.equals("THB")  || basecurrency.equals("JPY") || basecurrency.equals("CNY")) return "1210";    
                   else return "    ";  //MCA USD, KRW, HKD, THB, JPY, CNY 이외통화
               }else{
                   if(basecurrency.equals("USD") && !homecurrency.equals("840")) return "7760";
                   else if(basecurrency.equals("KRW") && !homecurrency.equals("410")) return "7730";
                   else if(basecurrency.equals("JPY")) return "    ";
                   else return "1210"; // 일반 승인 취소로
               }
        }
        
         return "    ";
	}

    private String getCurrency(String currency){
        if(currency.equals("KRW")) return "1";
        else if(currency.equals("USD")) return "2";
        else if(currency.equals("JPY")) return "3";
        else if(currency.equals("EUR")) return "4";
        else if(currency.equals("HKD")) return "5";
        else if(currency.equals("SGD")) return "7";
        else if(currency.equals("THB")) return "9";
        else if(currency.equals("CNY")) return "C";
        return "   ";
    }
    public void procCheckSYSTEM() throws MyException{
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 시스템 점검 여부 확인
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        String propFilePath = "";
        String csTitle = "SYSTEMCHECK";
        String csName = "";
        String csFlag = "";
        String csMsg  = "";
        
        csProps  = new Properties();
        try{
            File f = new File(new File("").getCanonicalPath());
            propFilePath = f.toString() + "/conf/cs.properties"; //proerties path 가져오기
            csProps.load(new BufferedReader(
					new InputStreamReader(
							new FileInputStream(propFilePath),"MS949")));
            Enumeration enuPropNames = csProps.propertyNames();
            while (enuPropNames.hasMoreElements()) {
                String propName = (String) enuPropNames.nextElement();
                if (propName.endsWith((".FLAG"))) {
                    csName = propName.substring(0, propName.lastIndexOf("."));
                    if(csTitle.equals(csName)){
                        csFlag = csProps.getProperty(csName + ".FLAG");
                        csFlag = csFlag.substring(0, 1);
                    }
                }
                if (propName.endsWith((".MSG"))) {
                    csName = propName.substring(0, propName.lastIndexOf("."));
                    if(csTitle.equals(csName)){
                        csMsg = csProps.getProperty(csName + ".MSG");
                    }
                }
            }
       
        }catch(Exception e){
             System.out.println(e);
        }
        if (AppUtil.checkNull(csFlag).equals("Y"))
            throw new MyException("[KICCWorker::procCheckSYSTEM]", "9999", csMsg, csMsg);
     }
}

