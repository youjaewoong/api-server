package api.server.payment.gateway.v100;

import api.server.common.config.datasource.DBConnectionManager;
import api.server.common.helper.AppUtil;
import api.server.common.helper.SendMail;
import api.server.common.helper.cipher.AbstractCipher;
import api.server.common.helper.cipher.CipherFactory;
import api.server.payment.gateway.AbstractApp;
import api.server.payment.gateway.AppDBProc;
import api.server.payment.my.MyException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * ******************************************************************************************
 *
 * @author KRPARTNERS 오병흔
 * <p>
 * *******************************************************************************************
 */

public class VerDBProc extends AppDBProc {

    public VerDBProc(DBConnectionManager dbcm) {
        super(dbcm);
    }

    //결제요청 데이터 유효성 확인 및 부가 데이터 추출
    public void checkRequest(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            System.out.println("[VerDBProc::checkRequest]currency : " + AppUtil.checkNull(dataInfo.get("RB03")).trim());
            System.out.println("[VerDBProc::checkRequest]merchantid : " + AppUtil.checkNull(dataInfo.get("RB01")).trim());
            //승인통화 및 부가 데이터 추출(기본카드 : VISA)
            sbQuery.append("select b.name, b.service, b.url, a.merchantno, a.status as sstatus, ");
            sbQuery.append("a.cvvyn, a.loginyn, a.verifyyn, ifnull(a.verifylimit, 0) as verifylimit, a.dupchkyn, b.status as mstatus, b.type, ");
            sbQuery.append("ifnull(c.amount, 0) as limitamount, ifnull(a.secretkey, '') as secretkey, ifnull(a.krmerchantid, '') as krmerchantid, ");
            sbQuery.append("ifnull(a.retrymerchantid, '') as retrymerchantid, ifnull(a.rmmalert, 'N') as rmmalert, ");
            sbQuery.append("ifnull(a.loginviewyn, 'Y') as loginviewyn, ifnull(a.secureyn, 'N') as secureyn, ");
            sbQuery.append("ifnull(a.non3dallowyn, 'Y') as non3dallowyn, ifnull(a.chkresponse, 'N') as chkresponse, ");
            sbQuery.append("ifnull(a.logoimg, '') as logoimg, ifnull(a.paytitle, ':: Eximbay ::') as paytitle, ");
            sbQuery.append("ifnull(a.dccyn, 'N') as dccyn, ifnull(a.mcpyn, 'N') as mcpyn, ifnull(a.mcpbasecurrency, '') as mcpfixedbasecurrcode ");
            sbQuery.append("from EXIMBAY.merchant_service a ");
            sbQuery.append("inner join EXIMBAY.ms_merchant b on a.merchantno=b.merchantno ");
            sbQuery.append("left outer join EXIMBAY.limit_transact c on a.merchantno=c.merchantno and c.servicecode='S001' and c.currency=? ");
            sbQuery.append("where a.merchantid=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB03")).trim());
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB01")).trim());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                //entity.setMerchantNo(rs.getInt("merchantno"));
                //entity.setCvvYN(rs.getString("cvvyn"));
                //entity.setDupChkYN(rs.getString("dupchkyn"));
                //entity.setDccYN(rs.getString("dccyn"));
                //entity.setMcpYN(rs.getString("mcpyn"));
                //entity.setMcpFixedBaseCurrCode(rs.getString("mcpfixedbasecurrcode"));
                dataInfo.put("VD_MCPYN", rs.getString("mcpyn"));
                dataInfo.put("VD_MERCHANTNO", rs.getString("merchantno"));

                //가맹점 및 서비스 상태 확인
                if (!rs.getString("sstatus").equals("A000"))
                    throw new MyException("[VerDBProc::checkRequest]", "W103", "invalid service", "invalid service");
                else if (!rs.getString("mstatus").equals("A000"))
                    throw new MyException("[VerDBProc::checkRequest]", "W104", "invalid merchant", "invalid merchant");
                else if (rs.getInt("merchantno") == 0)
                    throw new MyException("[VerDBProc::checkRequest]", "W105", "invalid merchant", "invalid merchant");

                //프리미엄 가맹점 확인
                if (!rs.getString("type").equals("A201"))
                    throw new MyException("[VerDBProc::checkRequest]", "W107", "invalid merchant", "invalid merchant");
            }
            rs.close();
            pstmt.close();

        } catch (MyException e) {
            throw e;
        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::checkRequest]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::checkRequest]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }


    public void reqTransactWeb(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            conn.setAutoCommit(false);

            //IP로 접속국가 구하기
            String countryCode = "";
            String countryName = "";
            String addr = AppUtil.checkNull(dataInfo.get("RB91")).trim();
            if (!addr.equals("")) {
                addr = addr.replace('.', '/');
                String[] addrArray = addr.split("/");

                long ipNum = 16777216 * Long.parseLong(addrArray[0])
                        + 65536 * Long.parseLong(addrArray[1])
                        + 256 * Long.parseLong(addrArray[2])
                        + Long.parseLong(addrArray[3]);

                pstmt = conn.prepareStatement("select country, name from EXIMBAY.geoip where end_num > ? and begin_num < ?  limit 1 ");
                pstmt.setLong(1, ipNum);
                pstmt.setLong(2, ipNum);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    countryCode = rs.getString("country");
                    countryName = rs.getString("name");
                }
                rs.close();
                pstmt.close();
            }


            double amt = AppUtil.checkNullDouble(dataInfo.get("VD_AMT"));

            sbQuery.append("insert into EXIMBAY.transact_web ( ");
            sbQuery.append("	merchantid, ref, cur, amt, email,  ");
            sbQuery.append("	buyer, tel, accessip, country, accesscountry, ");
            sbQuery.append("	reqdt, lang ");
            sbQuery.append(") values (  ");
            sbQuery.append("	?, ?, ?, ?, ?,  ");
            sbQuery.append("	?, ?, ?, ?, ?,  ");
            sbQuery.append("	now(), 'EN' ");
            sbQuery.append(")  ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")).trim());
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB90")).trim());
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB03")).trim());
            pstmt.setDouble(4, amt);
            pstmt.setString(5, AppUtil.checkNull(dataInfo.get("RB94")).trim());
            pstmt.setString(6, AppUtil.checkNull(dataInfo.get("RB92")).trim());
            pstmt.setString(7, AppUtil.checkNull(dataInfo.get("RB93")).trim());
            pstmt.setString(8, AppUtil.checkNull(dataInfo.get("RB91")).trim());
            pstmt.setString(9, countryCode);
            pstmt.setString(10, countryName);
            pstmt.executeUpdate();
            pstmt.close();

            pstmt = conn.prepareStatement("select last_insert_id() as webno ");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dataInfo.put("VD_WEBNO", rs.getString("webno"));
            }
            rs.close();
            pstmt.close();

            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ie) {
            }
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::reqTransactWeb]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ie) {
            }
            throw new MyException("[VerDBProc::reqTransactWeb]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    conn.setAutoCommit(true);
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }
    
    /*public void checkMinAmount(Map<String, String> dataInfo) throws MyException{
        sbQuery = new StringBuffer();
        
        try{
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            
            sbQuery.append("select * ");
			sbQuery.append("from service_currency ");
			sbQuery.append("where merchantid=? and cardcode=? and reqcurrency=? ");
			pstmt = conn.prepareStatement(sbQuery.toString());
			pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")).trim());
			pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB08")).trim());
			pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB03")).trim());
			rs = pstmt.executeQuery();
			if(rs.next()){
				dataInfo.put("VD_PAYTO", rs.getString("payto"));
                dataInfo.put("VD_MINAMOUNT", rs.getString("minamount"));
                dataInfo.put("VD_APPRVCUR", rs.getString("apprvcurrency"));
			}
            rs.close();
            pstmt.close();
        }catch(SQLException e){
            if(dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
			throw new MyException("[VerDBProc::checkMinAmount]", MyException.SY03, "SQLException:"+e, MyException.sysErrMsg);
		}catch(Exception e){
			throw new MyException("[VerDBProc::checkMinAmount]", MyException.SY03, "Exception:"+e, MyException.sysErrMsg);
		}finally{
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(dbcm != null){
					dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
				}
			}catch(Exception ie){}
		}
    }*/

    public void checkApprvCurrency(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //카드BIN 체크			
            dataInfo.put("VD_ISSUEDKR", "N");
            String cardNo = AppUtil.checkNull(dataInfo.get("RB09"));
            pstmt = conn.prepareStatement("select * from EXIMBAY.kr_cardbin where bin=? ");
            pstmt.setString(1, cardNo.substring(0, 6));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dataInfo.put("VD_ISSUEDKR", "Y");
                //return;
            }
            rs.close();
            pstmt.close();

            sbQuery = new StringBuffer();
            sbQuery.append("select ifnull(a.payto, '') as payto1, ifnull(b.payto, '') as payto2, a.apprvcurrency, ");
            sbQuery.append("ifnull(d.mcpyn, 'N') as mcpyn, ifnull(a.minamount, 0) as minamount ");
            sbQuery.append("from EXIMBAY.service_currency a ");
            sbQuery.append("inner join EXIMBAY.van b on a.vanno=b.vanno ");
            sbQuery.append("inner join EXIMBAY.merchant_service d on a.merchantid=d.merchantid ");
            sbQuery.append("where a.merchantid=? and a.cardcode=? and a.reqcurrency=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")).trim());
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB08")).trim());
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB03")).trim());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (!rs.getString("payto1").equals(""))
                    dataInfo.put("VD_PAYTO", rs.getString("payto1"));
                else dataInfo.put("VD_PAYTO", rs.getString("payto2"));

                dataInfo.put("VD_APPRVCUR", rs.getString("apprvcurrency"));
                //dataInfo.put("VD_DCCYN", rs.getString("dccyn"));
                dataInfo.put("VD_MCPYN", rs.getString("mcpyn"));
                dataInfo.put("VD_MINAMOUNT", rs.getString("minamount"));
            }
            rs.close();
            pstmt.close();

            //요청통화와 승인통화가 일치해야 함. --> transact 테이블에 exgno, vanexgno FK 삭제 후, comment
            double amt = AppUtil.checkNullDouble(dataInfo.get("VD_AMT"));

            sbQuery = new StringBuffer();
            sbQuery.append("select exgno, tocurrency, round(? * toamount/framount, 2) as apprvamount ");
            sbQuery.append("from EXIMBAY.exchange_rate ");
            sbQuery.append("where frcurrency=? and tocurrency=? ");
            sbQuery.append("order by exgno desc  ");
            sbQuery.append("limit 0, 1 ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setDouble(1, amt);
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB03")).trim());
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("VD_APPRVCUR")).trim());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dataInfo.put("VD_EXGNO", rs.getString("exgno"));
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::checkApprvCurrency]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::checkApprvCurrency]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    public void checkMCP(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            String baseCurCode = AppUtil.checkNull(dataInfo.get("RB03")).trim();
            String baseAmt = AppUtil.checkNull(dataInfo.get("RB04")).trim();
            String rateID = AppUtil.checkNull(dataInfo.get("RB28")).trim();
            String quoteCurId = AppUtil.checkNull(dataInfo.get("RB21")).trim();
            String quoteAmt = AppUtil.checkNull(dataInfo.get("RB22")).trim();
            String quoteMinor = AppUtil.checkNull(dataInfo.get("RB23")).trim();
            String convertRate = AppUtil.checkNull(dataInfo.get("RB24")).trim();
            String baseCurId = AppUtil.checkNull(dataInfo.get("RB25")).trim();
            double krwAmt = AppUtil.checkNullDouble(dataInfo.get("RB26"));
            String siteCurCode = AppUtil.checkNull(dataInfo.get("RB96")).trim();
            String siteAmt = AppUtil.checkNull(dataInfo.get("RB97")).trim();
            System.out.println("[VerDBProc::checkMCP]baseCurCode : " + baseCurCode);
            System.out.println("[VerDBProc::checkMCP]baseAmt : " + baseAmt);
            System.out.println("[VerDBProc::checkMCP]rateID : " + rateID);
            System.out.println("[VerDBProc::checkMCP]quoteCurId : " + quoteCurId);
            System.out.println("[VerDBProc::checkMCP]quoteAmt : " + quoteAmt);
            System.out.println("[VerDBProc::checkMCP]quoteMinor : " + quoteMinor);
            System.out.println("[VerDBProc::checkMCP]convertRate : " + convertRate);
            System.out.println("[VerDBProc::checkMCP]baseCurId : " + baseCurId);
            System.out.println("[VerDBProc::checkMCP]krwAmt : " + krwAmt);
            System.out.println("[VerDBProc::checkMCP]siteCurCode : " + siteCurCode);
            System.out.println("[VerDBProc::checkMCP]siteAmt : " + siteAmt);

            DecimalFormat df = new DecimalFormat("########.##########");
            int convertMinor = Integer.parseInt(convertRate.substring(0, 1));
            double convert = Double.parseDouble(convertRate.substring(1, convertRate.length())) * Math.pow(10, 0 - convertMinor);
            convert = Double.parseDouble(df.format(convert));

            double quote = Double.parseDouble(quoteAmt) * Math.pow(10, 0 - Integer.parseInt(quoteMinor));

            //사이트통화가 base 통화와 일치하지 않는 경우, 사이트통화 기준으로 통화 변경
            if (!siteCurCode.equals("") && !baseCurCode.equals(siteCurCode)) {
                baseCurCode = siteCurCode;
                baseAmt = siteAmt;

                pstmt = conn.prepareStatement("select currencyid from EXIMGW.iso4217_currencies where currencycode=? ");
                pstmt.setString(1, baseCurCode);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    baseCurId = rs.getString("currencyid");
                }
                rs.close();
                pstmt.close();
                System.out.println("[VerDBProc::checkMCP]baseCurCode2 : " + baseCurCode);
                System.out.println("[VerDBProc::checkMCP]baseAmt2 : " + baseAmt);
                System.out.println("[VerDBProc::checkMCP]baseCurId2 : " + baseCurId);

            }

            //환율조회 성공 거래만 확인 
            if (AppUtil.checkNull(dataInfo.get("RB27")).equals("0")) {
                double base = Double.parseDouble(baseAmt);
                if (!baseCurCode.equals("KRW") && !baseCurCode.equals("JPY")) base = base / 100;
                double quote2 = Math.round(base * convert * 100) / (double) 100;

                sbQuery.append("select b.currencyid as basecurid, c.currencyid as quotecurid, a.*  ");
                sbQuery.append("from EXIMBAY.mcp_exgrt_list a ");
                sbQuery.append("left outer join EXIMGW.iso4217_currencies b on a.basecur=b.currencycode ");
                sbQuery.append("left outer join EXIMGW.iso4217_currencies c on a.quotecur=c.currencycode ");
                sbQuery.append("where rateid=? ");
                pstmt = conn.prepareStatement(sbQuery.toString());
                pstmt.setString(1, rateID);
                rs = pstmt.executeQuery();
                if (rs.next()) {

                    System.out.println("[VerDBProc::checkMCP]base 통화 비교 : " + baseCurCode + "," + rs.getString("basecur"));
                    System.out.println("[VerDBProc::checkMCP]base currencyid 비교 : " + baseCurId + "," + AppUtil.formatData(rs.getString("basecurid"), AppUtil.RIGHT, 3, '0'));
                    System.out.println("[VerDBProc::checkMCP]자국통화코드 비교 : " + quoteCurId + "," + rs.getString("quotecurid"));
                    System.out.println("[VerDBProc::checkMCP]환율 비교 : " + convert + "," + rs.getDouble("convrate"));
                    System.out.println("[VerDBProc::checkMCP]자국통화금액 확인 : " + quote2 + "," + quote);
                    System.out.println("[VerDBProc::checkMCP]rateendtime : " + Integer.parseInt(rs.getString("rateendtime").substring(0, 10)) + "," + Integer.parseInt(AppUtil.getDateTime().substring(0, 10)));
                    System.out.println("[VerDBProc::checkMCP]krw금액 확인 : " + krwAmt + "," + Math.floor(Double.parseDouble(siteAmt) * rs.getDouble("basekrw")));

                    //base 통화 비교 
                    if (!baseCurCode.equals(rs.getString("basecur")))
                        throw new MyException("[VerDBProc::checkMCP]", "W604", "Not matched base currency code", "Not matched base currency code");
                        //base currencyid 비교
                    else if (!baseCurId.equals(AppUtil.formatData(rs.getString("basecurid"), AppUtil.RIGHT, 3, '0')))
                        throw new MyException("[VerDBProc::checkMCP]", "W605", "Not matched base currency id", "Not matched base currency id");
                        //자국통화코드 비교
                    else if (!quoteCurId.equals("") && !quoteCurId.equals(rs.getString("quotecurid")))
                        throw new MyException("[VerDBProc::checkMCP]", "W601", "Not matched quotation currency id", "Not matched quotation currency id");
                        //환율 비교
                    else if (convert != rs.getDouble("convrate"))
                        throw new MyException("[VerDBProc::checkMCP]", "W602", "Not matched convert rate", "Not matched convert rate");
                        //자국통화금액 확인
                    else if (quote2 != quote)
                        throw new MyException("[VerDBProc::checkMCP]", "W603", "Not matched quotation amount", "Not matched quotation amount");
                        //원화금액 확인
                    else if (krwAmt != Math.floor(Double.parseDouble(siteAmt) * rs.getDouble("basekrw"))) {
                        throw new MyException("[VerDBProc::checkMCP]", "W607", "Not matched KRW amount", "Not matched KRW amount");
                    }

                    //환율 endtime이 현재 시간보다 작을 경우 알림 처리
                    if (Integer.parseInt(rs.getString("rateendtime").substring(0, 10)) < Integer.parseInt(AppUtil.getDateTime().substring(0, 10))) {
                        try {
                            SendMail sm = new SendMail();
                            StringBuffer sbMail = new StringBuffer();
                            sbMail.append("Rate ID : " + rateID);
                            sbMail.append("<br>Current Time : " + AppUtil.getDateTime());
                            sbMail.append("<br>Rate End Time : " + rs.getString("rateendtime"));
                            sbMail.append("<br>Transaction Ref : " + AppUtil.checkNull(dataInfo.get("RB90")));
                            sm.send("kong96@eximbay.com", "EximGateway", "System Alert(MCP)", sbMail.toString());
                        } catch (Exception e) {
                            System.out.println("[VerDBProc::checkMCP:sendMail]Exception : " + e);
                        }
                    }
                } else {
                    throw new MyException("[VerDBProc::checkMCP]", "W600", "Invalid rateID", "Invalid rateID");
                }
                rs.close();
                pstmt.close();
            }

            //원화금액 셋팅
            if (krwAmt == 0) {
                df = new DecimalFormat("############");

                pstmt = conn.prepareStatement("select basicrate from EXIMBAY.keb_exgrt_list where currency=? order by exglno desc limit 1");
                pstmt.setString(1, baseCurCode);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    krwAmt = AppUtil.checkNullDouble(baseAmt) * rs.getDouble("basicrate");
                    if (baseCurCode.equals("JPY")) krwAmt = krwAmt / 100;
                    krwAmt = Math.floor(krwAmt);
                    dataInfo.put("RB26", AppUtil.formatData(df.format(krwAmt), AppUtil.RIGHT, 12, '0'));
                    System.out.println("[VerDBProc::checkMCP]krwAmt 추출 : " + dataInfo.get("RB26"));
                } else {
                    throw new MyException("[VerDBProc::checkMCP]", "W606", "No KRW amount of " + baseCurCode, "No KRW amount of " + baseCurCode);
                }
                rs.close();
                pstmt.close();
            }

        } catch (MyException e) {
            throw e;
        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::checkMCP]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::checkMCP]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    public void prepareReqTranact(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //카드타입 고객선택
            pstmt = conn.prepareStatement("select name from EXIMBAY.codelist where code=?");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB08")).trim());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dataInfo.put("VD_CARDNAME", rs.getString("name"));
            } else {
                //카드사정보가 없는 경우
                throw new MyException("[VerDBProc::reqTransact]", "SS01", "undefined cardtype", "undefined cardtype");
            }
            rs.close();
            pstmt.close();

            //결제 요청할 정보 추출
            sbQuery = new StringBuffer();
            sbQuery.append("select b.vanno, b.vancode, b.urlip, ifnull(b.port, '0') as port, b.transtype, ");
            sbQuery.append("ifnull(e.vanshopid, '') as vanshopid, ");
            sbQuery.append("f.accttype, f.startday, f.period, f.extradays, ifnull(e.secureyn, '') as secureyn ");
            sbQuery.append("from EXIMBAY.service_currency a ");
            sbQuery.append("inner join EXIMBAY.service_sys f on a.merchantid=f.merchantid and a.cardcode=f.cardcode ");
            sbQuery.append("inner join EXIMBAY.van b on a.vanno=b.vanno ");
            sbQuery.append("inner join EXIMBAY.merchant_service c on a.merchantid=c.merchantid ");
            sbQuery.append("inner join EXIMBAY.ms_merchant d on c.merchantno=d.merchantno ");
            sbQuery.append("left outer join EXIMBAY.van_sys e on a.vanno=e.vanno and e.cardcode=? and e.currency=? ");
            sbQuery.append("where a.status='A000' and b.status='A000' and f.status='A000' and c.status='A000' ");
            sbQuery.append("and a.merchantid=? and c.merchantno=? and c.servicecode=? ");
            sbQuery.append("and a.cardcode=? and a.reqcurrency=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB08")).trim());
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("VD_APPRVCUR")).trim());
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB01")).trim());
            pstmt.setInt(4, AppUtil.checkNullInt(dataInfo.get("VD_MERCHANTNO")));
            pstmt.setString(5, "S001");
            pstmt.setString(6, AppUtil.checkNull(dataInfo.get("RB08")).trim());
            pstmt.setString(7, AppUtil.checkNull(dataInfo.get("RB03")).trim());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dataInfo.put("VD_VANNO", rs.getString("vanno"));
                dataInfo.put("VD_VANCODE", rs.getString("vancode"));
                dataInfo.put("VD_ACCTTYPE", rs.getString("accttype"));
                dataInfo.put("VD_STARTDAY", rs.getString("startday"));
                dataInfo.put("VD_PERIOD", rs.getString("period"));
                dataInfo.put("VD_EXTRADAYS", rs.getString("extradays"));
            } else {
                throw new MyException("[VerDBProc::prepareReqTranact]", "SS02", "invalid configuration", "invalid configuration");
            }
            rs.close();
            pstmt.close();
        } catch (MyException e) {
            throw e;
        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::prepareReqTranact]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::prepareReqTranact]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    public void reqTransact(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //데이터 암호화
            AbstractCipher cipher = CipherFactory.getCipher(AbstractApp.g_appCfg.getKmsCekLastKeyIndex());
            byte[] encPan = cipher.encryptData(AppUtil.checkNull(dataInfo.get("RB09")).getBytes());
            byte[] encCvv = cipher.encryptData(AppUtil.checkNull(dataInfo.get("RB11")).getBytes());

            String curDate = AppUtil.getDateTime().substring(0, 8);
            int maxNo = 0;
            try {
                conn.setAutoCommit(false);

                pstmt = conn.prepareStatement("lock tables EXIMBAY.max_id write");
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("select maxid from EXIMBAY.max_id where merchantid=? and curdate=? ");
                pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")).trim());
                pstmt.setString(2, curDate);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    maxNo = rs.getInt("maxid");
                }
                rs.close();
                pstmt.close();

                if (maxNo == 0) {
                    maxNo = 1;

                    pstmt = conn.prepareStatement("insert into EXIMBAY.max_id (merchantid, curdate, maxid, updatedt) values (?, ?, ?, now()) ");
                    pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")).trim());
                    pstmt.setString(2, curDate);
                    pstmt.setInt(3, maxNo);
                    pstmt.executeUpdate();
                    pstmt.close();

                } else {
                    maxNo++;

                    pstmt = conn.prepareStatement("update EXIMBAY.max_id set maxid=?, updatedt=now() where merchantid=? and curdate=? ");
                    pstmt.setInt(1, maxNo);
                    pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB01")).trim());
                    pstmt.setString(3, curDate);
                    pstmt.executeUpdate();
                    pstmt.close();
                }

                pstmt = conn.prepareStatement("unlock tables ");
                pstmt.executeUpdate();
                pstmt.close();

                conn.commit();
            } catch (Exception ie) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }


            String transid = AppUtil.checkNull(dataInfo.get("RB01")).trim() + curDate + AppUtil.formatData(maxNo, AppUtil.RIGHT, 6, '0');

            int merchantNo = AppUtil.checkNullInt(dataInfo.get("VD_MERCHANTNO"));
            String merchantId = AppUtil.checkNull(dataInfo.get("RB01")).trim();
            int vanNo = AppUtil.checkNullInt(dataInfo.get("VD_VANNO"));
            String vanShopId = "";
            String transType = AppUtil.checkNull(dataInfo.get("VD_TRANSTYPE"));
            double totalAmount = AppUtil.checkNullDouble(dataInfo.get("VD_AMT"));
            byte[] expiryDate = cipher.encryptData(AppUtil.checkNull(dataInfo.get("RB10")).trim().getBytes());
            String serviceType = AppUtil.checkNull(dataInfo.get("VD_TRADETYPE"));
            String currency = AppUtil.checkNull(dataInfo.get("RB03")).trim();
            int exgNo = AppUtil.checkNullInt(dataInfo.get("VD_EXGNO"));
            String cardCode = AppUtil.checkNull(dataInfo.get("RB08")).trim();
            String secureTransId = "";
            String apprvCurrency = AppUtil.checkNull(dataInfo.get("VD_APPRVCUR"));
            double apprvAmount = AppUtil.checkNullDouble(dataInfo.get("VD_AMT"));

            String cardNo = AppUtil.checkNull(dataInfo.get("RB09")).trim();
            String cardNo1 = null;
            String cardNo4 = null;
            if (cardNo.length() > 4) {
                cardNo1 = cardNo.substring(0, 4);
                cardNo4 = cardNo.substring(cardNo.length() - 4, cardNo.length());
            } else {
                cardNo1 = "";
                cardNo4 = "";
            }
            String vanCurrency = AppUtil.checkNull(dataInfo.get("RB03")).trim();
            double vanAmount = AppUtil.checkNullDouble(dataInfo.get("VD_AMT"));
            int vanExgNo = AppUtil.checkNullInt(dataInfo.get("VD_EXGNO"));
            String keyIndex = cipher.getKeyIndex();

            if ("".equals(cardCode)) {
                cardCode = null;
            }
            if (vanAmount == 0) {
                vanCurrency = apprvCurrency;
                vanAmount = apprvAmount;
                vanExgNo = exgNo;
            }


            sbQuery.append("insert into EXIMBAY.transact ( ");
            sbQuery.append(" transid, merchantno, merchantid, vanno, vanshopid, transtype, ");
            sbQuery.append(" totamount, expirydt, servicetype, currency, exgno, ");
            sbQuery.append(" cardcode, securetransid, apprvcurrency, apprvamount, encpan, ");
            sbQuery.append(" enccvv, cardno1, cardno4, vancurrency, vanamount, ");
            sbQuery.append(" vanexgno, reqdt, KEYINDEX ");
            sbQuery.append(") values ( ");
            sbQuery.append(" ?,?,?,?,?,?, ");
            sbQuery.append(" ?,?,?,?,?,   ");
            sbQuery.append(" ?,?,?,?,?,   ");
            sbQuery.append(" ?,?,?,?,?,   ");
            sbQuery.append(" ?,now(),?    ");
            sbQuery.append(")");

            try (PreparedStatement pstmt = conn.prepareStatement(sbQuery.toString())) {
                int idx = 0;
                pstmt.setString(++idx, transid);
                pstmt.setInt(++idx, merchantNo);
                pstmt.setString(++idx, merchantId);
                pstmt.setInt(++idx, vanNo);
                pstmt.setString(++idx, vanShopId);
                pstmt.setString(++idx, transType);

                pstmt.setDouble(++idx, totalAmount);
                pstmt.setBytes(++idx, expiryDate);
                pstmt.setString(++idx, serviceType);
                pstmt.setString(++idx, currency);
                pstmt.setInt(++idx, exgNo);

                pstmt.setString(++idx, cardCode);
                pstmt.setString(++idx, secureTransId);
                pstmt.setString(++idx, apprvCurrency);
                pstmt.setDouble(++idx, apprvAmount);
                pstmt.setBytes(++idx, encPan);

                pstmt.setBytes(++idx, encCvv);
                pstmt.setString(++idx, cardNo1);
                pstmt.setString(++idx, cardNo4);
                pstmt.setString(++idx, vanCurrency);
                pstmt.setDouble(++idx, vanAmount);

                pstmt.setInt(++idx, vanExgNo);
                pstmt.setString(++idx, keyIndex);

                pstmt.executeUpdate();

                try (PreparedStatement pstmt2 = conn.prepareStatement("select last_insert_id() as transno ")) {
                    try (ResultSet rs = pstmt2.executeQuery()) {
                        if (rs.next()) {
                            dataInfo.put("RB02", transid);
                            dataInfo.put("VD_EXIMBAYTRANSNO", rs.getInt("transno") + "");
                        }
                    }
                }
            } catch (Exception e) {
                throw e;
            }

            System.out.println("[VerDBProc::reqTransact]transno : " + AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));


        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::reqTransact]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::reqTransact]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    public void insertMCPTransact(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            DecimalFormat df = new DecimalFormat("########.##########");
            String convertRate = AppUtil.checkNull(dataInfo.get("RB24")).trim();
            int convertMinor = Integer.parseInt(convertRate.substring(0, 1));
            double convert = Double.parseDouble(convertRate.substring(1, convertRate.length())) * Math.pow(10, 0 - convertMinor);
            convert = Double.parseDouble(df.format(convert));

            sbQuery = new StringBuffer();
            sbQuery.append("insert into EXIMBAY.mcp_transact ( ");
            sbQuery.append("transno, status, basecurrency, baseamount, quotecurrency, ");
            sbQuery.append("quoteamount, quoteamountminor, convrate, rateid, krwamount, ");
            sbQuery.append("reqcurrency, reqamount, regdt ");
            sbQuery.append(") values ( ");
            sbQuery.append("?, ?, ?, ?, ?, ");
            sbQuery.append("?, ?, ?, ?, ?, ");
            sbQuery.append("?, ?, now()) ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB27")).trim());
            pstmt.setInt(3, AppUtil.checkNullInt(dataInfo.get("RB25")));
            pstmt.setDouble(4, AppUtil.checkNullDouble(dataInfo.get("VD_AMT")));
            pstmt.setInt(5, AppUtil.checkNullInt(dataInfo.get("RB21")));
            pstmt.setDouble(6, AppUtil.checkNullDouble(dataInfo.get("RB22")));
            pstmt.setInt(7, AppUtil.checkNullInt(dataInfo.get("RB23")));
            pstmt.setDouble(8, convert);
            pstmt.setString(9, AppUtil.checkNull(dataInfo.get("RB28")).trim());
            pstmt.setDouble(10, AppUtil.checkNullDouble(dataInfo.get("RB26")));
            pstmt.setString(11, AppUtil.checkNull(dataInfo.get("RB96")));
            pstmt.setDouble(12, AppUtil.checkNullDouble(dataInfo.get("RB97")));
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::insertMCPTransact]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::insertMCPTransact]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    public void resTransact(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            String resCode = AppUtil.checkNull(dataInfo.get("VD_RESCODE")).trim();
            String resMsg = AppUtil.checkNull(dataInfo.get("VD_RESMSG")).trim();
            String vanCode = AppUtil.checkNull(dataInfo.get("VD_VANCODE")).trim();
            if (!resCode.equals("0000")) {
                if (resCode.startsWith("S")) {
                    pstmt = conn.prepareStatement("select * from EXIMBAY.error_msg where msgcode=? ");
                    pstmt.setString(1, resCode);
                } else {
                    if (vanCode.equals("V007") || vanCode.equals("V009") || vanCode.equals("V010")) {
                        pstmt = conn.prepareStatement("select * from EXIMBAY.error_msg where vancode='V007' and msgcode=? ");
                        pstmt.setString(1, resCode);
					/*}else if(entity.getVanCode().equals("V010")){
						pstmt = con.prepareStatement("select * from error_msg where vancode='V010' and msgcode=? ");
						pstmt.setString(1, entity.getVanResCode());*/
                    } else {
                        pstmt = conn.prepareStatement("select * from EXIMBAY.error_msg where vancode=? and msgcode=? ");
                        pstmt.setString(1, vanCode);
                        pstmt.setString(2, resCode);
                    }
                }
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (vanCode.equals("V007") || vanCode.equals("V009") || vanCode.equals("V010")) {
                        if (!AppUtil.checkNull(rs.getString("kr_msg")).equals(""))
                            dataInfo.put("VD_RESMSG", resMsg + "(" + rs.getString("kr_msg") + ")");
                        if (!AppUtil.checkNull(rs.getString("en_msg")).equals(""))
                            dataInfo.put("VD_RESMSG", resMsg + "|" + rs.getString("en_msg"));
                        else dataInfo.put("VD_RESMSG", resMsg + "|Decline");
                    } else if (vanCode.equals("V008")) {
                        dataInfo.put("VD_RESMSG", "[" + resCode + "]" + rs.getString("en_msg") + AppUtil.checkNull(rs.getString("en_tel")) + "(" + resMsg + ")");
                    } else {
                        dataInfo.put("VD_RESMSG", "[" + resCode + "]" + rs.getString("en_msg") + AppUtil.checkNull(rs.getString("en_tel")));
                    }
                } else {
                    //한글버전이 아닌 경우, 무조건 영문으로, DB에 에러코드 정의가 되어있지 않은 경우, 무조건 Decline으로 처리
                    if (vanCode.equals("V008")) {
                        //Payvision의 경우, 영문메시지이므로, 모두 영문메시지로 표시
                        dataInfo.put("VD_RESMSG", resMsg + "|" + resMsg);
                    } else dataInfo.put("VD_RESMSG", resMsg + "|Decline");
                }
                rs.close();
                pstmt.close();
            }

            //응답정보 저장
            sbQuery = new StringBuffer();
            sbQuery.append("update EXIMBAY.transact set ");
            sbQuery.append("resdt = now(), ");
            sbQuery.append("vanunqid = ?, ");
            sbQuery.append("rescode = ?, ");
            sbQuery.append("resmsg = ?, ");
            sbQuery.append("authcode = ?, ");
            if (resCode.equals("0000"))
                sbQuery.append("status = 'S100', ");
            sbQuery.append("orgtransid=? ");
            sbQuery.append("where transno=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            if (!AppUtil.checkNull(dataInfo.get("VD_SEQNO")).trim().equals(""))
                pstmt.setString(1, AppUtil.checkNull(dataInfo.get("VD_SEQNO")));
            else pstmt.setString(1, "");
            pstmt.setString(2, resCode);
            pstmt.setString(3, resMsg);
            pstmt.setString(4, AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")).trim());
            pstmt.setString(5, AppUtil.checkNull(dataInfo.get("VD_ORGTRANSID")).trim());
            pstmt.setInt(6, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));
            pstmt.executeUpdate();
            pstmt.close();

            //거래응답 일시를 DB 일자로 통일 2011-12-12 6:35오후 by kong
            pstmt = conn.prepareStatement("select date_format(resdt, '%Y%m%d%H%i%s') as resdt from EXIMBAY.transact where transno=? ");
            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dataInfo.put("VD_TRADEDT", rs.getString("resdt"));
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::resTransact]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::resTransact]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    public void insertSettlement(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        double serviceFee = 0;
        double cardFee = 0;
        double agencyFee = 0;
        double partnerFee = 0;
        double gstRate = 0;
        String partnerFeeType = "";
        double partnerBaseFee = 0;
        int acctDays = 0;
        String nowDT = "";

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            //수수료율
            sbQuery = new StringBuffer();
            sbQuery.append("select ifnull(b.feerate, 0) as agencyfee, ");
            sbQuery.append("ifnull(e.feerate, 0) as cardfee, ifnull(d.feerate, 0) as servicefee, ");
            sbQuery.append("ifnull(d.partnerfee, 0) as partnerfee, ifnull(a.gstrate, 0) as gstrate, a.gstyn, ");
            sbQuery.append("ifnull(d.partnerfeetype, 'P000') as partnerfeetype, ");
            sbQuery.append("ifnull(d.partnerbasefee, 0) as partnerbasefee, d.acctdays, date_format(now(), '%Y%m%d') as nowdt ");
            sbQuery.append("from EXIMBAY.ms_merchant a  ");
            sbQuery.append("inner join EXIMBAY.ms_agency b on a.agencyno=b.agencyno ");
            sbQuery.append("inner join EXIMBAY.merchant_service c on a.merchantno=c.merchantno  ");
            sbQuery.append("inner join EXIMBAY.service_sys d on c.merchantid=d.merchantid  ");
            sbQuery.append("inner join EXIMBAY.service_currency f on c.merchantid=f.merchantid and d.cardcode=f.cardcode ");
            sbQuery.append("inner join EXIMBAY.van_sys e on f.vanno=e.vanno and d.cardcode=e.cardcode and f.apprvcurrency=e.currency ");
            sbQuery.append("where a.merchantno=? and c.servicecode=? and d.cardcode=? and e.currency=? ");
            sbQuery.append("and c.status='A000' and d.status='A000' and f.status='A000' ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_MERCHANTNO")));
            pstmt.setString(2, "S001");
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB08")));
            pstmt.setString(4, AppUtil.checkNull(dataInfo.get("RB03")));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                agencyFee = rs.getDouble("agencyfee");
                cardFee = rs.getDouble("cardfee");
                serviceFee = rs.getDouble("servicefee");
                partnerFee = rs.getDouble("partnerfee");
                partnerFeeType = rs.getString("partnerfeetype");
                partnerBaseFee = rs.getDouble("partnerbasefee");
                if (AppUtil.checkNull(rs.getString("gstyn")).equals("Y")) gstRate = rs.getDouble("gstrate");
                acctDays = rs.getInt("acctdays");

                //응답시간 기준으로 지급일자 계산
                nowDT = rs.getString("nowdt");
            }
            rs.close();
            pstmt.close();

            System.out.println("agencyFee : " + agencyFee);
            System.out.println("cardFee : " + cardFee);
            System.out.println("serviceFee : " + serviceFee);
            System.out.println("acctDays : " + acctDays);
            System.out.println("nowDT : " + nowDT);

            //수수료 금액 추출(2007-05-06 2:41오후 Accounts_detail정산 시 적용)
            double totalAmount = AppUtil.checkNullDouble(dataInfo.get("VD_AMT"));
            double serviceAmount = (double) Math.round(totalAmount * serviceFee) / (double) 100;
            double cardAmount = (double) Math.round(totalAmount * cardFee) / (double) 100;
            double mainAmount = serviceAmount - cardAmount;
            double agencyAmount = (double) Math.round(mainAmount * agencyFee) / (double) 100;
            double gstAmount = (double) Math.round(serviceAmount * gstRate) / (double) 100;
            double partnerBaseAmount = 0;
            double partnerAmount = 0;

            if (partnerFeeType.equals("P000")) {
                partnerAmount = (double) Math.round(totalAmount * partnerFee) / (double) 100;
            } else if (partnerFeeType.equals("P001")) {
                partnerBaseAmount = (double) Math.round(totalAmount * partnerBaseFee) / (double) 100;
                partnerAmount = (double) Math.round((serviceAmount - partnerBaseAmount) * partnerFee) / (double) 100;
            }

            //입금예정일 추출
            String acctdt = "";
            String remitdt = "";

            int addDays = 0;
            int addPeriod = 0;
            //Stirng regDT = AppUtil.getDateTimeFormat(entity.getVanResDT());
            String regDT = nowDT.substring(0, 8);

            String acctType = AppUtil.checkNull(dataInfo.get("VD_ACCTTYPE"));
            int extraDays = AppUtil.checkNullInt(dataInfo.get("VD_EXTRADAYS"));
            int startDay = AppUtil.checkNullInt(dataInfo.get("VD_STARTDAY"));
            int period = AppUtil.checkNullInt(dataInfo.get("VD_PERIOD"));

            System.out.println("정산구분 : " + acctType);
            if (acctType.equals("A400")) {
                //일정산
                //addDays = entity.getPeriod() + entity.getExtraDays();
                remitdt = AppUtil.getDateadd(regDT, extraDays + 1);
                acctdt = AppUtil.getDateadd(remitdt, acctDays);
                System.out.println("일정산 : " + AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")) + ":" + regDT + ", " + acctdt + ", " + remitdt);
            } else if (acctType.equals("A401")) {
                //주정산

                int todayweek = AppUtil.getDayOfWeek(regDT);
                int startweek = startDay;
                String startdt = "";
                if (todayweek > startweek)
                    startdt = AppUtil.getDateadd(regDT, (startweek - todayweek));
                else if (todayweek == startweek)
                    startdt = regDT;
                else if (todayweek < startweek)
                    startdt = AppUtil.getDateadd(regDT, (startweek - todayweek - 7));

                String enddt = AppUtil.getDateadd(startdt, (7 * period) - 1);

                remitdt = AppUtil.getDateadd(enddt, extraDays + 1);
                acctdt = AppUtil.getDateadd(remitdt, acctDays);
                System.out.println("주정산 : " + AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")) + ":" + regDT + ":" + startdt + "~" + enddt + ", " + acctdt + ", " + remitdt);

            } else if (acctType.equals("A402")) {
                //월정산
                int today = Integer.parseInt(regDT.substring(6, 8));
                int days = AppUtil.getDayOfMonth(Integer.parseInt(regDT.substring(0, 4)), Integer.parseInt(regDT.substring(4, 6)));
                String startdt = AppUtil.getDateadd(regDT, 1 - today);
                String enddt = AppUtil.getDateadd(regDT, days - today);

                remitdt = AppUtil.getDateadd(enddt, extraDays + 1);
                acctdt = AppUtil.getDateadd(remitdt, acctDays);

                System.out.println("월정산 : " + AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")) + ":" + regDT + ":" + startdt + "~" + enddt + ", " + acctdt + ", " + remitdt);

            }

            //승인시점의 정산통화 및 정산환율 추출
            String billedCurrency = AppUtil.checkNull(dataInfo.get("RB03"));
            int billedExgNo = 0;
            pstmt = conn.prepareStatement("select * from EXIMBAY.service_currency where merchantId=? and reqcurrency=? and cardcode=? ");
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB03")));
            pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB08")));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                billedCurrency = rs.getString("billedcurrency");
            }
            rs.close();
            pstmt.close();

            sbQuery = new StringBuffer();
            sbQuery.append("select exgno, round(?*toamount/framount, 2) as apprvamount from EXIMBAY.exchange_rate ");
            sbQuery.append("where frcurrency=? and tocurrency=? ");
            sbQuery.append("order by regdt desc ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setDouble(1, AppUtil.checkNullDouble(dataInfo.get("VD_AMT")));
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB03")));
            pstmt.setString(3, billedCurrency);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                billedExgNo = rs.getInt("exgno");
            }
            rs.close();
            pstmt.close();

            System.out.println("[PaymentDBWrap::resTransaction]Billed ExgNo : " + billedCurrency + ", " + billedExgNo);

            //매입데이터
            int idx = 1;
            sbQuery = new StringBuffer();
            sbQuery.append("insert into EXIMBAY.settlement ( ");
            sbQuery.append("transno, merchantno, totalamount, servicefee, cardfee, ");
            sbQuery.append("agencyfee, apprvexgno, billedexgno, remitdt, acctdt, ");
            sbQuery.append("partnerfee, accttype, startday, period, extradays, ");
            sbQuery.append("serviceamount, cardamount, agencyamount, partneramount, billedcurrency, ");
            sbQuery.append("apprvcurrency, reqcurrency, gstrate, gstamount, partnerfeetype, ");
            sbQuery.append("partnerbasefee, regdt, moddt ");
            sbQuery.append(") values ( ");
            sbQuery.append("?, ?, ?, ?, ?, ");
            sbQuery.append("?, ?, ?, ?, ?, ");
            sbQuery.append("?, ?, ?, ?, ?, ");
            sbQuery.append("?, ?, ?, ?, ?, ");
            sbQuery.append("?, ?, ?, ?, ?, ");
            sbQuery.append("?, now(), now() )");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(idx++, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));
            pstmt.setInt(idx++, AppUtil.checkNullInt(dataInfo.get("VD_MERCHANTNO")));
            pstmt.setDouble(idx++, totalAmount);
            pstmt.setDouble(idx++, serviceFee);
            pstmt.setDouble(idx++, cardFee);

            pstmt.setDouble(idx++, agencyFee);
            pstmt.setInt(idx++, AppUtil.checkNullInt(dataInfo.get("VD_EXGNO")));
            pstmt.setInt(idx++, billedExgNo);
            pstmt.setString(idx++, remitdt);
            pstmt.setString(idx++, acctdt);

            pstmt.setDouble(idx++, partnerFee);
            pstmt.setString(idx++, acctType);

            pstmt.setInt(idx++, startDay);
            pstmt.setInt(idx++, period);
            pstmt.setInt(idx++, extraDays);
            pstmt.setDouble(idx++, serviceAmount);
            pstmt.setDouble(idx++, cardAmount);
            pstmt.setDouble(idx++, agencyAmount);
            pstmt.setDouble(idx++, partnerAmount);
            pstmt.setString(idx++, billedCurrency);
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB03")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB03")));
            pstmt.setDouble(idx++, gstRate);
            pstmt.setDouble(idx++, gstAmount);
            pstmt.setString(idx++, partnerFeeType);
            pstmt.setDouble(idx++, partnerBaseFee);
            pstmt.executeUpdate();
            pstmt.close();

            //PCINS에 전송할 MDR, 수수료금액, 정산예정일자
            DecimalFormat df = new DecimalFormat("###.##");
            String serviceFeeStr = df.format(serviceFee);
            String serviceFeeMaj = "";
            String serviceFeeMin = "";
            if (serviceFeeStr.indexOf(".") != -1) {
                serviceFeeMaj = serviceFeeStr.substring(0, serviceFeeStr.indexOf("."));
                serviceFeeMin = serviceFeeStr.substring(serviceFeeStr.indexOf(".") + 1, serviceFeeStr.length());
            } else serviceFeeMaj = serviceFeeStr;

            serviceFeeStr = AppUtil.formatData(serviceFeeMaj, AppUtil.RIGHT, 3, '0')
                    + AppUtil.formatData(serviceFeeMin, AppUtil.LEFT, 2, '0');

            dataInfo.put("VD_SERVICEFEE", serviceFeeStr);

            //수수료는 요청통화 기준(임시) --> 추후 JPY base 시점에 Base 통화로 변경
            double reqAmount = AppUtil.checkNullDouble(dataInfo.get("RB97"));
            double reqServiceAmount = (double) Math.round(reqAmount * serviceFee) / (double) 100;
            df = new DecimalFormat("############");   //소수이하 반올림
            dataInfo.put("VD_SERVICEFEEAMT", AppUtil.formatData(df.format(reqServiceAmount), AppUtil.RIGHT, 12, '0'));

            dataInfo.put("VD_REMITDT", remitdt);

        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::insertSettlement]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::insertSettlement]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    public void resTransactWeb(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            System.out.println("[VerDBProc::resTransactWeb]transno : " + AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));
            int idx = 1;
            sbQuery.append("update EXIMBAY.transact_web set ");
            sbQuery.append("rescode = ?, ");
            sbQuery.append("resmsg = ?, ");
            sbQuery.append("authcode = ?, ");
            sbQuery.append("cardco = ?, ");
            if (AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")) != 0) {
                sbQuery.append("transno = ?, ");
            }
            sbQuery.append("holdername = ?, ");
            sbQuery.append("payto = ?, ");
            sbQuery.append("resdt = now() ");
            sbQuery.append("where webno = ? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_RESCODE")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_RESMSG")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_AUTHCODE")));
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_CARDNAME")));
            if (AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")) != 0) {
                pstmt.setInt(idx++, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));
            }
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("RB12")).trim());
            pstmt.setString(idx++, AppUtil.checkNull(dataInfo.get("VD_PAYTO")));
            pstmt.setInt(idx++, AppUtil.checkNullInt(dataInfo.get("VD_WEBNO")));
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::resTransactWeb]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::resTransactWeb]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    //취소거래 데이터 유효성, 원거래 확인
    public void checkVoidRequest(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("select b.transid, a.authcode, a.transno, a.cur, a.amt, a.ref, b.cardcode, d.merchantno, c.status as sstatus, ");
            sbQuery.append("d.status as mstatus, ifnull(c.secretkey, '') as secretkey, d.type, b.transtype, date_format(b.resdt, '%Y%m%d') as orgtxndt ");
            sbQuery.append("from EXIMBAY.transact_web a ");
            sbQuery.append("inner join EXIMBAY.transact b on a.transno=b.transno ");
            sbQuery.append("inner join EXIMBAY.merchant_service c on a.merchantid=c.merchantid ");
            sbQuery.append("inner join EXIMBAY.ms_merchant d on c.merchantno=d.merchantno ");
            sbQuery.append("where a.merchantid=? and a.ref=? and a.rescode='0000' and b.transtype in ('T000', 'T002', 'T003', 'T004', 'T005', 'T006', 'T007') ");
            if (!AppUtil.checkNull(dataInfo.get("RB02")).trim().equals("")) sbQuery.append("and b.transid=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")).trim());
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB90")).trim());
            if (!AppUtil.checkNull(dataInfo.get("RB02")).trim().equals(""))
                pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB02")).trim());
            rs = pstmt.executeQuery();
            if (rs.next()) {

                //가맹점 및 서비스 상태 확인
                if (!(rs.getString("sstatus").equals("A000") || rs.getString("sstatus").equals("A001")))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W103", "invalid service", "invalid service");
                else if (!(rs.getString("mstatus").equals("A000") || rs.getString("mstatus").equals("A001")))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W104", "invalid merchant", "invalid merchant");
                else if (rs.getInt("merchantno") == 0)
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W105", "invalid merchant", "invalid merchant");

                //프리미엄 가맹점 확인
                if (!rs.getString("type").equals("A201"))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W107", "invalid merchant", "invalid merchant");
                else if (rs.getString("cardcode").equals("C015"))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W400", "Can not be proceeded the void of Paypal transaction.", "Can not be proceeded the void of Paypal transaction.");
                else if (!rs.getString("cur").equals(AppUtil.checkNull(dataInfo.get("RB03")).trim()))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W401", "Not matched currency", "Not matched currency");
                else if (rs.getDouble("amt") != AppUtil.checkNullDouble(dataInfo.get("VD_AMT")))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W402", "Not matched amount", "Not matched amount");
                else if (rs.getString("transtype").equals("T002") || rs.getString("transtype").equals("T005") || rs.getString("transtype").equals("T006"))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W403", "Already voided", "Already voided");
                else if (rs.getString("transtype").equals("T007"))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W700", "Already captured", "Already captured");
                else if (!rs.getString("authcode").equals(AppUtil.checkNull(dataInfo.get("RB51")).trim()))
                    throw new MyException("[VerDBProc::checkVoidRequest]", "W404", "Not matched authcode", "Not matched authcode");
                else {
                    dataInfo.put("VD_MERCHANTNO", rs.getString("merchantno"));
                    dataInfo.put("VD_EXIMBAYORGTRANSNO", rs.getString("transno"));
                    dataInfo.put("VD_EXIMBAYORGTXNDT", rs.getString("orgtxndt"));
                }
            } else {
                throw new MyException("[VerDBProc::checkVoidRequest]", "W405", "No matched transaction", "No matched transaction");
            }
            rs.close();
            pstmt.close();

        } catch (MyException e) {
            throw e;
        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::checkVoidRequest]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::checkVoidRequest]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    //취소거래 저장
    public void insertVoidTransact(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());
            conn.setAutoCommit(false);

            System.out.println("[VerDBProc::insertVoidTransact]orgtransno : " + AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
            //취소 처리 중 또는 완료 확인
            String status = "";
            String voidStatus = "";
            int settleno = 0;
            int voidNo = 0;
            String transType = "";
            String voidType = "V200";    //Reverse (정산확정 여부 확인)
            sbQuery.append("select ifnull(a.settleno, 0) as settleno, b.status, b.transtype, ifnull(c.status, '') as voidstatus , c.voidno ");
            sbQuery.append("from EXIMBAY.transact b ");
            sbQuery.append("left outer join EXIMBAY.settlement a on a.transno = b.transno ");
            sbQuery.append("left outer join EXIMBAY.transact_void c on b.transno=c.orgtransno ");
            sbQuery.append("where b.transno=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("voidstatus").equals("C100") || rs.getString("voidstatus").equals("C103")) {
                    //취소요청, 지급보류(취소요청 후 정산확정)
                    throw new MyException("[VerDBProc::insertVoidTransact]", "V200", "This transaction is already processing for void.", "This transaction is already processing for void.");
                } else if (rs.getString("voidstatus").equals("C101") || rs.getString("voidstatus").equals("C106")) {
                    //취소완료, 취소완료대기(취소확정 후 정산확정 대기)
                    throw new MyException("[VerDBProc::insertVoidTransact]", "V201", "This transaction was already reversed or refunded.", "This transaction was already reversed or refunded.");
                }
                settleno = rs.getInt("settleno");
                status = rs.getString("status");
                voidStatus = rs.getString("voidstatus");
                voidNo = rs.getInt("voidno");
                transType = rs.getString("transtype");

                if (!rs.getString("status").equals("S100")) voidType = "V201";    //Refund
            }
            rs.close();
            pstmt.close();
            System.out.println("[VerDBProc::insertVoidTransact]voidno : " + voidNo);
            if (voidNo == 0) {
                //transact_void 등록
                sbQuery = new StringBuffer();
                sbQuery.append("insert into EXIMBAY.transact_void ( ");
                sbQuery.append("orgtransno, voidtype, regdt, moddt, _reqdt ");
                sbQuery.append(") values ( ");
                sbQuery.append("?, ?, now(), now(), now() ) ");
                pstmt = conn.prepareStatement(sbQuery.toString());
                pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
                pstmt.setString(2, voidType);
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("select last_insert_id() as voidno ");
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    voidNo = rs.getInt("voidno");
                }
                rs.close();
                pstmt.close();
            } else {
                //취소요청 상태로 update
                sbQuery = new StringBuffer();
                sbQuery.append("update EXIMBAY.transact_void set ");
                sbQuery.append("status=?, ");
                sbQuery.append("voidtype=?, ");
                sbQuery.append("moddt=now() ");
                sbQuery.append("where orgtransno=? ");
                pstmt = conn.prepareStatement(sbQuery.toString());
                pstmt.setString(1, "C100");
                pstmt.setString(2, voidType);
                pstmt.setInt(7, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
                pstmt.executeUpdate();
                pstmt.close();
            }
            System.out.println("[VerDBProc::insertVoidTransact]voidno : " + voidNo);
            //transact_void_history 등록
            sbQuery = new StringBuffer();
            sbQuery.append("insert into EXIMBAY.transact_void_history ( ");
            sbQuery.append("voidno, status, voidtype, regdt ");
            sbQuery.append(") values ( ");
            sbQuery.append("?, 'C100', ?, now() ) ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, voidNo);
            pstmt.setString(2, voidType);
            pstmt.executeUpdate();
            pstmt.close();

            if (voidType.equals("V200")) {        //Reverse
                //transact 상태 변경
                pstmt = conn.prepareStatement("update EXIMBAY.transact set status='S101', moddt=now() where transno=? ");
                pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
                pstmt.executeUpdate();
                pstmt.close();

                //settlement 상태 변경(정산 전에만 update, 정산 후에는 조정으로 처리)
                pstmt = conn.prepareStatement("update EXIMBAY.settlement set status='S101', moddt=now() where settleno=? ");
                pstmt.setInt(1, settleno);
                pstmt.executeUpdate();
                pstmt.close();
            } else {                                //Refund
                //transact 상태 변경
                pstmt = conn.prepareStatement("update EXIMBAY.transact set status='S140', moddt=now() where transno=? ");
                pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
                pstmt.executeUpdate();
                pstmt.close();
            }

            String newVoidStatus = "";
            pstmt = conn.prepareStatement("select * from EXIMBAY.transact_void where orgtransno=? ");
            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                voidStatus = rs.getString("status");
                voidType = rs.getString("voidtype");
            }
            rs.close();
            pstmt.close();


            //transact_void update
            if (voidStatus.equals("C100")) {
                if (voidType.equals("V200")) {
                    newVoidStatus = "C101";
                    pstmt = conn.prepareStatement("update EXIMBAY.transact_void set status='C101', voiddt=now(), voidid='system', moddt=now() where orgtransno=? ");
                } else {
                    newVoidStatus = "C106";
                    pstmt = conn.prepareStatement("update EXIMBAY.transact_void set status='C106', voiddt=now(), voidid='system', moddt=now() where orgtransno=? ");
                }
            } else if (voidStatus.equals("C103")) {
                newVoidStatus = "C101";
                pstmt = conn.prepareStatement("update EXIMBAY.transact_void set status='C101', voiddt=now(), voidid='system', moddt=now() where orgtransno=? ");
            } else
                throw new MyException("[VerDBProc::insertVoidTransact]", "V301", "This transaction is not valid.", "This transaction is not valid.");

            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
            pstmt.executeUpdate();
            pstmt.close();


            //transact_void_history insert
            sbQuery = new StringBuffer();
            sbQuery.append("insert into EXIMBAY.transact_void_history ( ");
            sbQuery.append("voidno, status, voidtype, regdt ");
            sbQuery.append(") values ( ");
            sbQuery.append("?, ?, ?, now() ) ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, voidNo);
            pstmt.setString(2, newVoidStatus);
            pstmt.setString(3, voidType);
            pstmt.executeUpdate();
            pstmt.close();

            String newTransType = "T002";
            if (transType.equals("T003")) newTransType = "T006";
            else if (transType.equals("T004")) newTransType = "T005";
            System.out.println("transtype : " + transType + "==> newtranstype : " + newTransType);
            if (voidType.equals("V200")) {
                //transact 상태 변경
                pstmt = conn.prepareStatement("update EXIMBAY.transact set status='S102', transtype=?, moddt=now() where transno=? ");
                pstmt.setString(1, newTransType);
                pstmt.setInt(2, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
                pstmt.executeUpdate();
                pstmt.close();

                //settlement 상태 변경(V200(정산확정 전), 정산확정 후는 조정으로 처리)
                pstmt = conn.prepareStatement("update EXIMBAY.settlement set status='S102', moddt=now() where transno=? ");
                pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
                pstmt.executeUpdate();
                pstmt.close();
            } else {                                //Refund
                //transact 상태 변경
                pstmt = conn.prepareStatement("update EXIMBAY.transact set status='S141', transtype=?, moddt=now() where transno=? ");
                pstmt.setString(1, newTransType);
                pstmt.setInt(2, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
                pstmt.executeUpdate();
                pstmt.close();
            }
			
			/*//한도복구
			int merchantNo = 0;
			double totAmount = 0;
			String currency = "";
			pstmt = conn.prepareStatement("select * from EXIMBAY.transact where transno=? ");
			pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
			rs = pstmt.executeQuery();
			if(rs.next()){
				merchantNo = rs.getInt("merchantno");
				totAmount = rs.getDouble("totamount");
				currency = rs.getString("currency");
			}
			rs.close();
			pstmt.close();
			
			sbQuery = new StringBuffer();
			sbQuery.append("update EXIMBAY.limit_monthly set ");
			sbQuery.append("balance = balance + ? ");
			sbQuery.append("where merchantno=? and month=? and currency=? ");
			pstmt = conn.prepareStatement(sbQuery.toString());
			pstmt.setDouble(1, totAmount);
			pstmt.setInt(2, merchantNo);
			pstmt.setString(3, AppUtil.getDateTime().substring(0, 6));
			pstmt.setString(4, currency);
			pstmt.executeUpdate();
			pstmt.close();*/


            //거래응답 일시를 DB 일자로 통일 2011-12-12 6:35오후 by kong
            pstmt = conn.prepareStatement("select date_format(voiddt, '%Y%m%d%H%i%s') as resdt from EXIMBAY.transact_void where orgtransno=? ");
            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYORGTRANSNO")));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                dataInfo.put("VD_TRADEDT", rs.getString("resdt"));
            }
            rs.close();
            pstmt.close();

            if (AppUtil.checkNull(dataInfo.get("VD_TRADEDT")).equals(""))
                dataInfo.put("VD_TRADEDT", AppUtil.getDateTime());

            conn.commit();
        } catch (MyException e) {
            try {
                conn.rollback();
            } catch (Exception ie) {
            }
            throw e;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception ie) {
            }
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::insertVoidTransact]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ie) {
            }
            throw new MyException("[VerDBProc::insertVoidTransact]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    if (conn != null) conn.setAutoCommit(true);
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }


    //Capture거래 데이터 유효성, 원거래 확인
    public void checkCaptureRequest(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("select b.transid, a.authcode, a.transno, a.cur, a.amt, a.ref, b.cardcode, d.merchantno, c.status as sstatus, ");
            sbQuery.append("d.status as mstatus, d.type, b.transtype, date_format(b.resdt, '%Y%m%d') as orgtxndt, ");
            sbQuery.append("b.encpan, b.expirydt, a.holdername, b.KEYINDEX ");
            sbQuery.append("from EXIMBAY.transact_web a ");
            sbQuery.append("inner join EXIMBAY.transact b on a.transno=b.transno ");
            sbQuery.append("inner join EXIMBAY.merchant_service c on a.merchantid=c.merchantid ");
            sbQuery.append("inner join EXIMBAY.ms_merchant d on c.merchantno=d.merchantno ");
            sbQuery.append("where a.merchantid=? and a.ref=? and a.rescode='0000' ");
            if (!AppUtil.checkNull(dataInfo.get("RB02")).trim().equals("")) sbQuery.append("and b.transid=? ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, AppUtil.checkNull(dataInfo.get("RB01")).trim());
            pstmt.setString(2, AppUtil.checkNull(dataInfo.get("RB90")).trim());
            if (!AppUtil.checkNull(dataInfo.get("RB02")).trim().equals(""))
                pstmt.setString(3, AppUtil.checkNull(dataInfo.get("RB02")).trim());
            rs = pstmt.executeQuery();
            if (rs.next()) {

                //가맹점 및 서비스 상태 확인
                if (!rs.getString("sstatus").equals("A000"))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W103", "invalid service", "invalid service");
                else if (!rs.getString("mstatus").equals("A000"))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W104", "invalid merchant", "invalid merchant");
                else if (rs.getInt("merchantno") == 0)
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W105", "invalid merchant", "invalid merchant");

                //프리미엄 가맹점 확인
                if (!rs.getString("type").equals("A201"))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W107", "invalid merchant", "invalid merchant");
                else if (rs.getString("cardcode").equals("C015"))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W400", "Can not be proceeded the void of Paypal transaction.", "Can not be proceeded the void of Paypal transaction.");
                else if (!rs.getString("cur").equals(AppUtil.checkNull(dataInfo.get("RB03")).trim()))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W401", "Not matched currency", "Not matched currency");
                else if (rs.getDouble("amt") != AppUtil.checkNullDouble(dataInfo.get("VD_AMT")))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W402", "Not matched amount", "Not matched amount");
                else if (rs.getString("transtype").equals("T006"))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W403", "Already voided", "Already voided");
                else if (rs.getString("transtype").equals("T007"))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W700", "Already captured", "Already captured");
                else if (!rs.getString("transtype").equals("T003"))
                    throw new MyException("[VerDBProc::checkCaptureRequest]", "W701", "Can not be captured", "Can not be captured");
                else {
                    dataInfo.put("VD_MERCHANTNO", rs.getString("merchantno"));
                    dataInfo.put("VD_EXIMBAYTRANSNO", rs.getString("transno"));
                    //dataInfo.put("VD_EXIMBAYTRANSID", rs.getString("transid"));

                    dataInfo.put("RB08", rs.getString("cardcode"));     //카드구분

                    //AppCipher app = new AppCipher();
                    String keyindex = rs.getString("KEYINDEX");
                    AbstractCipher abstractCipher = CipherFactory.getCipher(keyindex);

                    String expirydt = "";

                    if ("00".equals(keyindex)) {
                        expirydt = AppUtil.checkNull(rs.getString("expirydt"));
                    } else {
                        byte[] bytes = abstractCipher.decryptData(rs.getBytes("expirydt"));
                        expirydt = bytes != null ? new String(bytes) : "";
                    }

                    dataInfo.put("RB09", new String(abstractCipher.decryptData(rs.getBytes("encpan"))));     //카드번호
                    dataInfo.put("RB10", expirydt);     //유효기간
                    dataInfo.put("RB11", "");     //CVC/4DBC
                    dataInfo.put("RB12", rs.getString("holdername"));     //Holder                    
                }
            } else {
                throw new MyException("[VerDBProc::checkCaptureRequest]", "W405", "No matched transaction", "No matched transaction");
            }
            rs.close();
            pstmt.close();

        } catch (MyException e) {
            throw e;
        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::checkCaptureRequest]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::checkCaptureRequest]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    //Authorization거래 상태를 T007로 변경
    public void updateAuthToCapture(Map<String, String> dataInfo) throws MyException {
        sbQuery = new StringBuffer();

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            pstmt = conn.prepareStatement("update EXIMBAY.transact set transtype='T004' where transno=? ");
            pstmt.setInt(1, AppUtil.checkNullInt(dataInfo.get("VD_EXIMBAYTRANSNO")));
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("[VerDBProc::updateAuthToCapture]", MyException.SY03, "SQLException:" + e, MyException.sysErrMsg);
        } catch (Exception e) {
            throw new MyException("[VerDBProc::updateAuthToCapture]", MyException.SY03, "Exception:" + e, MyException.sysErrMsg);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }
    }

    /*********************************************
     * 매출(미 정산금액) 이 없을 경우 취소거래 불가 처리
     * */
    public void checkTotalAmount(Map<String, String> dataInfo) throws MyException {

        StringBuffer sbQuery = new StringBuffer();

        HashMap returnMap = new HashMap();
        double totalAmount = 0.0; //최종 미정산금액
        String calCur = "";

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            ArrayList<HashMap> arrlist = new ArrayList<HashMap>();

            sbQuery.append("select t.apprvcurrency, sum(t.apprvamount) as apprvamount from EXIMBAY.settlement s ");
            sbQuery.append("inner join EXIMBAY.transact t on s.TRANSNO = t.TRANSNO ");
            sbQuery.append("where s.status='S100' and s.acctlno is null and s.merchantno=? ");
            sbQuery.append("group by t.apprvcurrency ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setInt(1, Integer.parseInt(dataInfo.get("VD_MERCHANTNO")));
            rs = pstmt.executeQuery();

            while (rs.next()) {
                HashMap appCurMap = new HashMap();

                appCurMap.put("apprvcurrency", rs.getString("apprvcurrency"));
                appCurMap.put("apprvamount", rs.getDouble("apprvamount"));

                arrlist.add(appCurMap);
            }

            //미정산 통화가 원화인 경우를 제외 하고 원화로 변경한다.
            Iterator iter = arrlist.iterator();

            while (iter.hasNext()) {
                double krwRateAmt = 0.0; //환율
                double curAmt = 0.0;  //미정산금

                HashMap map = (HashMap) iter.next();

                krwRateAmt = getKRWAmount(map.get("apprvcurrency").toString());
                curAmt = Double.valueOf(map.get("apprvamount").toString());

                if (map.get("apprvcurrency").toString().equals("JPY")) {
                    totalAmount += (curAmt * krwRateAmt) / 100;
                } else if (!map.get("apprvcurrency").toString().equals("KRW")) {
                    totalAmount += curAmt * krwRateAmt;
                } else {
                    totalAmount += curAmt;
                }
                System.out.println("krwRateAmt_getTotal ::> " + (String) map.get("apprvcurrency") + " : " + map.get("apprvamount") + " curAmt ::> " + curAmt + " :::: " + krwRateAmt + " :: " + totalAmount);
            }
            rs.close();
            pstmt.close();

            calCur = "KRW";//log

            // 해당 취소 거래 금액을 원화로 환산 한다.
            double amt = AppUtil.checkNullDouble(dataInfo.get("VD_AMT"));
            String cur = AppUtil.checkNull(dataInfo.get("RB03"));

            if (!cur.equals("KRW")) {
                double krwRateAmt = 0.0; //환율
                double curAmt = 0.0;  //미정산금
                krwRateAmt = getKRWAmount(cur);
                curAmt = amt;

                if (cur.equals("JPY")) {
                    amt = (curAmt * krwRateAmt) / 100;
                } else if (!cur.equals("KRW")) {
                    amt = curAmt * krwRateAmt;
                } else {
                    amt = curAmt;
                }
            }

            System.out.println("취소금액 : " + amt + "원  미정산금액 : " + totalAmount + "원");
            // 취소 거래 금액과 미 정산 금액을 비교한다.
            if (amt > totalAmount)
                throw new MyException("[VerDBProc::checkTotalAmount]", "W406", "No settlement amount.", "No settlement amount.");

        } catch (SQLException e) {
            System.out.println("[VerDBProc::checkTotalAmount]SQLException : " + e);
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("SY02", e.toString(), "[TransactVoidDBWrap::checkTotalAmount]", "Your request is denied. Please contact us!!");
        } catch (Exception e) {
            System.out.println("[VerDBProc::checkTotalAmount]Exception : " + e);
            throw new MyException("SY01", e.toString(), "[TransactVoidDBWrap::checkTotalAmount]", "Your request is denied. Please contact us!!");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }

    }

    public double getKRWAmount(String currency) throws MyException {
        StringBuffer sbQuery = new StringBuffer();
        double KRWAmount = 0.0;

        try {
            conn = dbcm.getConnection(AbstractApp.g_appCfg.getDBName());

            sbQuery.append("select basicrate from EXIMBAY.keb_exgrt_list ");
            sbQuery.append("where currency=? ");
            sbQuery.append("order by exggno desc limit 1 ");
            pstmt = conn.prepareStatement(sbQuery.toString());
            pstmt.setString(1, currency);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                KRWAmount = rs.getDouble("basicrate");
            }
            //System.out.println("KRWAmount ::> "+KRWAmount);
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            if (dbcm != null) dbcm.closeConnection(AbstractApp.g_appCfg.getDBName(), conn);
            throw new MyException("SY02", e.toString(), "[VerDBProc::getKRWAmount]", "Your request is denied. Please contact us!!");
        } catch (Exception e) {
            throw new MyException("SY01", e.toString(), "[VerDBProc::getKRWAmount]", "Your request is denied. Please contact us!!");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dbcm != null) {
                    dbcm.freeConnection(AbstractApp.g_appCfg.getDBName(), conn);
                }
            } catch (Exception ie) {
            }
        }

        return KRWAmount;
    }
}