package api.server.gateway.legacy.v100;

import api.server.common.helper.AppUtil;
import api.server.gateway.legacy.AbstractApp;
import api.server.gateway.legacy.AbstractVer;
import api.server.gateway.legacy.AppData;
import api.server.gateway.legacy.my.MyException;
import api.server.gateway.legacy.van.AppVAN;
import api.server.gateway.legacy.van.kicc.KICCWorker;

import java.util.Map;

/************************************************
 *
 * @author 케이알파트너스
 *
 */
public class VerWorker extends AbstractVer {
    private VerDBProc dbproc;
    private VerData verData;

    public VerWorker() {
        verData = new VerData();
    }

    public void setAppParameter() {
        appWorker.appData = (AppData) verData;
        dbproc = new VerDBProc(AbstractApp.g_dbcm);
    }

    @Override
    protected void procTransaction() throws MyException {

    }

    /* (non-Javadoc)
     * @see com.mcpay.app.ExtractWorker#runTransaction(java.io.InputStream, java.io.OutputStream)
     */
    public void procTransaction(Map<String, String> dataInfo) throws MyException {
        //////////////////////////////////////////////////////////////////////////
        // 전문코드별 처리
        //////////////////////////////////////////////////////////////////////////
        int format = Integer.parseInt(dataInfo.get("RH00"));
        try {
            switch (format) {
                case AppData.F0100:     //신용카드 승인
                case AppData.F0101:     //신용카드 승인(PCINS)
                case AppData.F0102:     ///BC UPOT, HanaUPOP, LotteUPOP, SSUPOP 신용카드 승인(자체 저장)
                case AppData.F0122:     ///BC UPOT, HanaUPOP, LotteUPOP, SSUPOP 신용카드 승인(자체 저장) - 수동매입
                case AppData.F0120:     //신용카드 인증(수동매입)
                case AppData.F0121:     //신용카드 인증(PCINS)
                case AppData.F0140:     //전화승인
                case AppData.F0400:     //일반 환급요청
                    //레거시 분석 5단계
                    procF0100(dataInfo);
                    break;
                case AppData.F0125:     //Capture
                case AppData.F0126:     //Capture(PCINS)
                    procF0125(dataInfo);
                    break;
                case AppData.F0160:     //DCC 환율조회
                case AppData.F0180:     //DCC 승인요청
                    procF0160(dataInfo);
                    break;
                case AppData.F0181:     //MCP 승인요청
                case AppData.F0182:     //MCP 승인요청(PCINS)
                case AppData.F0183:     //MCP 인증요청(PCINS)
                case AppData.F0184:     //MCP 인증요청
                    procF0181(dataInfo);
                    break;
                case AppData.F0200:     //신용카드 취소
                case AppData.F0201:     //신용카드 취소(PCINS)
                case AppData.F0202:     //BC UPOT, HanaUPOP, LotteUPOP, SSUPOP 취소(자체 취소)
                case AppData.F0204:     //부분취소
                case AppData.F0206:     //BC UPOT, HanaUPOP, LotteUPOP, SSUPOP부분취소
                    procF0200(dataInfo);
                    break;
                case AppData.F9999:     //Edgar 부하테스트 (2023.03.15)
                    procF9999(dataInfo);
                    break;
            }
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("[VerWorker::runWorker:procData]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        }

    }

    //신용카드 승인
    private void procF0100(Map<String, String> dataInfo) throws MyException {
        try {
            //////////////////////////////////////////////////////////////////////////
            // 거래 유효성 확인 및 transact_web, transact 처리
            //////////////////////////////////////////////////////////////////////////
            int format = Integer.parseInt((String) dataInfo.get("RH00"));
            if (format == AppData.F0101 || format == AppData.F0121) {
                //필수 데이터 확인
                String cardtype = AppUtil.checkNull(dataInfo.get("RB08")).trim();
                if (AppUtil.checkNull(dataInfo.get("RB03")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0100]", "W100", "invalid cur", "invalid cur");
                else if (AppUtil.checkNull(dataInfo.get("RB90")).trim().equals("") || AppUtil.checkNullDouble(dataInfo.get("RB90")) <= 0)
                    throw new MyException("[VerWorker::procF0100]", "W101", "invalid amt", "invalid amt");
                else if (AppUtil.checkNull(dataInfo.get("RB90")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0100]", "W102", "invalid ref", "invalid ref");
                else if (AppUtil.checkNull(dataInfo.get("RB92")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0100]", "W110", "invalid buyer", "invalid buyer");
                else if (cardtype.equals("") || (!cardtype.equals("C000") && !cardtype.equals("C001") && !cardtype.equals("C002") && !cardtype.equals("C003") && !cardtype.equals("C026")))
                    throw new MyException("[VerWorker::procF0100]", "W121", "invalid cardtype", "invalid cardtype");
                else if (AppUtil.checkNull(dataInfo.get("RB09")).trim().length() < 12)
                    throw new MyException("[VerWorker::procF0100]", "W122", "invalid card number", "invalid card number");
                else if (AppUtil.checkNull(dataInfo.get("RB10")).trim().length() != 4)
                    throw new MyException("[VerWorker::procF0100]", "W123", "invalid expiry date", "invalid expiry date");
                else if (AppUtil.checkNull(dataInfo.get("RB01")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0100]", "W129", "invalid mid", "invalid mid");

                //허용 가맹점 IP 체크

                System.out.println("[VerWorker::procF0100]허용 가맹점 IP 체크 완료");
                //승인금액
                String cur = AppUtil.checkNull(dataInfo.get("RB03")).trim();
                double amt = AppUtil.checkNullDouble(dataInfo.get("RB04"));
                if (!cur.equals("KRW") && !cur.equals("JPY") && !cur.equals("THB")) amt = amt / 100;
                dataInfo.put("VD_AMT", String.valueOf(amt));
                System.out.println("[VerWorker::procF0100]승인금액 : " + amt);
                //결제요청 데이터 유효성 확인 및 부가 데이터 추출
                dbproc.checkRequest(dataInfo);
                System.out.println("[VerWorker::procF0100]결제요청 데이터 유효성 확인 및 부가 데이터 추출 완료");
                //요청 정보 저장(transact_web)
                dbproc.reqTransactWeb(dataInfo);
                System.out.println("[VerWorker::procF0100]요청 정보 저장(transact_web)");
                //checkApprvCurrency

                dbproc.checkApprvCurrency(dataInfo);
                System.out.println("[VerWorker::procF0100]checkApprvCurrency");
                System.out.println("[VerWorker::procF0100]한국발급카드 : " + dataInfo.get("VD_ISSUEDKR"));
                //한국발급카드 거절
                if (AppUtil.checkNull(dataInfo.get("VD_ISSUEDKR")).equals("Y"))
                    throw new MyException("[VerWorker::procF0100]", "S021", "issued in KR ", "issued in KR");
                System.out.println("[VerWorker::procF0100]최소금액 : " + dataInfo.get("VD_MINAMOUNT"));
                //결제수단별 최소금액 확인
                if (AppUtil.checkNullDouble(dataInfo.get("VD_MINAMOUNT")) > amt)
                    throw new MyException("[VerWorker::procF0100]", "W203", "invalid amt", "invalid amt");
                System.out.println("[VerWorker::procF0100]요청/승인통화 : " + cur + "/" + dataInfo.get("VD_APPRVCUR"));
                //요청통화와 승인통화는 반드시 같아야 함
                if (!AppUtil.checkNull(dataInfo.get("VD_APPRVCUR")).equals(cur))
                    throw new MyException("[VerWorker::procF0100]", "W204", "invalid cur", "invalid cur");

                //요청 정보 저장(transact)
                if (format == AppData.F0121) dataInfo.put("VD_TRANSTYPE", "T003");
                else dataInfo.put("VD_TRANSTYPE", "T000");

                dataInfo.put("VD_TRADETYPE", "T102");

                dbproc.prepareReqTranact(dataInfo);
                dbproc.reqTransact(dataInfo);
                System.out.println("[VerWorker::procF0100]요청 정보 저장(transact) 완료");

                //일반 전문 format으로 변경
                if (format == AppData.F0101) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0100));
                    dataInfo.put("RH03", "0100");
                } else if (format == AppData.F0121) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0120));
                    dataInfo.put("RH03", "0120");
                }
            }

            if (format == AppData.F0102) {
                if (AppUtil.checkNull(dataInfo.get("RB72")).equals("04")) {// 삼성UPOP의 경우
                    if (AppUtil.checkNull(dataInfo.get("RB73")).trim().equals(""))
                        throw new MyException("[VerWorker::procF0100]", "W130", "invalid traceno", "invalid traceno");
                }
            }
            //////////////////////////////////////////////////////////////////////////
            // VAN 객체 생성
            //////////////////////////////////////////////////////////////////////////
            //레거시 분석 6단계
            AppVAN appVan = createVAN(dataInfo);

            //////////////////////////////////////////////////////////////////////////
            // VAN 거래 요청(거래 저장)
            //////////////////////////////////////////////////////////////////////////
            // String m_run = AppUtil.checkNull(AbstractApp.g_appCfg.getServerRun("EXIMGW"));
            String m_run = "REAL";

            //2022.04.22 KICC 개발기 및 테스트기 통신 고날 mid 분기 추가 walter.
            System.out.println("[VerWorker::procF0100] RB01 : " + AppUtil.checkNull(dataInfo.get("RB01")));
            //테스트 환경 티웨이 직가맹점 매입 데스트를 위해 KICC 승인 요청 되도록 분기 처리(1686C232C3,2A15B0AB0D,18A4FDFD8B)  2024.07.30 walter
            if (m_run.equals("TEST") && ("1FD7571895".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "2367AC3B96".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "1E9EAF4881".equals(AppUtil.checkNull(dataInfo.get("RB01")))
                    || "1686C232C3".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "2A15B0AB0D".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "18A4FDFD8B".equals(AppUtil.checkNull(dataInfo.get("RB01"))))) {
                m_run = "REAL";
            }

            if (m_run.equals("REAL")) {
                //레거시 분석 7단계
                appVan.procVAN(dataInfo);
            } else {
                appVan.procVANTest(dataInfo);
            }

            //////////////////////////////////////////////////////////////////////////
            // 거래 결과 transact_web, transact 저장
            //////////////////////////////////////////////////////////////////////////
            if (format == AppData.F0101 || format == AppData.F0121) {
                //PCINS 전문 format으로 변경
                if (format == AppData.F0101) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0101));
                    dataInfo.put("RH03", "0101");
                } else if (format == AppData.F0121) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0121));
                    dataInfo.put("RH03", "0121");
                }

                //요청 정보 저장(transact)
                dbproc.resTransact(dataInfo);

                if (format == AppData.F0101 && AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                    //매입 저장(settlement)
                    dbproc.insertSettlement(dataInfo);
                }

                //요청 정보 저장(transact_web)
                dbproc.resTransactWeb(dataInfo);


            }

            //////////////////////////////////////////////////////////////////////////
            // 응답전문 포맷팅
            //////////////////////////////////////////////////////////////////////////
            dataInfo.put("RH04", dataInfo.get("VD_RESCODE"));

            System.out.println("[VerWorker::procF0100]vd_rescode : " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
            if (!AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                //String resmsg = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' ') + AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                String resmsg = AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                System.out.println("[VerWorker::procF0100]vd_resmsg : " + resmsg);
                throw new MyException("[VerWorker::procF0100]", AppUtil.checkNull(dataInfo.get("VD_RESCODE")), resmsg, resmsg, AppUtil.checkNull(dataInfo.get("VD_SEQNO")));
            }

            dataInfo.put("SB01", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB01")), AppUtil.LEFT, 10, ' '));   //가맹점아이디
            dataInfo.put("SB02", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB02")), AppUtil.LEFT, 24, ' '));   //PG 일련번호
            dataInfo.put("SB03", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB03")), AppUtil.LEFT, 3, ' '));    //통화코드
            dataInfo.put("SB04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));  //거래금액
            dataInfo.put("SB05", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));  //봉사료
            dataInfo.put("SB06", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));  //세금
            dataInfo.put("SB07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부기간
            dataInfo.put("SB08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB08")), AppUtil.LEFT, 4, ' '));    //카드구분
            dataInfo.put("SB09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB09")), AppUtil.LEFT, 20, ' '));   //카드번호
            dataInfo.put("SB10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB10")), AppUtil.LEFT, 4, ' '));    //유효기간
            dataInfo.put("SB11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB12")), AppUtil.LEFT, 20, ' '));   //Holder
            dataInfo.put("SB12", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB13")), AppUtil.LEFT, 2, ' '));    //국가코드
            dataInfo.put("SB13", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")), AppUtil.LEFT, 12, ' '));   //승인번호
            dataInfo.put("SB14", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_TRADEDT_AUTH")), AppUtil.LEFT, 14, ' '));   //거래일시
            dataInfo.put("SB15", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' '));   //SeqNo

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("[VerWorker::procF0100]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        }
    }

    //Capture
    private void procF0125(Map<String, String> dataInfo) throws MyException {
        try {
            //////////////////////////////////////////////////////////////////////////
            // 거래 유효성 확인 및 transact_web, transact 처리
            //////////////////////////////////////////////////////////////////////////
            int format = Integer.parseInt((String) dataInfo.get("RH00"));
            if (format == AppData.F0126) {
                //필수 데이터 확인
                String cardtype = AppUtil.checkNull(dataInfo.get("RB08")).trim();
                if (AppUtil.checkNull(dataInfo.get("RB03")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0125]", "W100", "invalid cur", "invalid cur");
                else if (AppUtil.checkNull(dataInfo.get("RB90")).trim().equals("") || AppUtil.checkNullDouble(dataInfo.get("RB90")) <= 0)
                    throw new MyException("[VerWorker::procF0125]", "W101", "invalid amt", "invalid amt");
                else if (AppUtil.checkNull(dataInfo.get("RB90")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0125]", "W102", "invalid ref", "invalid ref");
                else if (AppUtil.checkNull(dataInfo.get("RB01")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0125]", "W129", "invalid mid", "invalid mid");

                //허용 가맹점 IP 체크

                System.out.println("[VerWorker::procF0125]허용 가맹점 IP 체크 완료");
                //승인금액
                String cur = AppUtil.checkNull(dataInfo.get("RB03")).trim();
                double amt = AppUtil.checkNullDouble(dataInfo.get("RB04"));
                if (!cur.equals("KRW") && !cur.equals("JPY") && !cur.equals("THB")) amt = amt / 100;
                dataInfo.put("VD_AMT", String.valueOf(amt));


                //데이터 유효성, 원거래 확인
                dbproc.checkCaptureRequest(dataInfo);
                System.out.println("[VerWorker::procF0125]Capture 데이터 유효성, 원거래 확인");

                System.out.println("[VerWorker::procF0125]승인금액 : " + amt);
                //결제요청 데이터 유효성 확인 및 부가 데이터 추출
                dbproc.checkRequest(dataInfo);
                System.out.println("[VerWorker::procF0125]결제요청 데이터 유효성 확인 및 부가 데이터 추출 완료");
                //요청 정보 저장(transact_web)
                //dbproc.reqTransactWeb(dataInfo);
                System.out.println("[VerWorker::procF0125]요청 정보 저장(transact_web)");
                //checkApprvCurrency
                dbproc.checkApprvCurrency(dataInfo);
                System.out.println("[VerWorker::procF0125]checkApprvCurrency");
//System.out.println("[VerWorker::procF0100]한국발급카드 : " + dataInfo.get("VD_ISSUEDKR"));
//                //한국발급카드 거절
//                if(AppUtil.checkNull(dataInfo.get("VD_ISSUEDKR")).equals("Y"))
//                    throw new MyException("[VerWorker::procF0100]", "S021", "issued in KR ", "issued in KR");
//System.out.println("[VerWorker::procF0100]최소금액 : " + dataInfo.get("VD_MINAMOUNT"));
//                //결제수단별 최소금액 확인
//                if(AppUtil.checkNullDouble(dataInfo.get("VD_MINAMOUNT")) > amt)
//                    throw new MyException("[VerWorker::procF0100]", "W203", "invalid amt", "invalid amt");
//System.out.println("[VerWorker::procF0100]요청/승인통화 : " + cur + "/" + dataInfo.get("VD_APPRVCUR"));
//                //요청통화와 승인통화는 반드시 같아야 함
//                if(!AppUtil.checkNull(dataInfo.get("VD_APPRVCUR")).equals(cur))
//                    throw new MyException("[VerWorker::procF0100]", "W204", "invalid cur", "invalid cur");
//
                //요청 정보 저장(transact)
                dataInfo.put("VD_TRANSTYPE", "T004");

                dataInfo.put("VD_TRADETYPE", "T102");

                //transid가 새로 생성됨. --> EXIMGW를 위해 원 거래 transid 유지
                //dbproc.reqTransact(dataInfo);
                dbproc.prepareReqTranact(dataInfo);

                System.out.println("[VerWorker::procF0125]요청 정보 저장(transact) 완료");

                //일반 전문 format으로 변경
                dataInfo.put("RH00", String.valueOf(AppData.F0125));
                dataInfo.put("RH03", "0125");

            }

            //////////////////////////////////////////////////////////////////////////
            // VAN 객체 생성
            //////////////////////////////////////////////////////////////////////////
            AppVAN appVan = createVAN(dataInfo);

            //////////////////////////////////////////////////////////////////////////
            // VAN 거래 요청(거래 저장)
            //////////////////////////////////////////////////////////////////////////
            /*if(format == AppData.F0101 || format == AppData.F0121){
                //테스트 승인
                appVan.procVANTest(dataInfo);
            }else{*/
            appVan.procVAN(dataInfo);
            //}

            //////////////////////////////////////////////////////////////////////////
            // 거래 결과 transact_web, transact 저장
            //////////////////////////////////////////////////////////////////////////
            if (format == AppData.F0126) {
                //PCINS 전문 format으로 변경
                dataInfo.put("RH00", String.valueOf(AppData.F0126));
                dataInfo.put("RH03", "0126");

                //요청 정보 저장(transact)
                dbproc.resTransact(dataInfo);

                if (AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                    //매입 저장(settlement)
                    dbproc.insertSettlement(dataInfo);

                    //원 Authorization거래 상태를 "Capture"로 변경
                    dbproc.updateAuthToCapture(dataInfo);
                }

                //요청 정보 저장(transact_web)
                //dbproc.resTransactWeb(dataInfo);


            }

            //////////////////////////////////////////////////////////////////////////
            // 응답전문 포맷팅
            //////////////////////////////////////////////////////////////////////////
            dataInfo.put("RH04", dataInfo.get("VD_RESCODE"));

            System.out.println("[VerWorker::procF0125]vd_rescode : " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
            if (!AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                //String resmsg = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' ') + AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                String resmsg = AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                System.out.println("[VerWorker::procF0125]vd_resmsg : " + resmsg);
                throw new MyException("[VerWorker::procF0100]", AppUtil.checkNull(dataInfo.get("VD_RESCODE")), resmsg, resmsg, AppUtil.checkNull(dataInfo.get("VD_SEQNO")));
            }

            dataInfo.put("SB01", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB01")), AppUtil.LEFT, 10, ' '));   //가맹점아이디
            dataInfo.put("SB02", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB02")), AppUtil.LEFT, 24, ' '));   //PG 일련번호
            dataInfo.put("SB03", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB03")), AppUtil.LEFT, 3, ' '));    //통화코드
            dataInfo.put("SB04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));  //거래금액
            dataInfo.put("SB05", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));  //봉사료
            dataInfo.put("SB06", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));  //세금
            dataInfo.put("SB07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부기간
            dataInfo.put("SB08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")), AppUtil.LEFT, 12, ' '));   //승인번호
            dataInfo.put("SB09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_TRADEDT")), AppUtil.LEFT, 14, ' '));   //거래일시
            dataInfo.put("SB10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' '));   //SeqNo

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("[VerWorker::procF0125]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        }
    }

    //DCC 환율조회/승인요청
    private void procF0160(Map<String, String> dataInfo) throws MyException {
        try {
            //////////////////////////////////////////////////////////////////////////
            // VAN 객체 생성
            //////////////////////////////////////////////////////////////////////////
            //AppVAN appVan = createVAN(dataInfo);
            AppVAN appVan = new KICCWorker(appWorker);

            //////////////////////////////////////////////////////////////////////////
            // VAN 거래 요청(거래 저장)
            //////////////////////////////////////////////////////////////////////////
            String m_run = AppUtil.checkNull(AbstractApp.g_appCfg.getServerRun("EXIMGW"));
            if (m_run.equals("REAL")) {
                appVan.procVAN(dataInfo);
            } else {
                appVan.procVANTest(dataInfo);
            }

            //////////////////////////////////////////////////////////////////////////
            // 응답전문 포맷팅
            //////////////////////////////////////////////////////////////////////////
            dataInfo.put("RH04", dataInfo.get("VD_RESCODE"));

            System.out.println("[VerWorker::procF0160]vd_rescode : " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
            if (!AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                //String resmsg = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' ') + AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                String resmsg = AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                System.out.println("[VerWorker::procF0160]vd_resmsg : " + resmsg);
                throw new MyException("[VerWorker::procF0160]", AppUtil.checkNull(dataInfo.get("VD_RESCODE")), resmsg, resmsg, AppUtil.checkNull(dataInfo.get("VD_SEQNO")));
            }

            dataInfo.put("SB01", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB01")), AppUtil.LEFT, 10, ' '));   //가맹점아이디
            dataInfo.put("SB02", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB02")), AppUtil.LEFT, 24, ' '));   //PG 일련번호
            dataInfo.put("SB03", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB03")), AppUtil.LEFT, 3, ' '));    //통화코드
            dataInfo.put("SB04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));  //거래금액
            dataInfo.put("SB05", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));  //봉사료
            dataInfo.put("SB06", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));  //세금
            dataInfo.put("SB07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부기간
            dataInfo.put("SB08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB08")), AppUtil.LEFT, 4, ' '));    //카드구분
            dataInfo.put("SB09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB09")), AppUtil.LEFT, 20, ' '));   //카드번호
            dataInfo.put("SB10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB10")), AppUtil.LEFT, 4, ' '));    //유효기간
            dataInfo.put("SB11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB12")), AppUtil.LEFT, 20, ' '));   //Holder
            dataInfo.put("SB12", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB13")), AppUtil.LEFT, 2, ' '));    //국가코드
            dataInfo.put("SB13", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")), AppUtil.LEFT, 12, ' '));            //승인번호
            dataInfo.put("SB14", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_TRADEDT_AUTH")), AppUtil.LEFT, 14, ' '));             //거래일시
            dataInfo.put("SB15", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' '));               //SeqNo
            dataInfo.put("SB16", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_DCCINFO")), AppUtil.LEFT, 25, ' '));             //DCC Info
            dataInfo.put("SB17", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_FOREIGNCURRENCY")), AppUtil.LEFT, 16, ' '));     //Foreign Currency Number
            dataInfo.put("SB18", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_EXCHANGERATE")), AppUtil.LEFT, 10, ' '));        //Exchange Rate
            dataInfo.put("SB19", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_INVERTEDRATE")), AppUtil.LEFT, 11, ' '));        //Inverted Rate
            dataInfo.put("SB20", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_CURRENCYCODE")), AppUtil.LEFT, 3, ' '));         //외환영문코드
            dataInfo.put("SB21", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_MARKUP")), AppUtil.LEFT, 9, ' '));               //Mark up percent
            dataInfo.put("SB22", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_COMMISSIONP")), AppUtil.LEFT, 9, ' '));          //Commission Percent
            dataInfo.put("SB23", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_COMMISSIONV")), AppUtil.LEFT, 16, ' '));         //Commission Value
            dataInfo.put("SB24", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_KICCTID")), AppUtil.LEFT, 15, ' '));             //Terminal ID(receipt)
            dataInfo.put("SB25", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_CARDMID")), AppUtil.LEFT, 20, ' '));             //Merchannt No(receipt)

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("[VerWorker::procF0160]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        }
    }

    //MCP 승인요청
    private void procF0181(Map<String, String> dataInfo) throws MyException {
        try {
            int format = Integer.parseInt((String) dataInfo.get("RH00"));
            if (format == AppData.F0182 || format == AppData.F0183) {
                //필수 데이터 확인
                String cardtype = AppUtil.checkNull(dataInfo.get("RB08")).trim();
                if (AppUtil.checkNull(dataInfo.get("RB03")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0181]", "W100", "invalid cur", "invalid cur");
                else if (AppUtil.checkNull(dataInfo.get("RB90")).trim().equals("") || AppUtil.checkNullDouble(dataInfo.get("RB90")) <= 0)
                    throw new MyException("[VerWorker::procF0181]", "W101", "invalid amt", "invalid amt");
                else if (AppUtil.checkNull(dataInfo.get("RB90")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0181]", "W102", "invalid ref", "invalid ref");
                else if (AppUtil.checkNull(dataInfo.get("RB92")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0181]", "W110", "invalid buyer", "invalid buyer");
                else if (cardtype.equals("") || (!cardtype.equals("C000") && !cardtype.equals("C001") && !cardtype.equals("C002") && !cardtype.equals("C003")))
                    throw new MyException("[VerWorker::procF0181]", "W121", "invalid cardtype", "invalid cardtype");
                else if (AppUtil.checkNull(dataInfo.get("RB09")).trim().length() < 12)
                    throw new MyException("[VerWorker::procF0181]", "W122", "invalid card number", "invalid card number");
                else if (AppUtil.checkNull(dataInfo.get("RB10")).trim().length() != 4)
                    throw new MyException("[VerWorker::procF0181]", "W123", "invalid expiry date", "invalid expiry date");
                else if (AppUtil.checkNull(dataInfo.get("RB01")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0181]", "W129", "invalid mid", "invalid mid");
                else if (AppUtil.checkNull(dataInfo.get("RB21")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0181]", "W130", "invalid quote currency", "invalid quote currency");
                else if (AppUtil.checkNull(dataInfo.get("RB22")).trim().equals("") || AppUtil.checkNullDouble(dataInfo.get("RB22")) <= 0)
                    throw new MyException("[VerWorker::procF0181]", "W131", "invalid quote amount", "invalid quote amount");

                //허용 가맹점 IP 체크

                System.out.println("[VerWorker::procF0182]허용 가맹점 IP 체크 완료");
                //승인금액
                String cur = AppUtil.checkNull(dataInfo.get("RB03")).trim();
                double amt = AppUtil.checkNullDouble(dataInfo.get("RB04"));
                if (!cur.equals("KRW") && !cur.equals("JPY") && !cur.equals("THB")) amt = amt / 100;
                dataInfo.put("VD_AMT", String.valueOf(amt));
                System.out.println("[VerWorker::procF0181]승인금액 : " + amt);

                //결제요청 데이터 유효성 확인 및 부가 데이터 추출
                dbproc.checkRequest(dataInfo);
                System.out.println("[VerWorker::procF0181]결제요청 데이터 유효성 확인 및 부가 데이터 추출 완료");
                //MCP사용 가맹점 아이디 확인
                if (!AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y"))
                    throw new MyException("[VerWorker::procF0181]", "W132", "not mcp merchant", "not mcp merchant");

                //요청 정보 저장(transact_web)
                dbproc.reqTransactWeb(dataInfo);
                System.out.println("[VerWorker::procF0181]요청 정보 저장(transact_web)");

                //RateID 및 Quote 금액 유효성 확인
                dbproc.checkMCP(dataInfo);
                System.out.println("[VerWorker::procF0181]RateID 및 Quote 금액 유효성 확인(checkMCP) : " + dataInfo.get("RB26"));

                //checkApprvCurrency
                dbproc.checkApprvCurrency(dataInfo);
                System.out.println("[VerWorker::procF0181]checkApprvCurrency:" + dataInfo.get("VD_MCPYN"));

                System.out.println("[VerWorker::procF0181]한국발급카드 : " + dataInfo.get("VD_ISSUEDKR"));
                //2012-10-19 MPI 테스트용
                //한국발급카드 거절
                if (AppUtil.checkNull(dataInfo.get("VD_ISSUEDKR")).equals("Y"))
                    throw new MyException("[VerWorker::procF0100]", "S021", "issued in KR ", "issued in KR");
                System.out.println("[VerWorker::procF0181]최소금액 : " + dataInfo.get("VD_MINAMOUNT"));
                //결제수단별 최소금액 확인
                if (AppUtil.checkNullDouble(dataInfo.get("VD_MINAMOUNT")) > amt)
                    throw new MyException("[VerWorker::procF0100]", "W203", "invalid amt", "invalid amt");
                System.out.println("[VerWorker::procF0181]요청/승인통화 : " + cur + "/" + dataInfo.get("VD_APPRVCUR"));
                //요청통화와 승인통화는 반드시 같아야 함
                if (!AppUtil.checkNull(dataInfo.get("VD_APPRVCUR")).equals(cur))
                    throw new MyException("[VerWorker::procF0181]", "W204", "invalid cur", "invalid cur");

                //요청 정보 저장(transact)
                if (format == AppData.F0183) dataInfo.put("VD_TRANSTYPE", "T003");
                else dataInfo.put("VD_TRANSTYPE", "T000");

                dataInfo.put("VD_TRADETYPE", "T102");

                dbproc.prepareReqTranact(dataInfo);
                dbproc.reqTransact(dataInfo);
                System.out.println("[VerWorker::procF0181]요청 정보 저장(transact) 완료");

                dbproc.insertMCPTransact(dataInfo);
                System.out.println("[VerWorker::procF0181]MCP 거래정보 저장 완료");

                //일반 전문 format으로 변경
                if (format == AppData.F0182) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0181));
                    dataInfo.put("RH03", "0181");
                    //2012-10-19 MPI 테스트용
                    //dataInfo.put("RH00", String.valueOf(AppData.F0100));
                    //dataInfo.put("RH03", "0100");
                } else if (format == AppData.F0183) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0184));
                    dataInfo.put("RH03", "0184");
                }
            }

            //////////////////////////////////////////////////////////////////////////
            // VAN 객체 생성
            //////////////////////////////////////////////////////////////////////////
            //AppVAN appVan = createVAN(dataInfo);

            AppVAN appVan = createVAN(dataInfo);
            //2012-10-19 MPI 테스트용
            //AppVAN appVan = new com.eximbay.van.payvision.PayvisionWorker(appWorker);

            //////////////////////////////////////////////////////////////////////////
            // VAN 거래 요청(거래 저장)
            //////////////////////////////////////////////////////////////////////////
            //테스트 환경 티웨이 직가맹점 매입 데스트를 위해 KICC 승인 요청 되도록 분기 처리(1686C232C3,2A15B0AB0D,18A4FDFD8B)  2024.07.30 walter
            String m_run = AppUtil.checkNull(AbstractApp.g_appCfg.getServerRun("EXIMGW"));
            if (m_run.equals("TEST") && ("1FD7571895".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "2367AC3B96".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "1E9EAF4881".equals(AppUtil.checkNull(dataInfo.get("RB01")))
                    || "1686C232C3".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "2A15B0AB0D".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "18A4FDFD8B".equals(AppUtil.checkNull(dataInfo.get("RB01"))))) {
                m_run = "REAL";
            }

            if (m_run.equals("REAL")) {
                appVan.procVAN(dataInfo);
            } else {
                appVan.procVANTest(dataInfo);
            }


            //////////////////////////////////////////////////////////////////////////
            // 거래 결과 transact_web, transact 저장
            //////////////////////////////////////////////////////////////////////////
            if (format == AppData.F0182 || format == AppData.F0183) {
                //PCINS 전문 format으로 변경
                if (format == AppData.F0182) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0182));
                    dataInfo.put("RH03", "0182");
                } else if (format == AppData.F0183) {
                    dataInfo.put("RH00", String.valueOf(AppData.F0183));
                    dataInfo.put("RH03", "0183");
                }

//System.out.println("[VerWorker::procF0181]원 승인 결과 ==> " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
//                //승인 결과를 성공으로 고정(for test)
//                dataInfo.put("VD_RESCODE", "0000");
//                dataInfo.put("VD_AUTHCODE", String.valueOf(Math.random() * 1000000).substring(0, 6));
//System.out.println("[VerWorker::procF0181]승인 결과 고정 ==> " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")) + "," + AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")));

                //결과 정보 저장(transact)
                dbproc.resTransact(dataInfo);
                System.out.println("[VerWorker::procF0181]결과 정보 저장(transact)");
                if (format == AppData.F0182 && AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                    //매입 저장(settlement)
                    dbproc.insertSettlement(dataInfo);
                    System.out.println("[VerWorker::procF0181]매입 저장(settlement)");
                }

                //결과 정보 저장(transact_web)
                dbproc.resTransactWeb(dataInfo);
                System.out.println("[VerWorker::procF0181]결과 정보 저장(transact_web)");
            }

            //////////////////////////////////////////////////////////////////////////
            // 응답전문 포맷팅
            //////////////////////////////////////////////////////////////////////////
            dataInfo.put("RH04", dataInfo.get("VD_RESCODE"));

            System.out.println("[VerWorker::procF0181]vd_rescode : " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
            if (!AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                //String resmsg = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' ') + AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                String resmsg = AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                System.out.println("[VerWorker::procF0182]vd_resmsg : " + resmsg);
                throw new MyException("[VerWorker::procF0181]", AppUtil.checkNull(dataInfo.get("VD_RESCODE")), resmsg, resmsg, AppUtil.checkNull(dataInfo.get("VD_SEQNO")));
            }

            dataInfo.put("SB01", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB01")), AppUtil.LEFT, 10, ' '));   //가맹점아이디
            dataInfo.put("SB02", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB02")), AppUtil.LEFT, 24, ' '));   //PG 일련번호
            dataInfo.put("SB03", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB03")), AppUtil.LEFT, 3, ' '));    //통화코드
            dataInfo.put("SB04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));  //거래금액
            dataInfo.put("SB05", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));  //봉사료
            dataInfo.put("SB06", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));  //세금
            dataInfo.put("SB07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부기간
            dataInfo.put("SB08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB08")), AppUtil.LEFT, 4, ' '));    //카드구분
            dataInfo.put("SB09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB09")), AppUtil.LEFT, 20, ' '));   //카드번호
            dataInfo.put("SB10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB10")), AppUtil.LEFT, 4, ' '));    //유효기간
            dataInfo.put("SB11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB12")), AppUtil.LEFT, 20, ' '));   //Holder
            dataInfo.put("SB12", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB13")), AppUtil.LEFT, 2, ' '));    //국가코드
            dataInfo.put("SB13", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")), AppUtil.LEFT, 12, ' '));            //승인번호
            dataInfo.put("SB14", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_TRADEDT_AUTH")), AppUtil.LEFT, 14, ' '));        //거래일시
            dataInfo.put("SB15", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' '));               //SeqNo

            if (format == AppData.F0182 || format == AppData.F0183) {
                dataInfo.put("SB16", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB96")), AppUtil.LEFT, 3, ' '));      //수수료 통화
                dataInfo.put("SB17", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SERVICEFEE")), AppUtil.RIGHT, 5, '0'));      //MDR
                dataInfo.put("SB18", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SERVICEFEEAMT")), AppUtil.RIGHT, 12, '0'));      //MDR 금액
                dataInfo.put("SB19", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_REMITDT")), AppUtil.LEFT, 8, ' '));      //정산일자
            }

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("[VerWorker::procF0181]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        }
    }

    //신용카드 취소
    private void procF0200(Map<String, String> dataInfo) throws MyException {
        try {
            //////////////////////////////////////////////////////////////////////////
            // 거래 유효성 확인 및 transact_web, transact 처리
            //////////////////////////////////////////////////////////////////////////
            int format = Integer.parseInt((String) dataInfo.get("RH00"));
            if (format == AppData.F0201) {
                //필수 데이터 확인
                if (AppUtil.checkNull(dataInfo.get("RB03")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0200]", "W120", "invalid cur", "invalid cur");
                else if (AppUtil.checkNull(dataInfo.get("RB04")).trim().equals("") || AppUtil.checkNullDouble(dataInfo.get("RB04")) <= 0)
                    throw new MyException("[VerWorker::procF0200]", "W121", "invalid amt", "invalid amt");
                else if (AppUtil.checkNull(dataInfo.get("RB90")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0200]", "W122", "invalid ref", "invalid ref");
                else if (AppUtil.checkNull(dataInfo.get("RB02")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0200]", "W123", "invalid transid", "invalid transid");
                else if (AppUtil.checkNull(dataInfo.get("RB50")).trim().equals("") || AppUtil.checkNullDouble(dataInfo.get("RB50")) <= 0)
                    throw new MyException("[VerWorker::procF0200]", "W133", "invalid void amt", "invalid void amt");
                else if (AppUtil.checkNull(dataInfo.get("RB51")).trim().equals(""))
                    throw new MyException("[VerWorker::procF0200]", "W134", "invalid authcode", "invalid authcode");

                //허용 가맹점 IP 체크

                System.out.println("[VerWorker::procF0200]허용 가맹점 IP 체크 완료");
                //승인금액
                String cur = AppUtil.checkNull(dataInfo.get("RB03")).trim();
                double amt = AppUtil.checkNullDouble(dataInfo.get("RB04"));
                if (!cur.equals("KRW") && !cur.equals("JPY") && !cur.equals("THB")) amt = amt / 100;
                dataInfo.put("VD_AMT", String.valueOf(amt));
                System.out.println("[VerWorker::procF0200]승인금액 : " + amt);

                //데이터 유효성, 원거래 확인
                dbproc.checkVoidRequest(dataInfo);
                System.out.println("[VerWorker::procF0200]취소 데이터 유효성, 원거래 확인");
                //미정산 금액 여부 확인
                dbproc.checkTotalAmount(dataInfo);
                System.out.println("[VerWorker::procF0200]취소 데이터 미정산 금액 확인");

                //일반 전문 format으로 변경
                dataInfo.put("RH00", String.valueOf(AppData.F0200));
                dataInfo.put("RH03", "0200");

            }


            //////////////////////////////////////////////////////////////////////////
            // VAN 객체 생성
            //////////////////////////////////////////////////////////////////////////
            AppVAN appVan = createVAN(dataInfo);

            //////////////////////////////////////////////////////////////////////////
            // VAN 거래 요청(거래 저장)
            //////////////////////////////////////////////////////////////////////////
            String m_run = AppUtil.checkNull(AbstractApp.g_appCfg.getServerRun("EXIMGW"));

            if (m_run.equals("TEST") && "1FD7571895".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "2367AC3B96".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "47B9A61BF2".equals(AppUtil.checkNull(dataInfo.get("RB01"))) || "1E9EAF4881".equals(AppUtil.checkNull(dataInfo.get("RB01")))) {
                m_run = "REAL";
            }

            if (m_run.equals("REAL")) {
                appVan.procVAN(dataInfo);
            } else {
                appVan.procVANTest(dataInfo);
            }

            //////////////////////////////////////////////////////////////////////////
            // 거래 결과 transact_void, transact 저장
            //////////////////////////////////////////////////////////////////////////
            if (format == AppData.F0201) {
                //PCINS 전문 format으로 변경
                dataInfo.put("RH00", String.valueOf(AppData.F0201));
                dataInfo.put("RH03", "0201");

                if (AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                    //취소 처리 저장
                    dbproc.insertVoidTransact(dataInfo);
                    System.out.println("[VerWorker::procF0200]취소 처리 저장");
                }
            }


            //////////////////////////////////////////////////////////////////////////
            // 응답전문 포맷팅
            //////////////////////////////////////////////////////////////////////////
            dataInfo.put("RH04", dataInfo.get("VD_RESCODE"));

            System.out.println("[VerWorker::procF0200]vd_rescode : " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
            if (!AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                //String resmsg = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' ') + AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                String resmsg = AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                System.out.println("[VerWorker::procF0200]vd_resmsg : " + resmsg);
                throw new MyException("[VerWorker::procF0200]", AppUtil.checkNull(dataInfo.get("VD_RESCODE")), resmsg, resmsg, AppUtil.checkNull(dataInfo.get("VD_SEQNO")));
            }

            dataInfo.put("SB01", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB01")), AppUtil.LEFT, 10, ' '));   //가맹점아이디
            dataInfo.put("SB02", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB02")), AppUtil.LEFT, 24, ' '));   //PG 일련번호
            dataInfo.put("SB03", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB03")), AppUtil.LEFT, 3, ' '));    //통화코드
            dataInfo.put("SB04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));  //거래금액
            dataInfo.put("SB05", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));  //봉사료
            dataInfo.put("SB06", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));  //세금
            dataInfo.put("SB07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부기간
            dataInfo.put("SB08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB50")), AppUtil.RIGHT, 12, '0'));  //취소금액
            dataInfo.put("SB09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")), AppUtil.LEFT, 12, ' '));   //승인번호
            dataInfo.put("SB10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_TRADEDT")), AppUtil.LEFT, 14, ' '));   //거래일자
            dataInfo.put("SB11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' '));

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("[VerWorker::procF0200]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        }
    }

    /**
     * @param dataInfo
     * @return : void
     * @throws MyException
     * @methodName : procF9999
     * @author : Edgar
     * @date : 2024.03.15
     * @description : 부하테스트 용
     */
    private void procF9999(Map<String, String> dataInfo) throws MyException {
        try {
            //////////////////////////////////////////////////////////////////////////
            // 거래 유효성 확인 및 transact_web, transact 처리
            //////////////////////////////////////////////////////////////////////////
            int format = Integer.parseInt((String) dataInfo.get("RH00"));

            //////////////////////////////////////////////////////////////////////////
            // VAN 객체 생성
            //////////////////////////////////////////////////////////////////////////
            AppVAN appVan = createVAN(dataInfo);

            //////////////////////////////////////////////////////////////////////////
            // VAN 거래 요청(거래 저장)
            //////////////////////////////////////////////////////////////////////////
            String m_run = AppUtil.checkNull(AbstractApp.g_appCfg.getServerRun("EXIMGW"));

            //2022.04.22 KICC 개발기 및 테스트기 통신 고날 mid 분기 추가 walter.
            System.out.println("[VerWorker::procF9999] RB01 : " + AppUtil.checkNull(dataInfo.get("RB01")));

            appVan.procVANStressTest(dataInfo);


            //////////////////////////////////////////////////////////////////////////
            // 응답전문 포맷팅
            //////////////////////////////////////////////////////////////////////////
            dataInfo.put("RH04", dataInfo.get("VD_RESCODE"));

            System.out.println("[VerWorker::procF9999]vd_rescode : " + AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
            if (!AppUtil.checkNull(dataInfo.get("VD_RESCODE")).equals("0000")) {
                //String resmsg = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' ') + AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                String resmsg = AppUtil.checkNull(dataInfo.get("VD_RESMSG"));
                System.out.println("[VerWorker::procF9999]vd_resmsg : " + resmsg);
                throw new MyException("[VerWorker::procF9999]", AppUtil.checkNull(dataInfo.get("VD_RESCODE")), resmsg, resmsg, AppUtil.checkNull(dataInfo.get("VD_SEQNO")));
            }

            dataInfo.put("SB01", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB01")), AppUtil.LEFT, 10, ' '));   //가맹점아이디
            dataInfo.put("SB02", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB02")), AppUtil.LEFT, 24, ' '));   //PG 일련번호
            dataInfo.put("SB03", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB03")), AppUtil.LEFT, 3, ' '));    //통화코드
            dataInfo.put("SB04", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB04")), AppUtil.RIGHT, 12, '0'));  //거래금액
            dataInfo.put("SB05", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB05")), AppUtil.RIGHT, 12, '0'));  //봉사료
            dataInfo.put("SB06", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB06")), AppUtil.RIGHT, 12, '0'));  //세금
            dataInfo.put("SB07", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB07")), AppUtil.RIGHT, 2, '0'));   //할부기간
            dataInfo.put("SB08", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB08")), AppUtil.LEFT, 4, ' '));    //카드구분
            dataInfo.put("SB09", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB09")), AppUtil.LEFT, 20, ' '));   //카드번호
            dataInfo.put("SB10", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB10")), AppUtil.LEFT, 4, ' '));    //유효기간
            dataInfo.put("SB11", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB12")), AppUtil.LEFT, 20, ' '));   //Holder
            dataInfo.put("SB12", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB13")), AppUtil.LEFT, 2, ' '));    //국가코드
            dataInfo.put("SB13", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")), AppUtil.LEFT, 12, ' '));   //승인번호
            dataInfo.put("SB14", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_TRADEDT_AUTH")), AppUtil.LEFT, 14, ' '));   //거래일시
            dataInfo.put("SB15", AppUtil.formatData(AppUtil.checkNull(dataInfo.get("VD_SEQNO")), AppUtil.LEFT, 12, ' '));   //SeqNo

        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException("[VerWorker::procF9999]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        }
    }


    private AppVAN createVAN(Map<String, String> dataInfo) {
        AppVAN appVan = null;

        String vanCode = AppUtil.checkNull(dataInfo.get("RH02")).trim();

        appVan = new KICCWorker(appWorker);

        return appVan;
    }

}
