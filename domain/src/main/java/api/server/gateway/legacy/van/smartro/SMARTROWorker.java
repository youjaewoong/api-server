/*
 * KSNETWorker.java
 *
 * Created on 2008년 10월 6일 (월), 오후 3:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package api.server.gateway.legacy.van.smartro;


import api.server.common.helper.AppUtil;
import api.server.gateway.legacy.van.AppVAN;
import api.server.gateway.legacy.van.AppVANClient;
import api.server.gateway.legacy.AbstractApp;
import api.server.gateway.legacy.AbstractWorker;
import api.server.gateway.legacy.AppData;
import api.server.gateway.legacy.AppWorker;
import api.server.gateway.legacy.my.MyException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 공현진
 */
public class SMARTROWorker implements AppVAN {

    private final String VAN_NAME = "SMARTRO";

    private AppWorker appWorker;
    private SMARTROData vanData;
    private SMARTRODBProc vanDBProc;

    //private Socket m_socket;
    //private InputStream m_is;
    //private OutputStream m_os;

    private String transType;
    private boolean bExcept;        //Connection 이후의 Exception구분(Disconnection을 위해)

    /**
     * Creates a new instance of SMARTROWorker
     */
    public SMARTROWorker(AppWorker appWorker) {
        this.appWorker = appWorker;

        vanData = new SMARTROData();
        vanDBProc = new SMARTRODBProc(AbstractApp.g_dbcm);
    }

    public void procVAN(Map<String, String> dataInfo) throws MyException {
        System.out.println("[SMARTROWorker::procVAN]Start");
        transType = AppUtil.checkNull(dataInfo.get("RH03"));

        AppVANClient vanConn = null;

        Map<String, String> vanReq = new HashMap<>();
        Map<String, String> vanRes = new HashMap<>();

        byte[] vanReqBytes = null;
        byte[] vanResBytes = null;

        bExcept = false;

        //////////////////////////////////////////////////////////////////////////
        // VAN 가맹점 아이디, 터미널 아이디, 사업자 번호 추출
        //////////////////////////////////////////////////////////////////////////
        if (!transType.equals("0200") && !transType.equals("0125")) { //2013.06.11
            try {
                vanDBProc.selectMerchant(dataInfo);
            } catch (MyException e) {
                throw e;
            }
        }

        //////////////////////////////////////////////////////////////////////////
        // 요청데이터 셋팅 및 저장
        //////////////////////////////////////////////////////////////////////////
        try {
            if (transType.equals("0100") || transType.equals("0120") || transType.equals("0140") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184")
                /*|| transType.equals("0400")*/) {   //환급거래 주문번호 확인 안함 20130311
                //동일 주문번호 확인, BIN 데이터 확인(해외카드 전용)
                vanDBProc.selectTransact(dataInfo);
            } else if (transType.equals("0200")) {
                //원거래 데이터 추출
                vanReq.put("VS_FIELD05", getCurrency(AppUtil.checkNull(dataInfo.get("RB03"))));
                vanDBProc.selectOrgTransact(vanReq, dataInfo);
            } else if (transType.equals("0125")) {
                //승인일 포함 20일 이내 거래인지 확인(거래구분 및 매입상태 확인) --> 정상인 경우 매입요청으로 변경
                vanDBProc.checkAuthTransact(dataInfo);
                dataInfo.put("VD_RESCODE", "0000");
                dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(dataInfo.get("VR_FIELD13")).trim());            //승인번호
                dataInfo.put("VD_TRADEDT", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));               //거래일자(한국시간 기준)
                return;
            }

            //데이터 포맷팅
            formatData(vanReq, dataInfo);

            //요청 데이터 저장
            vanDBProc.insertTransact(vanReq, dataInfo);
            dataInfo.put("VD_SEQNO", AppUtil.checkNull(vanReq.get("VD_SEQNO")));
        } catch (MyException e) {
            throw e;
        }

        /*if(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO")).equals("1446261")){
            System.out.println("매입취소로 처리");
            dataInfo.put("VD_EDISTATUS", "01");
        }*/

        //취소거래는 매입전 취소 또는 MCP 거래(cardtraceno 필요함)만 통신 --> 매입후 취소로 반송 건이 많아 관리 어려움.
        if (transType.equals("0100") || transType.equals("0120") || transType.equals("0160") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184")
                || (transType.equals("0200") && (AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")/* || AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y")*/))) {
            //////////////////////////////////////////////////////////////////////////
            // 요청데이터 구성
            //////////////////////////////////////////////////////////////////////////
            try {
                vanReqBytes = vanData.makeData(Integer.parseInt((String) dataInfo.get("RH00")), vanReq);
            } catch (MyException e) {
                throw e;
            }

            //////////////////////////////////////////////////////////////////////////
            // VAN 호스트 연결
            //////////////////////////////////////////////////////////////////////////
            long startTime = System.currentTimeMillis();
            try {
                vanConn = AbstractApp.g_van.getConnection(VAN_NAME, appWorker);
                appWorker.accessLog(AbstractWorker.CV, (VAN_NAME + "|" + vanConn.getID()).getBytes());
            /*}catch(SocketTimeoutException e){
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Connect]", MyException.SY20, "SocketTimeoutException:"+e, MyException.sysErrMsg);
            }catch(ConnectException e){
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Connect]", MyException.SY20, "ConnectException:"+e, MyException.sysErrMsg);*/
            } catch (MyException e) {
                throw e;
            } catch (Exception e) {
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Connect]", MyException.SY20, "Exception:" + e, MyException.sysErrMsg);
            } finally {
                if (bExcept) {
                    AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
                    appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
                }
            }

            //////////////////////////////////////////////////////////////////////////
            // 요청데이터 전송
            //////////////////////////////////////////////////////////////////////////
            try {
                appWorker.accessLog(AbstractWorker.SV, vanReqBytes);
                //appWorker.accessLog(AbstractWorker.SV);
                vanConn.sendData(vanReqBytes);
            } catch (SocketException e) {
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Send]", MyException.SY22, "SocketException:" + e, MyException.sysErrMsg);
            } catch (Exception e) {
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Send]", MyException.SY22, "Exception:" + e, MyException.sysErrMsg);
            } finally {
                if (bExcept) {
                    AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
                    appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
                }
            }

            //////////////////////////////////////////////////////////////////////////
            // 응답데이터 수신
            //////////////////////////////////////////////////////////////////////////
            try {
                vanResBytes = vanConn.recvData(AppVANClient.TYPE_LEN);

                appWorker.accessLog(AbstractWorker.RV, vanResBytes);
                //appWorker.accessLog(AbstractWorker.RV);
                appWorker.accessLog(AbstractWorker.LG, (transType + " Process Time : " + (System.currentTimeMillis() - startTime) + "ms").getBytes());
            } catch (SocketTimeoutException e) {
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Receive]", MyException.SY23, "SocketTimeoutException:" + e, MyException.sysErrMsg);
            } catch (SocketException e) {
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Receive]", MyException.SY23, "SocketException:" + e, MyException.sysErrMsg);
            } catch (Exception e) {
                bExcept = true;
                throw new MyException("[SMARTROWorker::procVAN:Receive]", MyException.SY23, "Exception:" + e, MyException.sysErrMsg);
            } finally {
                AbstractApp.g_van.closeConnection(VAN_NAME, appWorker, vanConn);
                appWorker.accessLog(AbstractWorker.DV, vanConn.getID().getBytes());
            }

            //////////////////////////////////////////////////////////////////////////
            // 응답데이터 분석
            //////////////////////////////////////////////////////////////////////////
            try {
                vanRes = vanData.parseData(vanResBytes, AppUtil.checkNull(vanReq.get("VS_FIELD01")));
            } catch (MyException e) {
                throw e;
            }
        } else if (transType.equals("0200") && !AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")) {
            //매입 후 취소 (정상취소로 응답)
            vanRes.put("VR_FIELD02", AppUtil.checkNull(dataInfo.get("VR_FIELD02")));
            vanRes.put("VR_FIELD03", AppUtil.checkNull(dataInfo.get("VR_FIELD03")));
            vanRes.put("VR_FIELD04", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));
            vanRes.put("VR_FIELD09", AppUtil.checkNull(dataInfo.get("VR_FIELD09")));
            //vanRes.put("VR_FIELD10", AppUtil.checkNull(dataInfo.get("VR_FIELD10")));
            vanRes.put("VR_FIELD10", "EDI REFUND");
            vanRes.put("VR_FIELD11", AppUtil.checkNull(dataInfo.get("VR_FIELD11")));
            vanRes.put("VR_FIELD12", AppUtil.checkNull(dataInfo.get("VR_FIELD12")));
            vanRes.put("VR_FIELD13", AppUtil.checkNull(dataInfo.get("VR_FIELD13")));
            vanRes.put("VR_FIELD14", AppUtil.checkNull(dataInfo.get("VR_FIELD14")));
            vanRes.put("VR_FIELD15", AppUtil.checkNull(dataInfo.get("VR_FIELD15")));
            vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
        } else if (transType.equals("0400")) {
            //취소완료 거래로 업데이트
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
            vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VD_MID")));
        }

        //////////////////////////////////////////////////////////////////////////
        // 응답데이터 저장
        //////////////////////////////////////////////////////////////////////////
        try {
            //승인취소 실패 시에 field16, 17을 원거래로 셋팅
            if (transType.equals("0200") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")) {
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
            }

            //정상거래로 처리(매입 후 취소로 처리) --> 응답 데이터 저장 시, mcpstatus가 원거래 값 유지되도록
            //if(transType.equals("0200") && !AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O"))
            //    vanRes.put("VR_FIELD03", "O");

            //응답 데이터 저장 (승인취소 거래는 mcpstatus유지되도록 함)
            vanDBProc.updateTransact(vanRes, dataInfo);

            if (transType.equals("0200")) {

                if (AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")) {
                    if (AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")) {
                        //매입 전 취소로 원거래를 "취소된 거래"로 업데이트
                        vanDBProc.updateOrgTransact(dataInfo);
                    }
                } else {
                    //매입 후 취소로 취소거래를 "매입취소"대상으로 업데이트
                    vanDBProc.updateEDICancel(dataInfo);

                    //정상거래로 처리
                    if (!AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O"))
                        vanRes.put("VR_FIELD03", "O");
                }

                /*if(AppUtil.checkNull(dataInfo.get("VD_DCCYN")).equals("Y") || AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y")){
                    if(AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){
                        //원 거래 데이터 업데이트, 매입취소요청(원거래 매입 후, 매입취소 되도록 업데이트 안함)
                        vanDBProc.updateOrgTransact(dataInfo);
                    }else{
                        //매입 후 취소
                        vanDBProc.updateEDICancel(vanRes, dataInfo);
                    }
                }else{
                    vanDBProc.updateEDICancel(vanRes, dataInfo);
                }*/
            } else if (transType.equals("0400")) {
                //환급요청은 매입취소로 업데이트
                vanDBProc.updateEDIRefund(dataInfo);
            }
        } catch (MyException e) {
            throw e;
        }

        //////////////////////////////////////////////////////////////////////////
        // 응답데이터 Copy
        //////////////////////////////////////////////////////////////////////////
        if (AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")) {    //알파벳 대문자 O
            dataInfo.put("VD_RESCODE", "0000");                     //응답코드
        } else {
            dataInfo.put("VD_RESCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());                     //응답코드
            dataInfo.put("VD_RESMSG", AppUtil.checkNull(vanRes.get("VR_FIELD09")).trim() + " " + AppUtil.checkNull(vanRes.get("VR_FIELD10")).trim() + " " + AppUtil.checkNull(vanRes.get("VR_FIELD11")).trim() + " " + AppUtil.checkNull(vanRes.get("VR_FIELD12")).trim());
        }

        dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());            //승인번호
        dataInfo.put("VD_TRADEDT", "20" + AppUtil.checkNull(vanRes.get("VR_FIELD04")));               //거래일자(한국시간 기준)
        dataInfo.put("VD_DCCINFO", AppUtil.checkNull(vanRes.get("VR_FIELD30")));                    //DCC 결제정보
        dataInfo.put("VD_FOREIGNCURRENCY", AppUtil.checkNull(vanRes.get("VR_FIELD20")));            //Foreign Amount
        dataInfo.put("VD_EXCHANGERATE", AppUtil.checkNull(vanRes.get("VR_FIELD21")));               //Exchange Rate
        dataInfo.put("VD_INVERTEDRATE", AppUtil.checkNull(vanRes.get("VR_FIELD22")));               //Inverted Rate
        dataInfo.put("VD_CURRENCYCODE", AppUtil.checkNull(vanRes.get("VR_FIELD23")));               //외환 영문코드
        dataInfo.put("VD_MARKUP", AppUtil.checkNull(vanRes.get("VR_FIELD24")));                     //Mark up percent
        dataInfo.put("VD_COMMISSIONP", AppUtil.checkNull(vanRes.get("VR_FIELD25")));                //Commission Percent
        dataInfo.put("VD_COMMISSIONV", AppUtil.checkNull(vanRes.get("VR_FIELD26")));                //Commission Value
/*System.out.println("VR_FIELD30:"+AppUtil.checkNull(vanRes.get("VR_FIELD30"))+":");
System.out.println("VR_FIELD20:"+AppUtil.checkNull(vanRes.get("VR_FIELD20"))+":");
System.out.println("VR_FIELD21:"+AppUtil.checkNull(vanRes.get("VR_FIELD21"))+":");
System.out.println("VR_FIELD22:"+AppUtil.checkNull(vanRes.get("VR_FIELD22"))+":");
System.out.println("VR_FIELD23:"+AppUtil.checkNull(vanRes.get("VR_FIELD23"))+":");
System.out.println("VR_FIELD24:"+AppUtil.checkNull(vanRes.get("VR_FIELD24"))+":");
System.out.println("VR_FIELD25:"+AppUtil.checkNull(vanRes.get("VR_FIELD25"))+":");
System.out.println("VR_FIELD26:"+AppUtil.checkNull(vanRes.get("VR_FIELD26"))+":");*/
        //}else{
        //    //응답코드 에러
        //    throw new MyException("[SMARTROWorker::procVAN]", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim(), AppUtil.checkNull(dataInfo.get("VD_ERRMSG")), AppUtil.checkNull(dataInfo.get("VD_ERRMSG")));
        //}
    }

    public void procVANTest(Map<String, String> dataInfo) throws MyException {
        System.out.println("[SMARTROWorker::procVANTest]Start");
        transType = AppUtil.checkNull(dataInfo.get("RH03"));

        AppVANClient vanConn = null;

        Map<String, String> vanReq = new HashMap<>();
        Map<String, String>vanRes = new HashMap<>();

        byte[] vanReqBytes = null;
        byte[] vanResBytes = null;

        bExcept = false;

        //////////////////////////////////////////////////////////////////////////
        // VAN 가맹점 아이디, 터미널 아이디, 사업자 번호 추출
        //////////////////////////////////////////////////////////////////////////
        try {
            vanDBProc.selectMerchant(dataInfo);
        } catch (MyException e) {
            throw e;
        }

        //////////////////////////////////////////////////////////////////////////
        // 요청데이터 셋팅 및 저장
        //////////////////////////////////////////////////////////////////////////
        try {
            if (transType.equals("0100") || transType.equals("0120") || transType.equals("0140") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184")) {
                //동일 주문번호 확인, BIN 데이터 확인(해외카드 전용)
                vanDBProc.selectTransact(dataInfo);
            } else if (transType.equals("0200")) {
                //원거래 데이터 추출
                vanReq.put("VS_FIELD05", getCurrency(AppUtil.checkNull(dataInfo.get("RB03"))));
                vanDBProc.selectOrgTransact(vanReq, dataInfo);
            } else if (transType.equals("0125")) {
                //승인일 포함 20일 이내 거래인지 확인(거래구분 및 매입상태 확인) --> 정상인 경우 매입요청으로 변경
                vanDBProc.checkAuthTransact(dataInfo);
                dataInfo.put("VD_RESCODE", "0000");
                dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(dataInfo.get("VR_FIELD13")).trim());            //승인번호
                dataInfo.put("VD_TRADEDT", AppUtil.checkNull(dataInfo.get("VR_FIELD04")));               //거래일자(한국시간 기준)
                return;
            }

            //데이터 포맷팅
            formatData(vanReq, dataInfo);

            //요청 데이터 저장
            vanDBProc.insertTransact(vanReq, dataInfo);
            dataInfo.put("VD_SEQNO", AppUtil.checkNull(vanReq.get("VD_SEQNO")));
        } catch (MyException e) {
            throw e;
        }

        //////////////////////////////////////////////////////////////////////////
        // 응답데이터 저장
        //////////////////////////////////////////////////////////////////////////
        try {
            //처리시간
            //Thread.sleep(2 * 1000);

            if (transType.equals("0100") || transType.equals("0120") || transType.equals("0140") || transType.equals("0180") || transType.equals("0181") || transType.equals("0184")) {
                //테스트카드 번호 체크
                String cardNo = AppUtil.checkNull(dataInfo.get("RB09")).trim();
                String expiryDT = AppUtil.checkNull(dataInfo.get("RB10")).trim();
                String cvv = AppUtil.checkNull(dataInfo.get("RB11")).trim();
                if (cardNo.equals("4434310000000001") || cardNo.equals("5466160000000001") || cardNo.equals("4658580000000001") || cardNo.equals("4773760000000001")
                        || cardNo.equals("5425500000000001") || cardNo.equals("4272290000000001") || cardNo.equals("5520370000000001") || cardNo.equals("5309700000000001")
                        || cardNo.equals("5191230000000001") || cardNo.equals("5131410000000001") || cardNo.equals("4477570000000001")) {

                    if (expiryDT.equals("1501") && cvv.equals("467")) {
                        //승인 결과를 성공으로 고정(for test)
                        vanRes.put("VR_FIELD03", "O");
                        vanRes.put("VR_FIELD13", String.valueOf(Math.random() * 1000000).substring(0, 6));
                    } else {
                        vanRes.put("VR_FIELD03", "X");
                        vanRes.put("VR_FIELD09", "Decline");
                        vanRes.put("VR_FIELD11", "expiry dt:01/15");
                        vanRes.put("VR_FIELD12", "cvv:467");
                    }
                } else {
                    vanRes.put("VR_FIELD03", "X");
                    vanRes.put("VR_FIELD09", "Decline");
                    vanRes.put("VR_FIELD11", "Card number");
                }
            } else {
                //취소 결과를 성공으로 고정(for test)
                vanRes.put("VR_FIELD03", "O");
                vanRes.put("VR_FIELD13", String.valueOf(Math.random() * 1000000).substring(0, 6));
            }
            vanRes.put("VR_FIELD04", AppUtil.getDateTime().substring(2, 14));


            System.out.println("[SMARTROWorker::procVANTest]승인 결과 고정 ==> " + AppUtil.checkNull(vanRes.get("VR_FIELD03")) + "," + AppUtil.checkNull(vanRes.get("VR_FIELD13")));

            //승인취소 실패 시에 field16, 17을 원거래로 셋팅
            if (transType.equals("0200") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")) {
                vanRes.put("VR_FIELD16", AppUtil.checkNull(dataInfo.get("VR_FIELD16")));
                vanRes.put("VR_FIELD17", AppUtil.checkNull(dataInfo.get("VR_FIELD17")));
            }

            //응답 데이터 저장 (승인취소 거래는 mcpstatus유지되도록 함)
            vanDBProc.updateTransact(vanRes, dataInfo);

            if (transType.equals("0200")) {
                if (AppUtil.checkNull(dataInfo.get("VD_EDISTATUS")).equals("00")) {
                    if (AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")) {
                        //매입 전 취소로 원거래를 "취소된 거래"로 업데이트
                        vanDBProc.updateOrgTransact(dataInfo);
                    }
                } else {
                    //매입 후 취소로 취소거래를 "매입취소"대상으로 업데이트
                    vanDBProc.updateEDICancel(dataInfo);

                    //정상거래로 처리
                    if (!AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O"))
                        vanRes.put("VR_FIELD03", "O");
                }

            }
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
        }

        //////////////////////////////////////////////////////////////////////////
        // 응답데이터 Copy
        //////////////////////////////////////////////////////////////////////////
        if (AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")) {    //알파벳 대문자 O
            dataInfo.put("VD_RESCODE", "0000");                     //응답코드
        } else {
            dataInfo.put("VD_RESCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());                     //응답코드
            dataInfo.put("VD_RESMSG", AppUtil.checkNull(vanRes.get("VR_FIELD09")).trim() + " " + AppUtil.checkNull(vanRes.get("VR_FIELD10")).trim() + " " + AppUtil.checkNull(vanRes.get("VR_FIELD11")).trim() + " " + AppUtil.checkNull(vanRes.get("VR_FIELD12")).trim());
        }

        dataInfo.put("VD_AUTHCODE", AppUtil.checkNull(vanRes.get("VR_FIELD13")).trim());            //승인번호
        dataInfo.put("VD_TRADEDT", "20" + AppUtil.checkNull(vanRes.get("VR_FIELD04")));               //거래일자(한국시간 기준)
        dataInfo.put("VD_DCCINFO", AppUtil.checkNull(vanRes.get("VR_FIELD30")));                    //DCC 결제정보
        dataInfo.put("VD_FOREIGNCURRENCY", AppUtil.checkNull(vanRes.get("VR_FIELD20")));            //Foreign Amount
        dataInfo.put("VD_EXCHANGERATE", AppUtil.checkNull(vanRes.get("VR_FIELD21")));               //Exchange Rate
        dataInfo.put("VD_INVERTEDRATE", AppUtil.checkNull(vanRes.get("VR_FIELD22")));               //Inverted Rate
        dataInfo.put("VD_CURRENCYCODE", AppUtil.checkNull(vanRes.get("VR_FIELD23")));               //외환 영문코드
        dataInfo.put("VD_MARKUP", AppUtil.checkNull(vanRes.get("VR_FIELD24")));                     //Mark up percent
        dataInfo.put("VD_COMMISSIONP", AppUtil.checkNull(vanRes.get("VR_FIELD25")));                //Commission Percent
        dataInfo.put("VD_COMMISSIONV", AppUtil.checkNull(vanRes.get("VR_FIELD26")));                //Commission Value

    }

    private void formatData(Map<String, String> vanReq, Map<String, String> dataInfo) throws MyException {

        try {
            String nowDT = AppUtil.getDateTime();

            if (transType.equals("0100") || transType.equals("0120") || transType.equals("0181") || transType.equals("0184") || transType.equals("0200")
                    || transType.equals("0400")) {
                //승인, 취소
                dataInfo.put("VD_MSGTYPE", getMsgType(Integer.parseInt((String) dataInfo.get("RH00"))));           //MsgType

                if (transType.equals("0200") || transType.equals("0400")) {
                    dataInfo.put("VD_STATUS", "T001");

                    if (transType.equals("0400")) {
                        dataInfo.put("VD_ORGTRANSNO", "0");
                    }
                } else if (transType.equals("0120") || transType.equals("0184")) {
                    //KEB Authorization(미매입)
                    dataInfo.put("VD_STATUS", "T003");                                                              //status
                    dataInfo.put("VD_ORGTRANSNO", "0");
                } else {
                    dataInfo.put("VD_STATUS", "T000");                                                              //status
                    dataInfo.put("VD_ORGTRANSNO", "0");                                                             //원거래transno
                }

                //VAN 전문구성===============================================
                //VAN TID
                vanReq.put("VS_FIELD00", AppUtil.checkNull(dataInfo.get("VD_TID")));
                //전문구분
                vanReq.put("VS_FIELD01", getField01(Integer.parseInt((String) dataInfo.get("RH00")), AppUtil.checkNull(dataInfo.get("RB03")), AppUtil.checkNull(dataInfo.get("VD_MCPYN"))));
                //POS Entry Mode
                vanReq.put("VS_FIELD02", "K");
                //카드번호=유효기간
                String trkData = AppUtil.checkNull(dataInfo.get("RB09")).trim() + "=" + AppUtil.checkNull(dataInfo.get("RB10"));
                vanReq.put("VS_FIELD03", AppUtil.formatData(trkData, AppUtil.LEFT, 37, ' '));  //trk data
                //할부개월수
                vanReq.put("VS_FIELD04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부
                if (transType.equals("0181") || transType.equals("0184") || (transType.equals("0200") && AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y"))) {
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
                //통화소숫점자릿수
                if (AppUtil.checkNull(dataInfo.get("RB03")).equals("KRW") || AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY")) {
                    vanReq.put("VS_FIELD06", "0");
                } else {
                    vanReq.put("VS_FIELD06", "2");
                }
                //총금액
                if (AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY")) {
                    //JPY금액 * 100
                    String jpyAmt = AppUtil.checkNull(dataInfo.get("RB04")) + "00";
                    vanReq.put("VS_FIELD07", AppUtil.formatData(jpyAmt, AppUtil.RIGHT, 12, '0'));
                } else {
                    vanReq.put("VS_FIELD07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));
                }

                //봉사료
                vanReq.put("VS_FIELD08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));
                //세금
                vanReq.put("VS_FIELD09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));
                if (transType.equals("0200")) {
                    //승인번호
                    vanReq.put("VS_FIELD10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB51")), AppUtil.LEFT, 12, ' '));
                    //거래일자
                    vanReq.put("VS_FIELD11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB52")), AppUtil.RIGHT, 6, ' '));
                } else {
                    //승인번호
                    vanReq.put("VS_FIELD10", AppUtil.formatData("", AppUtil.LEFT, 12, ' '));
                    //거래일자
                    vanReq.put("VS_FIELD11", AppUtil.formatData("", AppUtil.LEFT, 6, ' '));
                }
                //Working Key Index
                vanReq.put("VS_FIELD12", "AA");
                //비밀번호
                vanReq.put("VS_FIELD13", AppUtil.formatData("", AppUtil.LEFT, 16, ' '));
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
                vanReq.put("VS_FIELD19", AppUtil.formatData("", AppUtil.LEFT, 30, ' '));

                //수표조회
                //VISA && BC 서브몰 사업자 관련 필드 추가 - 2023-07-04 ayaan
                /**
                 * Edgar 수정
                 *   KICC 와 달리 SMARTRO는 우리가 VISA서브롤 전문을 추가하지 않으며
                 *   SMARTRO 자체에서 해당 전문을 BC로 올려주게 됨.
                 *   이에 따라 원복함
                 */
                /*
                if(AppUtil.checkNull(dataInfo.get("RB08")).equals("C000") && AppUtil.checkNull(dataInfo.get("VD_PURCHASECODE")).trim().equals("CD00")) {
                	vanReq.put("VS_FIELD20", AppUtil.formatData("10077007BI", AppUtil.LEFT, 40, ' '));
                }else {
                	vanReq.put("VS_FIELD20", AppUtil.formatData("", AppUtil.LEFT, 40, ' '));
                }
                */
                vanReq.put("VS_FIELD20", AppUtil.formatData("", AppUtil.LEFT, 40, ' '));

                //CVV2
                vanReq.put("VS_FIELD21_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB11")), AppUtil.LEFT, 3, ' '));
                //CVV예비
                if (!AppUtil.checkNull(dataInfo.get("RB11")).trim().equals(""))
                    vanReq.put("VS_FIELD21_2", AppUtil.formatData("CVCDATA", AppUtil.LEFT, 27, ' '));
                else vanReq.put("VS_FIELD21_2", AppUtil.formatData("", AppUtil.LEFT, 27, ' '));
                //선택통화 금액
                vanReq.put("VS_FIELD22_1", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB22")), AppUtil.RIGHT, 12, '0'));
                //선택통화 환율정보
                vanReq.put("VS_FIELD22_2", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB24")), AppUtil.RIGHT, 10, '0'));
                //선택통화 코드
                vanReq.put("VS_FIELD22_3", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB21")), AppUtil.RIGHT, 3, '0'));
                //선택통화 Minor Unit
                vanReq.put("VS_FIELD22_4", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB23")), AppUtil.RIGHT, 1, '0'));
                //BASE통화 코드
                vanReq.put("VS_FIELD22_5", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB25")), AppUtil.RIGHT, 3, '0'));
                //BASE통화 금액
                vanReq.put("VS_FIELD22_6", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 9, '0'));

                if (AppUtil.checkNull(dataInfo.get("RB14")).equals("Y")) {
                    //MPI 거래
                    vanReq.put("VS_FIELD23_1", "M");
                    vanReq.put("VS_FIELD23_2", "C");
                    vanReq.put("VS_FIELD23_3", "N");

                    String cavv = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB15")), AppUtil.LEFT, 40, ' ');
                    String xid = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB16")), AppUtil.LEFT, 40, ' ');
                    String eci = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB18")), AppUtil.LEFT, 2, ' ');
                    vanReq.put("VS_FIELD23_4", cavv + xid + eci);
                } else {
                    //오프라인 거래
                    vanReq.put("VS_FIELD23_1", " ");
                    vanReq.put("VS_FIELD23_2", " ");
                    vanReq.put("VS_FIELD23_3", "N");
                }
                if (transType.equals("0200"))
                    vanReq.put("VD_MCPYN", AppUtil.checkNull(dataInfo.get("VD_MCPYN")));
                else if (transType.equals("0181") || transType.equals("0184"))
                    vanReq.put("VD_MCPYN", "Y");
                else vanReq.put("VD_MCPYN", "N");

            } else
                throw new MyException("[SMARTROWorker::formatData]", MyException.SY21, "Undefined MsgType", MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[SMARTROWorker::formatData]", MyException.SY21, "Exception:" + e, MyException.sysErrMsg);
        }

    }

    private String getMsgType(int format) {
        if (format == AppData.F0200 || format == AppData.F0400) return "1210";
        else return "1130";
    }

    private String getField01(int format, String currency, String mcpYN) {
        switch (format) {
            case AppData.F0100:
            case AppData.F0120: //승인(수동매입)
                if (currency.equals("USD") || currency.equals("KRW")) return "1130";
                else return "8920";      //MCA USD, KRW 이외통화
            case AppData.F0181:                  //MCP 승인
            case AppData.F0184:                  //MCP Auth
                if (currency.equals("USD")) return "8850";
                else if (currency.equals("JPY")) return "8870";
                else return "8820";
            case AppData.F0200:                  //취소
            case AppData.F0400:
                if ((format == AppData.F0200 && mcpYN.equals("N")) || format == AppData.F0400) {
                    if (currency.equals("USD") || currency.equals("KRW")) return "1210";
                    else return "8930";  //MCA USD, KRW 이외통화
                } else {
                    if (currency.equals("USD")) return "8860";
                    else if (currency.equals("JPY")) return "8880";
                    else return "8830";
                }
        }

        return "    ";
    }

    private String getCurrency(String currency) {
        if (currency.equals("KRW")) return "1";
        else if (currency.equals("USD")) return "2";
        else if (currency.equals("JPY")) return "3";
        else if (currency.equals("EUR")) return "4";
        else if (currency.equals("HKD")) return "5";
        else if (currency.equals("GBP")) return "6";
        else if (currency.equals("SGD")) return "7";
        else if (currency.equals("AUD")) return "8";
        else if (currency.equals("THB")) return "9";
        else if (currency.equals("CAD")) return "A";
        else if (currency.equals("RUB")) return "B";
        else if (currency.equals("CNY")) return "C";

        return "   ";
    }
}

