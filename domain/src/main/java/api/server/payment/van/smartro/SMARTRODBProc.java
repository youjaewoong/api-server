/*
 * KSNETDBProc.java
 *
 * Created on 2008년 10월 6일 (월), 오후 3:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package api.server.payment.van.smartro;


import api.server.common.config.datasource.DBConnectionManager;
import api.server.common.helper.AppUtil;
import api.server.common.helper.cipher.AbstractCipher;
import api.server.common.helper.cipher.AppCipher;
import api.server.common.helper.cipher.CipherFactory;
import api.server.payment.gateway.AbstractApp;
import api.server.payment.my.MyException;

import java.sql.*;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author 공현진
 */
public class SMARTRODBProc {
    private DBConnectionManager dbcm;

	private Connection conn;
	private PreparedStatement pstmt;
	private CallableStatement cstmt;
	private ResultSet rs;
	private StringBuffer sbQuery;
    private AppCipher appCipher;

    /** Creates a new instance of SMARTRODBProc */
    public SMARTRODBProc(DBConnectionManager dbcm) {
        this.dbcm = dbcm;
        appCipher = new AppCipher();
    }

    //가맹점 정보 조회
    public void selectMerchant(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            
            sbQuery.append("select a.bizno, a.siteurl, a.serverip, c.smartrotid as tid, ");
            sbQuery.append("case ");
            sbQuery.append("    when b.cardcode='C002' then c.merchantno ");        //삼성 AMEX 2013.06
            sbQuery.append("    else concat('00', substring(c.merchantno, 4, 9)) ");
            sbQuery.append("end as mid, a.status, ifnull(c.mcpcode, '') as mcpcode,  ");
            sbQuery.append("c.apprvcurrency ");
            sbQuery.append("from EXIMGW.keb_merchant_service a ");
            sbQuery.append("inner join EXIMGW.keb_rmer_memberid b on a.merchantid=b.merchantid ");
            sbQuery.append("inner join EXIMGW.keb_merchant c on b.memberno=c.merchantno ");
            sbQuery.append("where a.merchantid=? and b.currency=? and b.cardcode=? ");      //keb_rmer_memberid에 cardcode 추가 2013.06.10 by kong
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB03")));
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB08")));
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(!rs.getString("status").equals("A300"))
                    throw new MyException("[SMARTRODBProc::selectMerchant]", "S011", "SMARTRO 정지 및 해지 가맹점", "미등록 가맹점");
                else if(!rs.getString("apprvcurrency").equals(AppUtil.checkNull(dataInfo.get("RB03")))) //2013.04.19 by kong
                    throw new MyException("[SMARTRODBProc::selectMerchant]", "S012", "KEB 가맹점번호 승인통화 불일치", "승인통화 불일치");
                else{
                    dataInfo.put("VD_BIZNO", rs.getString("bizno"));
                    dataInfo.put("VD_TID", rs.getString("tid"));
                    dataInfo.put("VD_MID", rs.getString("mid"));
                    dataInfo.put("VD_SITEURL", AppUtil.checkNull(rs.getString("siteurl")));
                    dataInfo.put("VD_SERVERIP", AppUtil.checkNull(rs.getString("serverip")));
                    //MCP 사업자구분 코드 추가 2012-10-18 by kong
                    dataInfo.put("VD_MCPCODE", rs.getString("mcpcode"));
                }
            }else{
                throw new MyException("[SMARTRODBProc::selectMerchant]", "S010", "SMARTRO 미등록 가맹점", "미등록 가맹점");
            }
            rs.close();
            pstmt.close();
            
            //smartro_merchant(사용안함)-smartro_transact 테이블의 merchantid(FK)로 인해 smartro_merchant에 merchantid가 없는 경우 임의 등록(Exception 무시)
            //2013-01-17 by kong
            try{
                boolean isRegisted = false;
                pstmt = conn.prepareStatement("select * from EXIMGW.smartro_merchant where merchantid=? ");
                pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")));
                rs = pstmt.executeQuery();
                if(rs.next()){
                    isRegisted = true;
                }
                rs.close();
                pstmt.close();
                
                if(!isRegisted){
                    String name = "KRPartners";
                    String url = "www.eximbay.com";
                    sbQuery = new StringBuffer();
                    sbQuery.append("select a.name, a.url ");
                    sbQuery.append("from EXIMBAY.ms_merchant a  ");
                    sbQuery.append("inner join EXIMBAY.merchant_service b on a.merchantno=b.merchantno ");
                    sbQuery.append("where b.merchantid=? ");
                    pstmt = conn.prepareStatement(sbQuery.toString());
                    pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")));
                    rs = pstmt.executeQuery();
                    if(rs.next()){
                        name = rs.getString("name");
                        url = rs.getString("url");
                    }
                    rs.close();
                    pstmt.close();
                    
                    sbQuery = new StringBuffer();
                    sbQuery.append("insert into EXIMGW.smartro_merchant (merchantid, name, bizno, siteurl, serverip,tid, mid,  status, regdt, moddt) ");
                    sbQuery.append("values (?, ?, '1138637262', ?, '61.78.75.98', '1020380001', '00-957499916','A300', now(), now()) ");
                    pstmt = conn.prepareStatement(sbQuery.toString());
                    pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")));
                    pstmt.setString(2, name);
                    pstmt.setString(3, url);
                    pstmt.executeUpdate();
                    pstmt.close();
                }
                
            }catch(Exception e){
                System.out.println("[SMARTRODBProc::selectMerchant]insert smartro_merchant : " + e);
            }
            
        }catch(MyException e){
            throw e;
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::selectMerchant]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::selectMerchant]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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

    public void selectTransact(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            /*try{
            //BIN 데이터 확인(해외카드 전용)
            String cardNo = AppUtil.checkNull(dataInfo.get("RB09"));
System.out.println("cardNo : " + cardNo);
            pstmt = conn.prepareStatement("select * from eximbay.dbo.cardbin where bin=? ");
            pstmt.setString(1, cardNo.substring(0, 6));
            rs = pstmt.executeQuery();
            if(rs.next()){
                throw new MyException("[KSNETDBProc::selectTransact]", "S021", "해외카드만 사용가능", "해외카드만 사용가능");
            }
            rs.close();
            pstmt.close();
            }catch(Exception ee){
                System.out.println("Exception : " + ee);
            }*/

            //동일한 주문번호 확인
            pstmt = conn.prepareStatement("select * from smartro_transact where transid=? and msgtype=? ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            if(AppUtil.checkNull(dataInfo.get("RH03")).equals("0400"))
                pstmt.setString(2, "1210");
            else pstmt.setString(2, "1130");
            rs = pstmt.executeQuery();
            if(rs.next()){
                throw new MyException("[SMARTRODBProc::selectTransact]", "S020", "주문번호 중복", "주문번호 중복");
            }
            rs.close();
            pstmt.close();

        }catch(MyException e){
            throw e;
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::selectTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::selectTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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

    //원거래 데이터 추출
    public void selectOrgTransact(Map<String, String> vanReq, Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //기 매입취소 건인지 확인
            pstmt = conn.prepareStatement("select count(*) as cnt from smartro_transact where transid=? and rfield13=? and msgtype='1210' and rfield03='O' ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB51")).trim());
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(rs.getInt("cnt") > 0)
                    throw new MyException("[SMARTRODBProc::selectTransact]", "V018", "이미 취소 요청된 거래", "이미 취소 요청된 거래");
            }
            rs.close();
            pstmt.close();
            
            //엔화의 경우
            String orgAmt = AppUtil.checkNull(dataInfo.get("RB04"));
            if(AppUtil.checkNull(dataInfo.get("RB03")).equals("JPY")){
                //JPY금액 * 100
                String jpyAmt = AppUtil.checkNull(dataInfo.get("RB04")) + "00";
                orgAmt = AppUtil.formatData(jpyAmt, AppUtil.RIGHT, 12, '0');
            }

            pstmt = conn.prepareStatement("select * from smartro_transact where transid=? and rfield13=? and msgtype='1130' and rfield03='O' ");
System.out.println("[SMARTRODBProc::selectOrgTransact]transid : " + AppUtil.checkNull(dataInfo.get("RB02"))+":");
System.out.println("[SMARTRODBProc::selectOrgTransact]rfield13 : " + AppUtil.checkNull(dataInfo.get("RB51")).trim()+":");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB51")).trim());
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(rs.getString("status").equals("T002"))
                    throw new MyException("[SMARTRODBProc::selectTransact]", "V011", "이미 취소된 거래", "이미 취소된 거래");
                //else if(!(rs.getString("rfield13").trim()).equals(AppUtil.checkNull(dataInfo.get("RB51")).trim()))
                //    //원거래승인번호 일치여부
                //    throw new MyException("[SMARTRODBProc::selectTransact]", "V012", "승인거래 없음", "승인거래 없음");
                else if(!rs.getString("rfield04").startsWith(AppUtil.checkNull(dataInfo.get("RB52")).trim().substring(2, 8)))
                    //원거래일자 일치여부
                    throw new MyException("[SMARTRODBProc::selectTransact]", "V013", "승인거래 없음", "승인거래 없음");
                else if(!rs.getString("field04").equals(AppUtil.checkNull(dataInfo.get("RB07")).trim()))
                    //할부기간 일치여부
                    throw new MyException("[SMARTRODBProc::selectTransact]", "V014", "승인거래 없음", "승인거래 없음");
                else if(!rs.getString("field05").equals(AppUtil.checkNull(vanReq.get("VS_FIELD05"))))
                    //통화코드 일치여부
                    throw new MyException("[SMARTRODBProc::selectTransact]", "V015", "승인거래 없음", "승인거래 없음");
                else if(!rs.getString("field07").equals(orgAmt))
                    //승인금액 일치여부
                    throw new MyException("[SMARTRODBProc::selectTransact]", "V016", "승인거래 없음", "승인거래 없음");
                else{
                    AbstractCipher cipher = CipherFactory.getCipher(rs.getString("KEYINDEX"));

                    byte[] encField03 = rs.getBytes("field03");
                    //byte[] decField03 = appCipher.decryptData(encField03);
                    byte[] decField03 = cipher.decryptData(encField03);
                    
                    //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정
                    /*
                    byte[] encField21_1 = rs.getBytes("field21_1");
                    byte[] decField21_1 = cipher.decryptData(encField21_1);
                    */

                    String trkData = new String(decField03).trim();
                    String cardNo = trkData.substring(0, trkData.indexOf("="));
                    String expDT = trkData.substring(trkData.indexOf("=")+1, trkData.length());

                    dataInfo.put("RB09", cardNo);
                    dataInfo.put("RB10", expDT);
                    
                    //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
                    //dataInfo.put("RB11", new String(decField21_1));
                    
                    dataInfo.put("VD_ORGTRANSNO", rs.getString("transno"));
                    dataInfo.put("VD_TID", rs.getString("tid"));  //원거래 정보로
                    dataInfo.put("VD_MID", rs.getString("mid"));    //원거래 정보로

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
                    }
                    
                    //MCP 취소거래 시, 16, 17은 응답에 포합되지 않아, 원거래 데이터 사용 20120510 by kong
                    dataInfo.put("VD_RFIELD16", rs.getString("rfield16"));
                    dataInfo.put("VD_RFIELD17", AppUtil.checkNull(rs.getString("rfield17")));

                    dataInfo.put("VD_MCPYN", AppUtil.checkNull(rs.getString("mcpyn")));
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
                    
                }
            }else{
                throw new MyException("[SMARTRODBProc::selectTransact]", "V010", "승인거래 없음", "승인거래 없음");
            }
            rs.close();
            pstmt.close();

            pstmt = conn.prepareStatement("select * from smartro_transact where orgtransno=? and rfield03='O' and edistatus in ('02', '03', '60') ");
            pstmt.setInt(1, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))));
            rs = pstmt.executeQuery();
            if(rs.next()){
                throw new MyException("[SMARTRODBProc::selectTransact]", "V017", "이미 취소요청된 거래", "이미 취소요청된 거래");
            }
            rs.close();
            pstmt.close();
            

            
        }catch(MyException e){
            throw e;
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::selectOrgTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::selectOrgTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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

    //요청 데이터 저장
    public void insertTransact(Map<String, String> vanReq, Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            AbstractCipher cipher = CipherFactory.getCipher(AbstractApp.g_appCfg.getKmsCekLastKeyIndex());
            byte[] encFIELD02 = cipher.encryptData(AppUtil.checkNull(vanReq.get("VS_FIELD03")).getBytes());
            //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
			//byte[] encFIELD21_1 = cipher.encryptData(AppUtil.checkNull(vanReq.get("VS_FIELD21_1")).getBytes());

            String curDate = AppUtil.getDateTime().substring(0, 8);
            int maxNo = 0;

            try{
                conn.setAutoCommit(false);

                pstmt = conn.prepareStatement("lock tables max_id write");
                pstmt.executeUpdate();
                pstmt.close();
                
                pstmt = conn.prepareStatement("select maxsmartro from max_id where lastdtsmartro=? ");
                pstmt.setString(1, curDate);
                rs = pstmt.executeQuery();
                if(rs.next()){
                    maxNo = rs.getInt("maxsmartro");
                }
                rs.close();
                pstmt.close();

                if(maxNo == 0) maxNo = 1;
                else maxNo++;
System.out.println("maxNo: " + maxNo);

                pstmt = conn.prepareStatement("update max_id set maxsmartro=?, lastdtsmartro=? ");
                pstmt.setInt(1, maxNo);
                pstmt.setString(2, curDate);
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("unlock tables ");
                pstmt.executeUpdate();
                pstmt.close();


                conn.commit();
            }catch(Exception ie){
System.out.println("Exception : " + ie);
                conn.rollback();
            }finally{
                conn.setAutoCommit(true);
            }



            /*pstmt = conn.prepareStatement("call sp_getSMARTROMaxId(?)  ");
            pstmt.setString(1, curDate);
            rs = pstmt.executeQuery();
			if(rs.next()){
                maxNo = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
System.out.println("maxNo: " + maxNo);*/
            
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
                    if(rs.getString("mcpcode").equals("KP")){
                        fin2fn = "K";
                    }
                }
                rs.close();
                pstmt.close();
            }
System.out.println("fin2fn: " + fin2fn);            
            

            String seqNo = curDate.substring(2, 8) + AppUtil.formatData(maxNo, AppUtil.RIGHT, 6, '0');
System.out.println("seqNo: " + seqNo);

            int idx = 1;

            sbQuery = new StringBuffer();
            sbQuery.append("insert into smartro_transact ( ");
            sbQuery.append("    transid, seqno, merchantid, msgtype, field02, ");
            sbQuery.append("    field03, field04, field05, field06, field07, ");
            sbQuery.append("    field08, field09, field10, field11, field12, ");
            sbQuery.append("    field13, field14, field15, field16_1, field16_2, ");
            sbQuery.append("    field16_3, field16_4, field17, field18, field19, ");
            sbQuery.append("    field22_1, field22_2, field22_3, field22_4, status, ");
            sbQuery.append("    orgtransno, ");
            sbQuery.append("    reqdt, tid, mid, field21_1, field21_2, ");
            sbQuery.append("    mcpyn, mcpquotecurr, mcpquoteamount, mcpquoteamountminor, mcpconvrate, ");
            sbQuery.append("    mcpbasecurr, mcpkrwamount, mcpstatus, mcprateid, fin2fn, KEYINDEX ");
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
            sbQuery.append("    ?, ?, ?, ?, ?, ? ");
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
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD23_4")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_STATUS")));

            if(Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))) != 0)
                pstmt.setInt(idx++, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))));

            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_TID")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_MID")));
            
            //Edgar 2024.08.02 CVV값 미저장 변경으로 인한 수정 
            //pstmt.setBytes(idx++, encFIELD21_1);
            pstmt.setBytes(idx++, null);
            
            pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VS_FIELD21_2")));

            if(AppUtil.checkNull(dataInfo.get("RH03")).equals("0181") || AppUtil.checkNull(dataInfo.get("VD_MCPYN")).equals("Y")){
                pstmt.setString(idx++, AppUtil.checkNull(vanReq.get("VD_MCPYN")));
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB21")));
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB22")));
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB23")));
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB24")));

                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB25")));
                pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB26")));
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
            pstmt.setString(idx++, cipher.getKeyIndex());

            pstmt.executeUpdate();
            pstmt.close();

            pstmt = conn.prepareStatement("select transno from smartro_transact where seqno=? order by transno desc  ");
            pstmt.setString(1, seqNo);
            rs = pstmt.executeQuery();
            if(rs.next()){
                dataInfo.put("VD_TRANSNO", rs.getString("transno"));
            }
            rs.close();
            pstmt.close();
            
            /*
            if(AppUtil.checkNull(dataInfo.get("RH03")).equals("0180")){
                //DCC 승인요청 시, 환율조회 Tracking Code에 해당하는 SMARTRO_DCC_TRANSACT테이블의 REFTRANSNO 필드 update
                pstmt = conn.prepareStatement("update smartro_dcc_transact set reftransno=? where seqno=?  ");
                pstmt.setInt(1, (int)AppUtil.checkNullInt(dataInfo.get("VD_TRANSNO")));
                pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB19")));
                pstmt.executeUpdate();
                pstmt.close();
            }*/

            vanReq.put("VD_SEQNO", seqNo);


        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::insertTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::insertTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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

    //응답 데이터 저장
    public void updateTransact(Map<String, String> vanRes, Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            if(AppUtil.checkNull(dataInfo.get("RH03")).equals("0160")){
                /*sbQuery.append("update smartro_dcc_transact set ");
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
                sbQuery.append("RFIELD19 = ?, ");
                sbQuery.append("RFIELD20 = ?, ");
                sbQuery.append("RFIELD21 = ?, ");
                sbQuery.append("RFIELD22 = ?, ");
                sbQuery.append("RFIELD23 = ?, ");
                sbQuery.append("RFIELD24 = ?, ");
                sbQuery.append("RFIELD25 = ?, ");
                sbQuery.append("resdt = now() ");
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
                pstmt.setString(9, AppUtil.checkNull(vanRes.get("VR_FIELD14")));
                pstmt.setString(10, AppUtil.checkNull(vanRes.get("VR_FIELD15")));
                pstmt.setString(11, AppUtil.checkNull(vanRes.get("VR_FIELD16")));
                pstmt.setString(12, AppUtil.checkNull(vanRes.get("VR_FIELD17")));
                pstmt.setString(13, AppUtil.checkNull(vanRes.get("VR_FIELD30")));
                pstmt.setString(14, AppUtil.checkNull(vanRes.get("VR_FIELD20")));
                pstmt.setString(15, AppUtil.checkNull(vanRes.get("VR_FIELD21")));
                pstmt.setString(16, AppUtil.checkNull(vanRes.get("VR_FIELD22")));
                pstmt.setString(17, AppUtil.checkNull(vanRes.get("VR_FIELD23")));
                pstmt.setString(18, AppUtil.checkNull(vanRes.get("VR_FIELD24")));
                pstmt.setString(19, AppUtil.checkNull(vanRes.get("VR_FIELD25")));
                pstmt.setString(20, AppUtil.checkNull(vanRes.get("VR_FIELD26")));
                pstmt.setInt(21, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_TRANSNO"))));
                pstmt.executeUpdate();
                pstmt.close();*/
            }else{
                sbQuery.append("update smartro_transact set ");
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
                sbQuery.append("resdt = now() ");
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
                pstmt.setString(9, AppUtil.checkNull(vanRes.get("VR_FIELD14")));
                pstmt.setString(10, AppUtil.checkNull(vanRes.get("VR_FIELD15")));
                pstmt.setString(11, AppUtil.checkNull(vanRes.get("VR_FIELD16")));
                pstmt.setString(12, AppUtil.checkNull(vanRes.get("VR_FIELD17")));
                pstmt.setString(13, AppUtil.checkNull(vanRes.get("VR_FIELD30")));
                //승인요청 시, 실패는 무조건 3으로 처리
                //if(AppUtil.checkNull(dataInfo.get("RB27")).equals("0") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){
                if(!AppUtil.checkNull(dataInfo.get("RH03")).equals("0200") && !AppUtil.checkNull(vanRes.get("VR_FIELD03")).equals("O")){
                    System.out.println("mcpstatus : 3");
                    pstmt.setString(14, "3");   //dcc declined
                }else{
                    System.out.println("mcpstatus : " + AppUtil.checkNull(dataInfo.get("RB27")) + ":");
                    pstmt.setString(14, AppUtil.checkNull(dataInfo.get("RB27")));
                }
                pstmt.setString(15, AppUtil.checkNull(vanRes.get("VR_FIELD32")));
                pstmt.setInt(16, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_TRANSNO"))));
                pstmt.executeUpdate();
                pstmt.close();
            }

        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::updateTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::updateTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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

    //원거래 승인취소
    public void updateOrgTransact(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            /*//매입여부 체크
            String ediStatus = "";
            pstmt = conn.prepareStatement("select edistatus from smartro_transact where transno= ? ");
            pstmt.setInt(1, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))));
            rs = pstmt.executeQuery();
            if(rs.next()){
                ediStatus = rs.getString("edistatus");
            }
            rs.close();
            pstmt.close();*/

            //if(ediStatus.equals("00")){
                //매입 전 취소
                pstmt = conn.prepareStatement("update smartro_transact set status='T002' where transno=? ");
                pstmt.setInt(1, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_ORGTRANSNO"))));
                pstmt.executeUpdate();
                pstmt.close();
            /*}else{
                //매입 후 취소
                //updateEDICancel(dataInfo);
            }*/
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::updateOrgTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::updateOrgTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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
    
    //매입취소 요청 대상
    public void updateEDIRefund(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("update smartro_transact set edistatus='02' where transno=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, Integer.parseInt(AppUtil.checkNull(dataInfo.get("VD_TRANSNO"))));
            pstmt.executeUpdate();
            pstmt.close();

        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::updateEDIRefund]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::updateEDIRefund]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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
    
    //매입취소 요청 대상
    public void updateEDICancel(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();

        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("update smartro_transact set ");
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
			throw new MyException("[SMARTRODBProc::updateOrgTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::updateOrgTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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

    //원거래 데이터 확인
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
            
            sbQuery.append("select status, edistatus, rfield13, date_format(now(), '%Y%m%d%H%i%s') as resdt, ");
            sbQuery.append("timestampdiff(day,  timestamp(concat('20', substring(rfield04, 1, 6)), '000000'), timestamp(date_format(now(), '%Y%m%d'), '000000'))+1 as diff ");
            sbQuery.append("from EXIMGW.smartro_transact ");
            sbQuery.append("where transid=? and msgtype='1130' and rfield03='O' ");
            pstmt = conn.prepareStatement(sbQuery.toString());
System.out.println("[SMARTRODBProc::checkAuthTransact]transid : " + AppUtil.checkNull(dataInfo.get("RB02"))+":");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            rs = pstmt.executeQuery();
            if(rs.next()){
                if(!rs.getString("status").equals("T003"))
                    //Auth 거래 아님
                    throw new MyException("[SMARTRODBProc::checkAuthTransact]", "V019", "invalid transaction", "invalid transaction");
                else if(rs.getInt("diff") > 20)
                    //승인일 포함 20일 초과 거래
                    throw new MyException("[SMARTRODBProc::checkAuthTransact]", "V020", "invalid transaction", "invalid transaction");
                else if(!"00".equals(rs.getString("edistatus")))
                    //매입요청 대기 상태가 아닌 경우
                    throw new MyException("[SMARTRODBProc::checkAuthTransact]", "V021", "invalid transaction", "invalid transaction");
                else{
                    dataInfo.put("VR_FIELD13", rs.getString("rfield13"));
                    dataInfo.put("VR_FIELD04", rs.getString("resdt"));
                }
            }else{
                throw new MyException("[SMARTRODBProc::checkAuthTransact]", "V010", "승인거래 없음", "승인거래 없음");
            }
            rs.close();
            pstmt.close();
            
            //매입상태로 업데이트
            pstmt = conn.prepareStatement("update EXIMGW.smartro_transact set status='T000' where transid=? and msgtype='1130' and rfield03='O' ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB02")));
            pstmt.executeUpdate();
            pstmt.close();

        }catch(MyException e){
            throw e;
        }catch(SQLException e){
			if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[SMARTRODBProc::checkAuthTransact]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[SMARTRODBProc::checkAuthTransact]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
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


