/*
 * KSNETDBProc.java
 */

package api.server.payment.van.kicc;


import api.server.common.config.datasource.DBConnectionManager;
import api.server.common.helper.AppUtil;
import api.server.common.helper.cipher.AbstractCipher;
import api.server.common.helper.cipher.AppCipher;
import api.server.common.helper.cipher.CipherFactory;
import api.server.payment.gateway.AbstractApp;
import api.server.payment.my.MyException;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author 케이알파트너스
 * 
 */
public class KICCDBProc {
    private DBConnectionManager dbcm;

    private Connection conn;
    private PreparedStatement pstmt;
    private CallableStatement cstmt;
    private ResultSet rs;
    private StringBuffer sbQuery;
    private AppCipher appCipher;

    /** Creates a new instance of KICCDBProc */
    public KICCDBProc(DBConnectionManager dbcm) {
        this.dbcm = dbcm;
        appCipher = new AppCipher();
    }

     /**************************************************************************
     * 
     * 가맹점 정보 조회
     * 
     *************************************************************************/
    public void selectMerchant(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            
            sbQuery.append("select a.bizno, a.siteurl, a.serverip, c.kicctid as tid, c.cardcode as purchasecode, ");
            //sbQuery.append("case ");
            //sbQuery.append("    when b.cardcode='C002' then c.merchantno ");        //삼성 AMEX 2013.06
            //sbQuery.append("    when b.cardcode='C026' then c.merchantno ");        //비인증 BCUPOP
            //sbQuery.append("    when b.cardcode='C023' then c.merchantno ");        //UPOP(BC, 롯데, 하나)
            //sbQuery.append("    else concat('00', substring(c.merchantno, 4, 9)) ");
            //sbQuery.append("end as mid, a.status, ifnull(c.mcpcode, '') as mcpcode,  ");
            sbQuery.append("c.merchantno as mid, a.status, ifnull(c.mcpcode, '') as mcpcode,  ");
            sbQuery.append("c.apprvcurrency, c.upoptype, ");
            sbQuery.append("d.acquirer_merchant_id, ifnull(d.submall_biz_id,'') as submall_biz_id , d.submall_site_url, ifnull(d.status,'') as submall_status ");  //Edgar 2024.10.23 서브몰 정보 확인 추가
            sbQuery.append("from EXIMGW.keb_merchant_service a ");
            sbQuery.append("inner join EXIMGW.keb_rmer_memberid b on a.merchantid=b.merchantid ");
            sbQuery.append("inner join EXIMGW.keb_merchant c on b.memberno=c.merchantno ");
            sbQuery.append("left outer join EXIMGW.acquirer_submall d on c.cardcode=d.acquirer_code and c.merchantno=d.acquirer_merchant_id and a.bizno=d.submall_biz_id ");  //Edgar 2024.10.23 서브몰 정보 확인 추가
            sbQuery.append("where a.merchantid=? and b.currency=? and b.cardcode=? "); 
            
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB03")));
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB08")));
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(!rs.getString("status").equals("A300"))
                    throw new MyException("[KICCDBProc::selectMerchant]", "S011", "KICC 정지 및 해지 가맹점", "미등록 가맹점");
                else if(rs.getString("tid").equals("5004427"))  //신한은행 거래 중단
                    throw new MyException("[KICCDBProc::selectMerchant]", "S011", "해지 가맹점", "해지 가맹점");
                else if(!rs.getString("apprvcurrency").equals(AppUtil.checkNull(dataInfo.get("RB03")))) 
                    throw new MyException("[KICCDBProc::selectMerchant]", "S012", "KEB 가맹점번호 승인통화 불일치", "승인통화 불일치");
                
                // Edgar 2024.10.23 거절 로직 추가 
                if("CD00".equals(AppUtil.checkNull(rs.getString("purchasecode")))) { // BC매입사인 경우만 확인 
                	if("".equals(rs.getString("submall_biz_id")) || !"COMPLETE".equals(rs.getString("submall_status"))) {
                		// 서브몰 정보가 정상적으로 들어있지 않은 경우 
                		String errMsg = "매입사 미등록 가맹점 (매입사 가맹점번호:"
     			                 + AppUtil.checkNull(rs.getString("mid")).trim()
     			                 + ", 서브몰 사업자번호:"
     			                 + rs.getString("bizno")
     			                 +", 진행상태:"
     			                 + rs.getString("submall_status")+")";
                		
                		if("C001".equals(dataInfo.get("RB08"))) {
                			String m_run = AppUtil.checkNull(AbstractApp.g_appCfg.getServerRun("EXIMGW"));
                			if("REAL".equals(m_run)) {
                				// MASTER 카드는 거절처리 한다. 
                				throw new MyException("[KICCDBProc::selectMerchant]", "S013", errMsg, errMsg);
                			}else {
                				dataInfo.put("VD_TMP_WARN", errMsg);
                			}
                		}else {
                			// 이외는 에러로깅을 위해 임시 저장한다. - 프로세스는 정상진행  
                			dataInfo.put("VD_TMP_WARN", errMsg);
                		}
                	}	
                }
                
                dataInfo.put("VD_BIZNO",    rs.getString("bizno"));
                dataInfo.put("VD_KICCTID",  rs.getString("tid"));
                dataInfo.put("VD_CARDMID",  AppUtil.checkNull(rs.getString("mid")).trim().replace("-",""));
                dataInfo.put("VD_MID",      AppUtil.checkNull(rs.getString("mid")).trim().replace("-",""));
                dataInfo.put("VD_SITEURL",  AppUtil.checkNull(rs.getString("siteurl")));
                dataInfo.put("VD_SERVERIP", AppUtil.checkNull(rs.getString("serverip")));
                dataInfo.put("VD_MCPCODE",  rs.getString("mcpcode"));
                // 2018-08-23 해당 UPOPTYPE은 비씨 UPOP 비인증 Recurring거래시 구분값으로 사용한다.
                dataInfo.put("VD_UPOPTYPE", AppUtil.checkNull(rs.getString("upoptype")));
                // 2022-10-20 매입사 관련 코드 추가
                dataInfo.put("VD_PURCHASECODE", AppUtil.checkNull(rs.getString("purchasecode")));
              
            }else{
                throw new MyException("[KICCDBProc::selectMerchant]", "S010", "KICC 미등록 가맹점", "미등록 가맹점");
            }
            rs.close();
            pstmt.close();
        }catch(MyException e){
            throw e;
        }catch(SQLException e){
                if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                throw new MyException("[KICCDBProc::selectMerchant]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
        }catch(Exception e){
                throw new MyException("[KICCDBProc::selectMerchant]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
        }finally{
                try{
                        if(rs != null) rs.close();
                        if(pstmt != null) pstmt.close();
                        if(dbcm != null){
                                dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                        }
                }catch(Exception ie){}
        }
    }
     /**************************************************************************
     * 
     * 사용자 IP / DM Session ID 추출
     * 
     *************************************************************************/
    public void selectRiskInfo(Map<String, String> dataInfo) throws MyException{
        System.out.println("[selectRiskInfo] Start transid : "+AppUtil.checkNull(dataInfo.get("RB02")));
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            
            sbQuery.append("select b.ACCESSIP as accessip , c.BILLTO_IPADDRESS as riskaccessip, d.DFID as risksessionid  ");
            sbQuery.append("from EXIMBAY.transact a  ");
            sbQuery.append("left outer join EXIMBAY.transact_web b on a.transno = b.transno  ");
            sbQuery.append("left outer join EXIMBAY.dm_transact c on a.transno = c.transno ");
            sbQuery.append("left outer join EXIMBAY.df_transact d on c.dfno = d.dfno  ");
            sbQuery.append("where a.transid = ? ");
            
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            System.out.println("[selectRiskInfo] Start pstmt : "+pstmt.toString());
            rs = pstmt.executeQuery();
            if(rs.next()){
                                   
                    dataInfo.put("VD_ACCESSIP",    AppUtil.checkNull(rs.getString("accessip")));
                    dataInfo.put("VD_RISKACCESSIP",    AppUtil.checkNull(rs.getString("riskaccessip")));
                    if (!AppUtil.checkNull(rs.getString("risksessionid")).trim().equals(""))
                        dataInfo.put("VD_RISKSESSIONID",    "krpartners" + AppUtil.checkNull(rs.getString("risksessionid")));
                    System.out.println("[selectRiskInfo] Start pstmt IN ");   
                    
             }
            System.out.println("[selectRiskInfo] VD_ACCESSIP : "+dataInfo.get("VD_ACCESSIP")+", VD_RISKACCESSIP : "+dataInfo.get("VD_RISKACCESSIP")+", VD_RISKSESSIONID : "+dataInfo.get("VD_RISKSESSIONID")); 
            System.out.println("[selectRiskInfo] End");
            rs.close();
            pstmt.close();
        }catch(SQLException e){
                if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                //throw new MyException("[KICCDBProc::selectRiskInfo]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
        }catch(Exception e){
                //throw new MyException("[KICCDBProc::selectRiskInfo]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
        }finally{
                try{
                        if(rs != null) rs.close();
                        if(pstmt != null) pstmt.close();
                        if(dbcm != null){
                                dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                        }
                }catch(Exception ie){}
        }
    }
    /**************************************************************************
     * 
     * 거래 원장 조회 (동일 주문번호 확인)
     * 
     *************************************************************************/
    public void selectTransact(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //동일한 주문번호 확인
            pstmt = conn.prepareStatement("select * from kicc_transact where transid=? and msgtype=? ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            if(AppUtil.checkNull(dataInfo.get("RH03")).equals("0400"))
                pstmt.setString(2, "1210");
            else pstmt.setString(2, "1130");
            rs = pstmt.executeQuery();
            if(rs.next()){
                throw new MyException("[KICCDBProc::selectTransact]", "S020", "주문번호 중복", "주문번호 중복");
            }
            rs.close();
            pstmt.close();

        }catch(MyException e){
            throw e;
        }catch(SQLException e){
                if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                throw new MyException("[KICCDBProc::selectTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
        }catch(Exception e){
                throw new MyException("[KICCDBProc::selectTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
        }finally{
                try{
                        if(rs != null) rs.close();
                        if(pstmt != null) pstmt.close();
                        if(dbcm != null){
                                dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                        }
                }catch(Exception ie){}
        }
    }

    /**************************************************************************
     * 
     * 원거래 데이터 추출
     * 
     *************************************************************************/
    public void selectOrgTransact(Map<String, String> vanReq, Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //기 매입취소 건인지 확인 
            pstmt = conn.prepareStatement("select count(*) as cnt from kicc_transact where transid=? and rfield13=? and msgtype='1210' and rfield03='O' and edistatus in ('12', '02', '03', '60', '64') ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB51")).trim());
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(rs.getInt("cnt") > 0)
                    throw new MyException("[KICCDBProc::selectOrgTransact]", "V018", "이미 취소 요청된 거래", "이미 취소 요청된 거래");
            }
            rs.close();
            pstmt.close();
            
            //일본 엔화와 타이 바트의 경우
            String orgAmt = AppUtil.checkNull(dataInfo.get("RB04"));
            if(AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY") || AppUtil.checkNull(dataInfo.get("RB03")).equals("THB")){
                //JPY금액 * 100, THB금액 * 100
                String jpyAmt = AppUtil.checkNull(dataInfo.get("RB04")) + "00";
                orgAmt = AppUtil.formatData(jpyAmt, AppUtil.RIGHT, 12, '0');
            }

            pstmt = conn.prepareStatement("select *,date_format(resdt,'%Y%m%d') AS authdt from kicc_transact where transid=? and rfield13=? and msgtype='1130' and rfield03='O' ");

            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB51")).trim());
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(rs.getString("status").equals("T002"))
                    throw new MyException("[KICCDBProc::selectOrgTransact]", "V011", "이미 취소된 거래", "이미 취소된 거래");
                else if(!rs.getString("field04").equals(AppUtil.checkNull(dataInfo.get("RB07")).trim()))
                    //할부기간 일치여부
                    throw new MyException("[KICCDBProc::selectOrgTransact]", "V014", "승인거래 없음", "승인거래 없음");
                else if(!rs.getString("field05").equals(AppUtil.checkNull(vanReq.get("VS_FIELD05"))))
                    //통화코드 일치여부
                    throw new MyException("[KICCDBProc::selectOrgTransact]", "V015", "승인거래 없음", "승인거래 없음");
                else if(!rs.getString("field07").equals(orgAmt))
                    //승인금액 일치여부
                    throw new MyException("[KICCDBProc::selectOrgTransact]", "V016", "승인거래 없음", "승인거래 없음");
                else if (!AppUtil.checkNull(dataInfo.get("RB04")).trim().equals(AppUtil.checkNull(dataInfo.get("RB50")).trim()))
                    //전체 취소 확인 여부
                    throw new MyException("[KICCDBProc::selectTransact]", "V009", "전체취소 거래 금액 불일치", "전체취소 거래 금액 불일치");
                else{
                
// yunsu 20160927 (매입사 원거래 시간과 KRP원거래 시간의 오차로 인한 로직 수정)
                    if(!rs.getString("rfield04").startsWith(AppUtil.checkNull(dataInfo.get("RB52")).trim().substring(2, 8))){
                    	//오류로 판명될경우 재 검증
                    	if(!rs.getString("authdt").equals(AppUtil.checkNull(dataInfo.get("RB52")))){
                            //원거래일자 일치여부
                            throw new MyException("[KICCDBProc::selectOrgTransact]", "V013", "원거래일자 불일치. ", "원거래일자 불일치. ");
                    	}
                    }
// yunsu 20160927 VAN에 전송할 데이터 세팅 
                	dataInfo.put("VD_AUTHDT", AppUtil.checkNull(rs.getString("rfield04")).trim().substring(0,6));

                    AbstractCipher cipher = CipherFactory.getCipher(rs.getString("KEYINDEX"));

                    byte[] encField03 = rs.getBytes("field03");
                    //byte[] decField03 = appCipher.decryptData(encField03);
                    byte[] decField03 = cipher.decryptData(encField03);
                    
                    //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
                    //byte[] encField21_1 = rs.getBytes("field21_1");
                    //byte[] decField21_1 = cipher.decryptData(encField21_1);

                    String trkData = new String(decField03).trim();
                    String cardNo = trkData.substring(0, trkData.indexOf("="));
                    String expDT = trkData.substring(trkData.indexOf("=")+1, trkData.length());
                    
                    dataInfo.put("RB09", cardNo);
                    dataInfo.put("RB10", expDT);
                    //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
                    //dataInfo.put("RB11", new String(decField21_1));
                    
                    dataInfo.put("VD_ORGTRANSNO", rs.getString("transno"));
                    dataInfo.put("VD_KICCTID", rs.getString("kicctid"));  //원거래 정보로
                    dataInfo.put("VD_CARDMID", rs.getString("cardmid"));    //원거래 정보로
                    // KICC는 취소시 필요로 한다.
                    if (AppUtil.checkNull(rs.getString("field21_2")).trim().equals("CVCDATACU") || AppUtil.checkNull(rs.getString("field21_2")).trim().equals("CU") ) {
                        dataInfo.put("RB08", "C026");   // 비씨 은련 구분 
                        
                        if (AppUtil.checkNull(dataInfo.get("RB52")).equals(AppUtil.getDateTime().substring(0, 8))) { 
                            dataInfo.put("VD_REALCANCELYN", "Y");
                        }
                        if(AppUtil.checkNull(rs.getString("status")).equals("T003") && !AppUtil.checkNull(dataInfo.get("VD_REALCANCELYN")).equals("Y"))
                            throw new MyException("[KICCDBProc::selectTransact]", "V012", "매입 후 취소요망", "매입 후 취소요망");
                        
                        
                    } else {
                        if (AppUtil.checkNull(rs.getString("edistatus")).equals("00") ) // 매입전 취소  
                        {
                            /**
                             * UPOP 통신 유무(RB74) 값 'N' 이고, BCUPOP, HANAUPOP 거래인 경우
                             * VD_REALCANCELYN 값을 세팅하지 않는다.
                             *
                             */
                            if (AppUtil.checkNull(dataInfo.get("RB74")).trim().equals("N") &&
                                    (AppUtil.checkNull(rs.getString("rfield14")).trim().equals("BCUPOP") ||
                                     AppUtil.checkNull(rs.getString("rfield14")).trim().equals("HanaUPOP"))) {

                            } else {
                                dataInfo.put("VD_REALCANCELYN", "Y");
                            }
                        }
                    }  
                    dataInfo.put("VD_MPI", AppUtil.checkNull(rs.getString("field22_1")).trim());
                    dataInfo.put("VD_MPIMODULE", AppUtil.checkNull(rs.getString("field22_2")).trim());

                    //MCP 거래인 경우, 직접 통신
                    if(!AppUtil.checkNull(rs.getString("mcpyn")).equals("Y")){
                        dataInfo.put("VR_FIELD02", AppUtil.checkNull(rs.getString("rfield02")));
                        dataInfo.put("VR_FIELD03", rs.getString("rfield03"));
                        dataInfo.put("VR_FIELD04", rs.getString("rfield04"));
                        dataInfo.put("VR_FIELD09", AppUtil.checkNull(rs.getString("rfield09")));
                        dataInfo.put("VR_FIELD10", AppUtil.checkNull(rs.getString("rfield10")));
                        //dataInfo.put("VR_FIELD10", "Refund");
                        dataInfo.put("VR_FIELD11", AppUtil.checkNull(rs.getString("rfield11")));
                        dataInfo.put("VR_FIELD12", AppUtil.checkNull(rs.getString("rfield12")));
                        dataInfo.put("VR_FIELD13", rs.getString("rfield13"));
                        dataInfo.put("VR_FIELD14", AppUtil.checkNull(rs.getString("rfield14")));
                        dataInfo.put("VR_FIELD15", AppUtil.checkNull(rs.getString("rfield15")));
                    } else {
                        dataInfo.put("VR_FIELD03", AppUtil.checkNull(rs.getString("rfield03")).trim());
                        dataInfo.put("VR_FIELD14", AppUtil.checkNull(rs.getString("rfield14")));
                        dataInfo.put("VR_FIELD15", AppUtil.checkNull(rs.getString("rfield15")));
                    }
                    
                    //MCP 취소거래 시, 16, 17은 응답에 포합되지 않아, 원거래 데이터 사용 20120510 by kong
                    dataInfo.put("VD_RFIELD16", rs.getString("rfield16"));
                    dataInfo.put("VR_FIELD16",  rs.getString("rfield16"));               
                    dataInfo.put("VD_RFIELD17", AppUtil.checkNull(rs.getString("rfield17")));

                    dataInfo.put("VD_MCPYN", AppUtil.checkNull(rs.getString("mcpyn")));
                    dataInfo.put("VD_MCPCODE" , AppUtil.checkNull(rs.getString("field14")));
                    dataInfo.put("VD_EDISTATUS", AppUtil.checkNull(rs.getString("edistatus")));

                    //MCP 거래 데이터
                    dataInfo.put("RB21", AppUtil.checkNull(rs.getString("mcpquotecurr")));
                    dataInfo.put("RB22", AppUtil.checkNull(rs.getString("mcpquoteamount")));
                    dataInfo.put("RB23", AppUtil.checkNull(rs.getString("mcpquoteamountminor")));
                    dataInfo.put("RB24", AppUtil.checkNull(rs.getString("mcpconvrate")));
                    dataInfo.put("RB25", AppUtil.checkNull(rs.getString("mcpbasecurr")));
                    dataInfo.put("RB26", AppUtil.checkNull(rs.getString("mcpkrwamount")));
                    dataInfo.put("RB27", AppUtil.checkNull(rs.getString("mcpstatus")));
                    dataInfo.put("RB28", AppUtil.checkNull(rs.getString("mcprateid")));
                    
                    //승인 취소 거절시 매입취소 진행 로직에서 직가맹 거래의 경우 제외. walter 2024.01.11
                    dataInfo.put("ORG_STATUS", AppUtil.checkNull(rs.getString("status")));
                    
                }
            }else{
                throw new MyException("[KICCDBProc::selectTransact]", "V010", "승인거래 없음", "승인거래 없음");
            }
            rs.close();
            pstmt.close();

            pstmt = conn.prepareStatement("select * from kicc_transact where orgtransno=? and rfield03='O' and edistatus in ('02', '03', '60') ");
            pstmt.setInt(1, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))));
            rs = pstmt.executeQuery();
            if(rs.next()){
                throw new MyException("[KICCDBProc::selectTransact]", "V017", "이미 취소요청된 거래", "이미 취소요청된 거래");
            }
            rs.close();
            pstmt.close();
            

            
        }catch(MyException e){
            throw e;
        }catch(SQLException e){
                if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                throw new MyException("[KICCDBProc::selectOrgTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
        }catch(Exception e){
                throw new MyException("[KICCDBProc::selectOrgTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
        }finally{
                try{
                        if(rs != null) rs.close();
                        if(pstmt != null) pstmt.close();
                        if(dbcm != null){
                                dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                        }
                }catch(Exception ie){}
        }
    }
    
    /**************************************************************************
    * 부분 취소 유효성 체크. 
    ***************************************************************************/
    
    public void selectOrgPartTransact(Map<String, String> vanReq, Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            
            int baseAmt = 0;
            
            /************************************************************
             * 1. 원거래 데이터가 매입 상태인지를 확인한다
             ***********************************************************/
            pstmt = conn.prepareStatement("select *,date_format(resdt,'%Y%m%d') AS authdt from EXIMGW.kicc_transact where transid=? and trim(rfield13) = ? and msgtype='1130' and  rfield03='O' ");

            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB51")).trim());
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(rs.getString("status").equals("T002"))
                    throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V011", "이미 취소된 거래.", "이미 취소된 거래.");
                else if(rs.getString("edistatus").equals("99"))
                    throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V011", "이미 취소된 거래.", "이미 취소된 거래.");
                else if(rs.getString("status").equals("T003")  )
                    throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V019", "매입전 승인상태로 부분취소 불가.", "매입전 승인상태로 부분취소 불가.");
                else if(!rs.getString("field04").equals(AppUtil.checkNull(dataInfo.get("RB07")).trim()))
                    //할부기간 일치여부
                    throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V014", "승인거래 없음. ", "승인거래 없음. ");
                else{
// yunsu 20160927 (매입사 원거래 시간과 KRP원거래 시간의 오차로 인한 로직 수정)
                	if(!rs.getString("rfield04").startsWith(AppUtil.checkNull(dataInfo.get("RB52")).trim().substring(2, 8))){
                    	//오류로 판명될경우 재 검증
                    	if(!rs.getString("authdt").equals(AppUtil.checkNull(dataInfo.get("RB52")))){
                            //원거래일자 일치여부
                            throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V013", "원거래일자 불일치. ", "원거래일자 불일치. ");
                    	}
                    }
// yunsu 20160927 VAN에 전송할 데이터 세팅
                	dataInfo.put("VD_AUTHDT", AppUtil.checkNull(rs.getString("rfield04")).trim().substring(0,6));

                    if (rs.getString("mcpyn").equals("Y") || rs.getString("dccyn").equals("Y")) {
                       if(!rs.getString("mcpbasecurr").equals(AppUtil.getCurrencyCode(AppUtil.checkNull(dataInfo.get("RB03")))))
                            //통화코드 일치여부
                            throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V015", "기본통화 불일치. ", "기본통화 불일치.");
                       if(!rs.getString("mcpquotecurr").equals(AppUtil.checkNull(dataInfo.get("RB53"))))
                            //자국코드 일치여부
                            throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V015", "자국통화 불일치. ", "자국통화 불일치."); 
                       
                       baseAmt = Integer.parseInt(rs.getString("field07"));  // 기본 총 통화금액
                    }                       
                    else {
                         if(!AppUtil.checkNull(rs.getString("field05")).trim().equals(AppUtil.checkNull(vanReq.get("VS_FIELD05"))))
                            //통화코드 일치여부
                            throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V015", "기본통화 불일치. ", "기본통화 불일치.");
                       
                        baseAmt = Integer.parseInt(rs.getString("field07"));     // 기본 총 통화금액
                   }
                    // 원 승인 통화 금액 저장
                    dataInfo.put("VD_ORGBASEAMT", String.valueOf(baseAmt));

                    AbstractCipher cipher = CipherFactory.getCipher(rs.getString("KEYINDEX"));

                    byte[] encField03 = rs.getBytes("field03");
                    //byte[] decField03 = appCipher.decryptData(encField03);
                    byte[] decField03 = cipher.decryptData(encField03);
                    
                    //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
                    //byte[] encField21_1 = rs.getBytes("field21_1");
                    //byte[] decField21_1 = cipher.decryptData(encField21_1);
                    
                    String trkData = new String(decField03).trim();
                    String cardNo = trkData.substring(0, trkData.indexOf("="));
                    String expDT = trkData.substring(trkData.indexOf("=")+1, trkData.length());

                    dataInfo.put("RB09", cardNo);
                    dataInfo.put("RB10", expDT);
                    //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
                    //dataInfo.put("RB11", new String(decField21_1));
                    
                    dataInfo.put("VD_ORGTRANSNO", rs.getString("transno"));
                    dataInfo.put("VD_KICCTID", rs.getString("kicctid"));  //원거래 정보로
                    dataInfo.put("VD_CARDMID", rs.getString("cardmid"));    //원거래 정보로
                    // KICC는 취소시 필요로 한다.
                    dataInfo.put("VD_MPI", AppUtil.checkNull(rs.getString("field22_1")).trim());
                    dataInfo.put("VD_MPIMODULE", AppUtil.checkNull(rs.getString("field22_2")).trim());
                    
                    dataInfo.put("VR_FIELD02", AppUtil.checkNull(rs.getString("rfield02")));
                    dataInfo.put("VR_FIELD04", AppUtil.checkNull(rs.getString("rfield04")));
                    dataInfo.put("VR_FIELD09", AppUtil.checkNull(rs.getString("rfield09")));
                    
                    dataInfo.put("VR_FIELD11", AppUtil.checkNull(rs.getString("rfield11")));
                    dataInfo.put("VR_FIELD12", AppUtil.checkNull(rs.getString("rfield12")));
                    dataInfo.put("VR_FIELD13", rs.getString("rfield13"));
                    dataInfo.put("VR_FIELD14", AppUtil.checkNull(rs.getString("rfield14")));
                    dataInfo.put("VR_FIELD15", AppUtil.checkNull(rs.getString("rfield15")));
                    
                    // KICC는 취소시 필요로 한다.
                    if (AppUtil.checkNull(rs.getString("field21_2")).trim().equals("CVCDATACU") || AppUtil.checkNull(rs.getString("field21_2")).trim().equals("CU") ) {
                        dataInfo.put("RB08", "C026");   // 비씨 은련 구분 
                    }    
                    
                    dataInfo.put("VD_RFIELD16", rs.getString("rfield16"));
                    dataInfo.put("VR_FIELD16",  rs.getString("rfield16"));  
                    dataInfo.put("VD_RFIELD17", AppUtil.checkNull(rs.getString("rfield17")));

                    dataInfo.put("VD_MCPYN", AppUtil.checkNull(rs.getString("mcpyn")));
                    dataInfo.put("VD_MCPCODE" , AppUtil.checkNull(rs.getString("field14")));
                    dataInfo.put("VD_EDISTATUS", AppUtil.checkNull(rs.getString("edistatus")));

                    //MCP 거래 데이터
                    dataInfo.put("RB21", AppUtil.checkNull(rs.getString("mcpquotecurr")));
                    dataInfo.put("RB22", AppUtil.checkNull(rs.getString("mcpquoteamount")));
                    dataInfo.put("RB23", AppUtil.checkNull(rs.getString("mcpquoteamountminor")));
                    dataInfo.put("RB24", AppUtil.checkNull(rs.getString("mcpconvrate")));
                    dataInfo.put("RB25", AppUtil.checkNull(rs.getString("mcpbasecurr")));
                    dataInfo.put("RB26", AppUtil.checkNull(rs.getString("mcpkrwamount")));
                    dataInfo.put("RB27", AppUtil.checkNull(rs.getString("mcpstatus")));
                    dataInfo.put("RB28", AppUtil.checkNull(rs.getString("mcprateid")));
                    
                    dataInfo.put("VR_FIELD31", AppUtil.checkNull(rs.getString("cardtraceno")));
                    
                    /********************************************************************************
                     * 자국통화 금액의 유효성 검사
                     ********************************************************************************/
                    if (rs.getString("mcpyn").equals("Y") || rs.getString("dccyn").equals("Y")) {
                        String McpConvMinor = AppUtil.formatData(AppUtil.checkNull(rs.getString("mcpconvrate")), AppUtil.RIGHT, 10, '0').substring(0, 1);
                        String McpConvRate  = AppUtil.formatData(AppUtil.checkNull(rs.getString("mcpconvrate")), AppUtil.RIGHT, 10, '0');
                        String McpConvRate_result = McpConvRate.substring(1, (McpConvRate.length() - Integer.parseInt(McpConvMinor))) + "." + McpConvRate.substring(McpConvRate.length() - Integer.parseInt(McpConvMinor));
                        
                        double baseamt = 0;
                        double curamt  = 0;
                        if (!dataInfo.get("RB03").equals("KRW") &&  !dataInfo.get("RB03").equals("JPY")) {
                            baseamt = Double.parseDouble(dataInfo.get("RB50")) / 100;
                        } else { 
                            baseamt = Double.parseDouble(dataInfo.get("RB50"));
                        }
                        if (AppUtil.checkNull(rs.getString("mcpquoteamountminor")).equals("2")) {
                            curamt = Double.parseDouble(dataInfo.get("RB54")) / 100;
                        } else { 
                            curamt = Double.parseDouble(dataInfo.get("RB54"));
                        }
                        System.out.println("자국통화금액 " + curamt);
                        System.out.println("직계산자국통화금액 " + baseamt * Double.parseDouble(McpConvRate_result));
                        System.out.println("자국통화금액 오차 5% " + (baseamt * Double.parseDouble(McpConvRate_result) * 1.05));
                        System.out.println("자국통화금액 오차 -5% " + (baseamt * Double.parseDouble(McpConvRate_result) * 0.95));
                        
                        // 매입사 하나의 경우 자국 통화가 VND일경우는 첫 승인 거래시 100동 기준이므로 환율에 곱하기 100을 하여 저장하였으므로 환율에 나누기 100을 해서 계산을 한다.
                        if (AppUtil.checkNull(dataInfo.get("RB21")).equals("704") && AppUtil.checkNull(dataInfo.get("RB03")).equals("KRW") && AppUtil.checkNull(dataInfo.get("VD_RFIELD16")).equals("03")) {
                            if ((curamt > (baseamt * ( Double.parseDouble(McpConvRate_result) / 100 ) * 1.05) ) ||  (curamt < (baseamt * ( Double.parseDouble(McpConvRate_result) / 100 ) * 0.95) )) {
                                 throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V015", "자국통화 오차범위초과 ", "자국통화 오차범위초과 ");
                            } 
                        } else {
                            if ((curamt > (baseamt * Double.parseDouble(McpConvRate_result) * 1.05) ) ||  (curamt < (baseamt * Double.parseDouble(McpConvRate_result) * 0.95) )) {
                                 throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V015", "자국통화 오차범위초과 ", "자국통화 오차범위초과 ");
                            }
                        } 
                    }
    
                }
            }else{
                throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V010", "승인거래 없음. ", "승인거래 없음. ");
            }
            rs.close();
            pstmt.close();
            
            /*******************************************************************
             * 2. 취소 거래 데이터의 합을 구한다
             *******************************************************************/
            pstmt = conn.prepareStatement("select sum(field07) as base from EXIMGW.kicc_transact where transid=? and trim(rfield13) = ? and msgtype in ('0204','0206','1210')  and rfield03='O'  and edistatus in ('12', '02', '03', '60', '64')  ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB51")).trim());
            rs = pstmt.executeQuery();
            if(rs.next()){
                
                int part_baseamt    = rs.getInt("base");
                
                //엔화의 경우
                String orgAmt = AppUtil.checkNull(dataInfo.get("RB50"));
                if(AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY") || AppUtil.checkNull(dataInfo.get("RB03")).equals("THB")){
                    //JPY금액 * 100
                    String jpyAmt = AppUtil.checkNull(dataInfo.get("RB50"))  + "00";
                    orgAmt = AppUtil.formatData(jpyAmt, AppUtil.RIGHT, 12, '0');
                } else {
                    orgAmt = AppUtil.checkNull(dataInfo.get("RB50"));
                } 
                
                int part_cancelamt  = Integer.parseInt(orgAmt);
          
                
                if (baseAmt - (part_cancelamt + part_baseamt) < 0)
                    throw new MyException("[KICCDBProc::selectOrgPartTransact]", "V030", "잔여금액이 취소금액보다 작습니다.", "잔여금액이 취소금액보다 작습니다.");
                
            }
            rs.close();
            pstmt.close();
            
        }catch(MyException e){
            throw e;
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::selectOrgPartTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::selectOrgPartTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }
    
    /**************************************************************************
    * 요청 데이터 저장
    ***************************************************************************/
    public void insertTransact(Map<String, String> vanReq, Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            AbstractCipher cipher = CipherFactory.getCipher(AbstractApp.g_appCfg.getKmsCekLastKeyIndex());

            byte[] encFIELD02 = cipher.encryptData(AppUtil.checkNull(vanReq.get("VS_FIELD03")).getBytes());
            //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
			//byte[] encFIELD21_1 = cipher.encryptData(AppUtil.checkNull(vanReq.get("VS_FIELD21_1")).getBytes());

            
            // Edgar 2025.03.07 table lock제거  
            /*
            String curDate = AppUtil.getDateTime().substring(0, 8);
            int maxNo = 0;

            try{
                conn.setAutoCommit(false);

                pstmt = conn.prepareStatement("lock tables max_id write");
                pstmt.executeUpdate();
                pstmt.close();
                
                pstmt = conn.prepareStatement("select maxkicc from max_id where lastdtkicc=? ");
                pstmt.setString(1, curDate);
                rs = pstmt.executeQuery();
                if(rs.next()){
                    maxNo = rs.getInt("maxkicc");
                }
                rs.close();
                pstmt.close();

                if(maxNo == 0) maxNo = 1;
                else maxNo++;


                pstmt = conn.prepareStatement("update max_id set maxkicc=?, lastdtkicc=? ");
                pstmt.setInt(1, maxNo);
                pstmt.setString(2, curDate);
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("unlock tables ");
                pstmt.executeUpdate();
                pstmt.close();


                conn.commit();
            }catch(Exception ie){

                conn.rollback();
            }finally{
                conn.setAutoCommit(true);
            }
            */

            
            //MCP 거래의 경우 업체코드 확인 --> KRPartner인 경우, Purecommerce FIN2에서 제외
            String fin2fn = "";
            if(AppUtil.checkNull(vanReq.get("VD_MCPYN")).equals("Y")){
                sbQuery = new StringBuffer();
                sbQuery.append("select ifnull(c.mcpcode, '') as mcpcode ");
                sbQuery.append("from EXIMGW.keb_merchant_service a ");
                sbQuery.append("inner join EXIMGW.keb_rmer_memberid b on a.merchantid=b.merchantid and b.currency=? ");
                sbQuery.append("inner join EXIMGW.keb_merchant c on b.memberno=c.merchantno ");
                sbQuery.append("where a.merchantid=? ");
                pstmt = conn.prepareStatement(sbQuery.toString());
                pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB03")));
                pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB01")));
                rs = pstmt.executeQuery();
                if(rs.next()){
                    // KICC의 경우 모두 GCMC 환율을 쓴다...
                    if(!rs.getString("mcpcode").equals("PC")){
                        fin2fn = "K";
                    }
                }
                rs.close();
                pstmt.close();
            }

            //String seqNo = curDate.substring(2, 8) + AppUtil.formatData(maxNo, AppUtil.RIGHT, 6, '0');
            String seqNo = AppUtil.generateKey();
         
                
            /******** 중요 KICC는 마이너 유신 8과 4만을 허용하므로 변경하여야 한다 ********/
            /******** 추후 FIN 파일 대사를 위해 거래테이블에도 변환 환율을 입력 시킨다 ****/ 
            if (AppUtil.checkNull(dataInfo.get("VD_CARDMID")).length() > 3 ) { 
                    if (AppUtil.checkNull(dataInfo.get("VD_CARDMID")).trim().substring(0,3).equals("009")) {  // 하나(외환)만 적용.
                        String rate_org = AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB24")), AppUtil.RIGHT, 10, '0');
                       if( !rate_org.substring(0,1).equals("8") && !rate_org.substring(0,1).equals("4") && !rate_org.substring(0,1).equals("0")) {
                           String unit = rate_org.substring(0,1);
                           String rate = rate_org.substring(1, 9 - Integer.parseInt(unit) + 1) + "." + rate_org.substring(9 - Integer.parseInt(unit) + 1);
                           // 기본통화 410 이고 자국통화 704 일경우 마이너 유닛이 4일경우는 100 기준이므로..환율에 100을 곱해서 준다.
                           if (AppUtil.checkNull(dataInfo.get("RB21")).equals("704") && AppUtil.checkNull(dataInfo.get("RB25")).equals("410") ) {
                               rate = String.valueOf(Double.valueOf(rate) * 100);
                           }
                           DecimalFormat min;
                           min = new DecimalFormat("#############.0000");  // 마이너 유닛 4로 만든다.

                           String tmp_rate = "4" + AppUtil.formatData(min.format(Double.valueOf(rate)).replace(".", "") , AppUtil.RIGHT, 9, '0');
                           dataInfo.put("RB24", tmp_rate);
                       }
                    }
            }
            
            int idx = 1;

            sbQuery = new StringBuffer();
            sbQuery.append("insert into kicc_transact ( ");
            sbQuery.append("    transid, seqno, merchantid, msgtype, field02, ");
            sbQuery.append("    field03, field04, field05, field06, field07, ");
            sbQuery.append("    field08, field09, field10, field11, field12, ");
            sbQuery.append("    field13, field14, field15, field16_1, field16_2, ");
            sbQuery.append("    field16_3, field16_4, field17, field18, field19, ");
            sbQuery.append("    field22_1, field22_2, field22_3, field22_4, status, ");
            sbQuery.append("    orgtransno, ");
            sbQuery.append("    reqdt, kicctid, cardmid, field21_1, field21_2, ");
            sbQuery.append("    mcpyn, mcpquotecurr, mcpquoteamount, mcpquoteamountminor, mcpconvrate, ");
            sbQuery.append("    mcpbasecurr, mcpkrwamount, mcpstatus, mcprateid, fin2fn, ");
            sbQuery.append("    cardtraceno, KEYINDEX ");
            sbQuery.append(") values ( ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            if(Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))) == 0)
                sbQuery.append("null, ");
            else sbQuery.append("?, ");
            sbQuery.append("    now(), ?, ?, ?, ?, ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            sbQuery.append("    ?, ?, ?, ?, ?, ");
            sbQuery.append("    ?, ? ");
            sbQuery.append(") ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.setString(idx++, seqNo);
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB01")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_MSGTYPE")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD02")));

            pstmt.setBytes(idx++, encFIELD02);
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD04")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD05")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD06")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD07")));

            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD08")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD09")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD10")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD11")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD12")));

            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD13")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD14")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD15")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD16_1")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD16_2")));

            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD16_3")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD16_4")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD17")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD18")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD19")));

            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD23_1")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD23_2")));
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD23_3")));
            /****************************************************************************
            * eWallete 2020.09.03.
            * eWallete의 경우에는 Track2Data의 카드번호=유효기간 이외의 데이터를 확인하기 위해
            * 저장한다.
            ****************************************************************************/ 
            if (AppUtil.checkNull(vanReq.get("VS_FIELD23_1")).equals("V"))
                pstmt.setString(idx++, AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB15")), AppUtil.LEFT, 40, ' ')+AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB16")), AppUtil.LEFT, 40, ' '));
            else
                pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD23_4")) + AppUtil.checkNull(vanReq.get("VS_FIELD23_5")).trim());
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_STATUS")));

            if(Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))) != 0)
                pstmt.setInt(idx++, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))));

            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_KICCTID")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_CARDMID")));
            //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
            //pstmt.setBytes(idx++, encFIELD21_1);
            pstmt.setBytes(idx++,  null);
            
            //Edgar 2024.10.10 Amex경우 CVV 4자리값이 이 필드에 들어가므로 치환하여 저장. 
            String temp_field21_2 = AppUtil.checkNull(vanReq.get("VS_FIELD21_2"));
            StringBuffer sbField21_2 = new StringBuffer();
            
            for(int i=0; i<temp_field21_2.length(); i++) {
            	char c = temp_field21_2.charAt(i);
            	if(Character.isDigit(c)) {
            		//숫자 인경우 치환한다. 
            		sbField21_2.append("*");
            	}else {
            		sbField21_2.append(c);
            	}
            }
            pstmt.setString(idx++, sbField21_2.toString());

            if(AppUtil.checkNull(dataInfo.get("RH03")).equals("0184") || AppUtil.checkNull(dataInfo.get("RH03")).equals("0181") || AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y") ){
                pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VD_MCPYN")));
                
                /********************* 부분 취소 적용  *********************************/ 
                if (AppUtil.checkNull(dataInfo.get("RH03")).equals("0204")) {  
                    pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB53")));
                    pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB54")));
                } else {
                    pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB21")));
                    pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB22")));
                }
               
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB23")));
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB24")));

                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB25")));
                /********************* 부분 취소 적용  *********************************/ 
                if (AppUtil.checkNull(dataInfo.get("RH03")).equals("0204")) {  
                    if (AppUtil.checkNull(dataInfo.get("RB25")).equals("410")) {
                        pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD07")));
                    } else {
                        // 원화 금액 계산 == (취소로 들어온 금액) * (kicc - filed07 금액) } / (kicc에서 승인되었던 원화금액)
                        double PartKRWAMT = (Double.parseDouble(AppUtil.checkNull(vanReq.get("VS_FIELD07"))) * Double.parseDouble(AppUtil.checkNull(dataInfo.get("RB26")))) / Double.parseDouble(AppUtil.checkNull(dataInfo.get("VD_ORGBASEAMT")));
                        DecimalFormat df = new DecimalFormat("0");
                        pstmt.setString(idx++, AppUtil.formatData(AppUtil.checkNull(df.format(PartKRWAMT)), AppUtil.RIGHT, 12, '0') );
                    }
                   
                } else {
                    pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB26")));
                }    
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB27")));
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB28")));
            }else{
                pstmt.setString(idx++, "N");
                pstmt.setString(idx++, "");
                pstmt.setString(idx++, "");
                pstmt.setString(idx++, "");
                pstmt.setString(idx++, "");

                pstmt.setString(idx++, "");
                pstmt.setString(idx++, "");
                pstmt.setString(idx++, "");
                pstmt.setString(idx++, "");
            }
            pstmt.setString(idx++, fin2fn);
            //pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_CARDTRACENO")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB73")));
            pstmt.setString(idx++, cipher.getKeyIndex());
            pstmt.executeUpdate();
            pstmt.close();

            // Edgar 2025.03.07 transno 가져오는 방식 변경 
            /*
            pstmt = conn.prepareStatement("select transno from kicc_transact where seqno=? order by transno desc  ");
            pstmt.setString(1, seqNo);
            */
            pstmt = conn.prepareStatement("select last_insert_id() as transno ");
            rs = pstmt.executeQuery();
            if(rs.next()){
                dataInfo.put("VD_TRANSNO", rs.getString("transno"));
            }
            rs.close();
            pstmt.close();
            
            vanReq.put("VD_SEQNO", seqNo);


        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::insertTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::insertTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }

    /**************************************************************************
    * 응답 데이터 저장
    ***************************************************************************/
    public void updateTransact(Map<String, String> vanRes, Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            if(AppUtil.checkNull(dataInfo.get("RH03")).equals("0160")){
                
            }else{
                
                String tradeDT = AppUtil.getDateTime();
                dataInfo.put("VD_DBTRADEDT", tradeDT);
                
                sbQuery.append("update kicc_transact set ");
                sbQuery.append("RFIELD02 = ?, ");
                sbQuery.append("RFIELD03 = ?, ");
                sbQuery.append("RFIELD04 = ?, ");
                sbQuery.append("RFIELD09 = ?, ");
                sbQuery.append("RFIELD10 = ?, ");
                sbQuery.append("RFIELD11 = ?, ");
                sbQuery.append("RFIELD12 = ?, ");
                sbQuery.append("RFIELD13 = ?, ");
                sbQuery.append("RFIELD14 = ?, ");
                sbQuery.append("RFIELD15 = ?, ");
                sbQuery.append("RFIELD16 = ?, ");
                sbQuery.append("RFIELD17 = ?, ");
                sbQuery.append("RFIELD18 = ?, ");
                sbQuery.append("mcpstatus = ?, ");
                sbQuery.append("cardtraceno = ?, ");
                
                sbQuery.append("resdt = ? ");
                sbQuery.append("where transno = ? ");
                pstmt = conn.prepareStatement(sbQuery.toString());
                pstmt.setString(1, AppUtil.checkNull(vanRes.get("VR_FIELD02")));
                pstmt.setString(2, AppUtil.checkNull(vanRes.get("VR_FIELD03")));
                pstmt.setString(3, AppUtil.checkNull(vanRes.get("VR_FIELD04")));
                pstmt.setString(4, AppUtil.checkNull(vanRes.get("VR_FIELD09")));
                pstmt.setString(5, AppUtil.checkNull(vanRes.get("VR_FIELD10")));
                pstmt.setString(6, AppUtil.checkNull(vanRes.get("VR_FIELD11")));
                pstmt.setString(7, AppUtil.checkNull(vanRes.get("VR_FIELD12")));
                pstmt.setString(8, AppUtil.checkNull(vanRes.get("VR_FIELD13")));
                if ((AppUtil.checkNull(dataInfo.get("RB08")).equals("C026") || AppUtil.checkNull(dataInfo.get("RB08")).equals("C008") ) && AppUtil.checkNull(vanRes.get("VR_FIELD16")).equals("01"))      // BCUPOP의 경우는 카드명을 "BCUPOP"로 입력한다
                    pstmt.setString(9, "BCUPOP");
                else
                     pstmt.setString(9, AppUtil.checkNull(vanRes.get("VR_FIELD14"))); 
                
                pstmt.setString(10, AppUtil.checkNull(vanRes.get("VR_FIELD15")));
                if ((AppUtil.checkNull(dataInfo.get("RB08")).equals("C026")  || AppUtil.checkNull(dataInfo.get("RB08")).equals("C008") ) && AppUtil.checkNull(vanRes.get("VR_FIELD16")).equals("01")) { // BCUPOP의 USD 경우는 "99"로 입력한다
                    if (AppUtil.checkNull(dataInfo.get("RB03")).trim().equals("USD"))
                        pstmt.setString(11, "99");
                    else
                        pstmt.setString(11, "01");
                } else {     
                    pstmt.setString(11, AppUtil.checkNull(vanRes.get("VR_FIELD16")));
                }
                pstmt.setString(12, AppUtil.checkNull(vanRes.get("VR_FIELD17")));
                pstmt.setString(13, AppUtil.checkNull(vanRes.get("VR_FIELD30")));
                //승인요청 시, 실패는 무조건 3으로 처리
                if(!AppUtil.checkNull(dataInfo.get("RH03")).equals("0200") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){
                    System.out.println("mcpstatus : 3");
                    pstmt.setString(14, "3");   //dcc declined
                }else{
                    System.out.println("mcpstatus : " + AppUtil.checkNull(dataInfo.get("RB27")) + ":");
                    pstmt.setString(14, AppUtil.checkNull(dataInfo.get("RB27")));
                }
                pstmt.setString(15, AppUtil.checkNull(vanRes.get("VR_FIELD31")).trim());
                
                pstmt.setString(16, dataInfo.get("VD_DBTRADEDT"));
                pstmt.setInt(17, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_TRANSNO"))));
                
                pstmt.executeUpdate();
                pstmt.close();
            }

        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::updateTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::updateTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }

    /**************************************************************************
    * 원거래 승인취소
    ***************************************************************************/
    public void updateOrgTransact(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
           conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

           pstmt = conn.prepareStatement("update kicc_transact set status='T002' where transno=? ");
           pstmt.setInt(1, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))));
           pstmt.executeUpdate();
           pstmt.close();
           
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::updateOrgTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::updateOrgTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }
    
    /**************************************************************************
    * 매입취소 요청 대상
    ***************************************************************************/
    public void updateEDIRefund(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("update kicc_transact set edistatus='02' where transno=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_TRANSNO"))));
            pstmt.executeUpdate();
            pstmt.close();

        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::updateEDIRefund]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::updateEDIRefund]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }

    /**************************************************************************
    * 매입취소 요청 대상
    ***************************************************************************/
    public void updateEDICancel(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("update kicc_transact set ");
            sbQuery.append("rfield03='O', ");
            sbQuery.append("rfield13=?, ");
            sbQuery.append("rfield16=?, ");
            sbQuery.append("rfield17=?, ");
            sbQuery.append("edistatus='02' ");
            sbQuery.append("where transno=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB51")), AppUtil.LEFT, 12, ' '));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("VD_RFIELD16")));
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("VD_RFIELD17")));
            pstmt.setInt(4, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_TRANSNO"))));
            pstmt.executeUpdate();
            pstmt.close();

        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::updateEDICancel]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::updateEDICancel]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }
    
    /**************************************************************************
    * 매입취소 요청 대상
    ***************************************************************************/
    public void updateEDIPartCancel(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("update kicc_transact set ");
            sbQuery.append("rfield03='O', ");
            sbQuery.append("rfield13=?, ");
            sbQuery.append("rfield16=?, ");
            sbQuery.append("rfield17=?, ");
            if (AppUtil.checkNull(dataInfo.get("RB08")).equals("C026"))                
                sbQuery.append("edistatus='02' ");
            else {
                if (AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y")) {
                    if (AppUtil.getCurrencyCode(AppUtil.checkNull(dataInfo.get("RB03"))).equals(AppUtil.checkNull(dataInfo.get("RB53"))))  // 자국통화와 기본통화가 같을경우..
                        sbQuery.append("edistatus='02' ");
                    else {
                        // 매입사가 외환/비씨가 아닐경우 배치에서 제외시킨다.
                        if (AppUtil.checkNull(dataInfo.get("VD_RFIELD16")).equals("03"))       // 외환 부분취소
                            sbQuery.append("edistatus='12' ");
                        else if (AppUtil.checkNull(dataInfo.get("VD_RFIELD16")).equals("01"))  // 비씨 부분취소
                            sbQuery.append("edistatus='12' ");
                        else  {
                            sbQuery.append("edistatus='02' ");
                        }
                    }
                } else 
                    sbQuery.append("edistatus='02' ");
            }
            sbQuery.append("where transno=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RB51")), AppUtil.LEFT, 12, ' '));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("VD_RFIELD16")));
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("VD_RFIELD17")));
            pstmt.setInt(4, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_TRANSNO"))));
            pstmt.executeUpdate();
            pstmt.close();

        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::updateEDIPartCancel]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::updateEDIPartCancel]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }

    
    /**************************************************************************
    * 원거래 데이터 확인
    ***************************************************************************/
    public void checkAuthTransact(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //엔화의 경우
            String orgAmt = AppUtil.checkNull(dataInfo.get("RB04"));
            if(AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY")){
                //JPY금액 * 100
                String jpyAmt = AppUtil.checkNull(dataInfo.get("RB04")) + "00";
                orgAmt = AppUtil.formatData(jpyAmt, AppUtil.RIGHT, 12, '0');
            }
            String tradeDT = AppUtil.getDateTime();
            dataInfo.put("VD_DBTRADEDT", tradeDT);
            sbQuery.append("select status, edistatus, rfield13, date_format(now(), '%Y%m%d%H%i%s') as resdt, ");
            sbQuery.append("timestampdiff(day,  timestamp(concat('20', substring(rfield04, 1, 6)), '000000'), timestamp(date_format(now(), '%Y%m%d'), '000000'))+1 as diff ");
            sbQuery.append("from EXIMGW.kicc_transact ");
            sbQuery.append("where transid=? and msgtype='1130' and rfield03='O' ");
            pstmt = conn.prepareStatement(sbQuery.toString());
System.out.println("[KICCDBProc::checkAuthTransact]transid : " + AppUtil.checkNull(dataInfo.get("RB02"))+":");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(!rs.getString("status").equals("T003"))
                    //Auth 거래 아님
                    throw new MyException("[KICCDBProc::checkAuthTransact]", "V019", "invalid transaction", "invalid transaction");
                else if(rs.getInt("diff") > 20)
                    //승인일 포함 20일 초과 거래
                    throw new MyException("[KICCDBProc::checkAuthTransact]", "V020", "invalid transaction", "invalid transaction");
                else if(!"00".equals(rs.getString("edistatus")))
                    //매입요청 대기 상태가 아닌 경우
                    throw new MyException("[KICCDBProc::checkAuthTransact]", "V021", "invalid transaction", "invalid transaction");
                else{
                    dataInfo.put("VR_FIELD13", rs.getString("rfield13"));
                    //dataInfo.put("VR_FIELD04", rs.getString("resdt"));
                 
                }
            }else{
                throw new MyException("[KICCDBProc::checkAuthTransact]", "V010", "승인거래 없음", "승인거래 없음");
            }
            rs.close();
            pstmt.close();
            
            //매입상태로 업데이트
            pstmt = conn.prepareStatement("update EXIMGW.kicc_transact set status='T000' where transid=? and msgtype='1130' and rfield03='O' ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.executeUpdate();
            pstmt.close();

        }catch(MyException e){
            throw e;
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[KICCDBProc::checkAuthTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[KICCDBProc::checkAuthTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }
}


