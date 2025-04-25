
package api.server.gateway.legacy.van.kicc;

import api.server.common.helper.AppUtil;
import api.server.gateway.legacy.my.MyException;

import java.util.HashMap;
import java.util.Map;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public class KICCData {
    private final boolean DEBUG = true;

    public byte[] makeData(int format, Map<String, String> vanData) throws MyException {
        String indicator = "=>";
        
        try{
            String field01 = AppUtil.checkNull(vanData.get("VS_FIELD01"));

            StringBuffer bodyBuf = new StringBuffer();

            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD01"))); debug(indicator, "D01", AppUtil.checkNull(vanData.get("VS_FIELD01")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD02"))); debug(indicator, "D02", AppUtil.checkNull(vanData.get("VS_FIELD02")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD03"))); debug(indicator, "D03", AppUtil.checkNull(vanData.get("VS_FIELD03")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD04"))); debug(indicator, "D04", AppUtil.checkNull(vanData.get("VS_FIELD04")));

            if(field01.equals("7720") || field01.equals("7730") || field01.equals("7750") || field01.equals("7760")){
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD05_1"))); debug(indicator, "D05", AppUtil.checkNull(vanData.get("VS_FIELD05_1")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD06_1"))); debug(indicator, "D06", AppUtil.checkNull(vanData.get("VS_FIELD06_1")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD07_1"))); debug(indicator, "D07", AppUtil.checkNull(vanData.get("VS_FIELD07_1")));
            }else{
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD05"))); debug(indicator, "D05", AppUtil.checkNull(vanData.get("VS_FIELD05")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD06"))); debug(indicator, "D06", AppUtil.checkNull(vanData.get("VS_FIELD06")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD07"))); debug(indicator, "D07", AppUtil.checkNull(vanData.get("VS_FIELD07")));
            }
            
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD08"))); debug(indicator, "D08", AppUtil.checkNull(vanData.get("VS_FIELD08")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD09"))); debug(indicator, "D09", AppUtil.checkNull(vanData.get("VS_FIELD09")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD10"))); debug(indicator, "D10", AppUtil.checkNull(vanData.get("VS_FIELD10")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD11"))); debug(indicator, "D11", AppUtil.checkNull(vanData.get("VS_FIELD11")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD12"))); debug(indicator, "D12", AppUtil.checkNull(vanData.get("VS_FIELD12")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD13"))); debug(indicator, "D13", AppUtil.checkNull(vanData.get("VS_FIELD13")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD14"))); debug(indicator, "D14", AppUtil.checkNull(vanData.get("VS_FIELD14")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD15"))); debug(indicator, "D15", AppUtil.checkNull(vanData.get("VS_FIELD15")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD16_1"))); debug(indicator, "D16", AppUtil.checkNull(vanData.get("VS_FIELD16_1")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD16_2"))); debug(indicator, "D16", AppUtil.checkNull(vanData.get("VS_FIELD16_2")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD16_3"))); debug(indicator, "D16", AppUtil.checkNull(vanData.get("VS_FIELD16_3")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD16_4"))); debug(indicator, "D16", AppUtil.checkNull(vanData.get("VS_FIELD16_4")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD17"))); debug(indicator, "D17", AppUtil.checkNull(vanData.get("VS_FIELD17")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD18"))); debug(indicator, "D18", AppUtil.checkNull(vanData.get("VS_FIELD18")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD19"))); debug(indicator, "D19", AppUtil.checkNull(vanData.get("VS_FIELD19")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD20"))); debug(indicator, "D20", AppUtil.checkNull(vanData.get("VS_FIELD20"))); 
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD21_1"))); debug(indicator, "D21", AppUtil.checkNull(vanData.get("VS_FIELD21_1")));
            bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD21_2"))); debug(indicator, "D21", AppUtil.checkNull(vanData.get("VS_FIELD21_2")));
            if(field01.equals("1130") || field01.equals("1210")){
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_1"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_1")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_2"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_2")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_3"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_3")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_4"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_4")));
                // 하나카드 요청으로 사용자 IP 정보전송
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_5"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_5")));
                
            }else{
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD22_1"))); debug(indicator, "D22", AppUtil.checkNull(vanData.get("VS_FIELD22_1")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD22_2"))); debug(indicator, "D22", AppUtil.checkNull(vanData.get("VS_FIELD22_2")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD22_3"))); debug(indicator, "D22", AppUtil.checkNull(vanData.get("VS_FIELD22_3")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD22_4"))); debug(indicator, "D22", AppUtil.checkNull(vanData.get("VS_FIELD22_4")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD22_5"))); debug(indicator, "D22", AppUtil.checkNull(vanData.get("VS_FIELD22_5")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD22_6"))); debug(indicator, "D22", AppUtil.checkNull(vanData.get("VS_FIELD22_6")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_1"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_1")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_2"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_2")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_3"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_3")));
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_4"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_4")));
                // 하나카드 요청으로 사용자 IP 정보전송
                bodyBuf.append(AppUtil.checkNull(vanData.get("VS_FIELD23_5"))); debug(indicator, "D23", AppUtil.checkNull(vanData.get("VS_FIELD23_5")));
            }

            String header = makeHeader(AppUtil.checkNull(vanData.get("VS_FIELD00")), AppUtil.checkNull(vanData.get("VD_SEQNO")), bodyBuf.toString().getBytes().length);

            String vanStr = header + bodyBuf.toString();

            return vanStr.getBytes();
        }catch(MyException e){
            throw e;
        }catch(Exception e){
            throw new MyException("[KICCData::makeData]", MyException.SY21, "Exception:"+e, MyException.sysErrMsg);
        }
    }

    public Map<String, String> parseData(byte[] recvBytes, String field01) throws MyException{
        Map<String, String> vanData = new HashMap<String, String>();
        String indicator = "<=";
        try{
            int idx = 123;
                
            vanData.put("VR_FIELD01", new String(recvBytes, idx,  4)); idx += 4;    debug(indicator, "D01", AppUtil.checkNull(vanData.get("VR_FIELD01")));
            vanData.put("VR_FIELD02", new String(recvBytes, idx, 12)); idx += 12;   debug(indicator, "D02", AppUtil.checkNull(vanData.get("VR_FIELD02")));
            vanData.put("VR_FIELD03", new String(recvBytes, idx,  1)); idx += 1;    debug(indicator, "D03", AppUtil.checkNull(vanData.get("VR_FIELD03")));
            vanData.put("VR_FIELD04", new String(recvBytes, idx, 12)); idx += 12;   debug(indicator, "D04", AppUtil.checkNull(vanData.get("VR_FIELD04")));
            vanData.put("VR_FIELD05", new String(recvBytes, idx, 20)); idx += 20;   debug(indicator, "D05", AppUtil.checkNull(vanData.get("VR_FIELD05")));
            vanData.put("VR_FIELD06", new String(recvBytes, idx,  4)); idx += 4;    debug(indicator, "D06", AppUtil.checkNull(vanData.get("VR_FIELD06")));
            vanData.put("VR_FIELD07", new String(recvBytes, idx,  2)); idx += 2;    debug(indicator, "D07", AppUtil.checkNull(vanData.get("VR_FIELD07")));
            vanData.put("VR_FIELD08", new String(recvBytes, idx, 12)); idx += 12;   debug(indicator, "D08", AppUtil.checkNull(vanData.get("VR_FIELD08")));
            vanData.put("VR_FIELD09", new String(recvBytes, idx, 16)); idx += 16;   debug(indicator, "D09", AppUtil.checkNull(vanData.get("VR_FIELD09")));
            vanData.put("VR_FIELD10", new String(recvBytes, idx, 16)); idx += 16;   debug(indicator, "D10", AppUtil.checkNull(vanData.get("VR_FIELD10")));
            vanData.put("VR_FIELD11", new String(recvBytes, idx, 16)); idx += 16;   debug(indicator, "D11", AppUtil.checkNull(vanData.get("VR_FIELD11")));
            vanData.put("VR_FIELD12", new String(recvBytes, idx, 16)); idx += 16;   debug(indicator, "D12", AppUtil.checkNull(vanData.get("VR_FIELD12")));
            vanData.put("VR_FIELD13", new String(recvBytes, idx, 12)); idx += 12;   debug(indicator, "D13", AppUtil.checkNull(vanData.get("VR_FIELD13")));
            vanData.put("VR_FIELD14", new String(recvBytes, idx, 16)); idx += 16;   debug(indicator, "D14", AppUtil.checkNull(vanData.get("VR_FIELD14")));
            vanData.put("VR_FIELD15", new String(recvBytes, idx,  2)); idx += 2;    debug(indicator, "D15", AppUtil.checkNull(vanData.get("VR_FIELD15")));
            vanData.put("VR_FIELD16", new String(recvBytes, idx,  2)); idx += 2;    debug(indicator, "D16", AppUtil.checkNull(vanData.get("VR_FIELD16")));
            vanData.put("VR_FIELD17", new String(recvBytes, idx, 15)); idx += 15;   debug(indicator, "D17", AppUtil.checkNull(vanData.get("VR_FIELD17")));
            vanData.put("VR_FIELD18", new String(recvBytes, idx,  2)); idx += 2;    debug(indicator, "D18", AppUtil.checkNull(vanData.get("VR_FIELD18")));
            vanData.put("VR_FIELD19", new String(recvBytes, idx, 20)); idx += 20;   debug(indicator, "D19", AppUtil.checkNull(vanData.get("VR_FIELD19")));
            if(!field01.equals("1130") && !field01.equals("1210")){
                vanData.put("VR_FIELD20", new String(recvBytes, idx, 16)); idx += 16;   debug(indicator, "D20", AppUtil.checkNull(vanData.get("VR_FIELD20")));
                vanData.put("VR_FIELD21", new String(recvBytes, idx, 10)); idx += 10;   debug(indicator, "D21", AppUtil.checkNull(vanData.get("VR_FIELD21")));
                vanData.put("VR_FIELD22", new String(recvBytes, idx, 11)); idx += 11;   debug(indicator, "D22", AppUtil.checkNull(vanData.get("VR_FIELD22")));
                vanData.put("VR_FIELD23", new String(recvBytes, idx,  3)); idx += 3;    debug(indicator, "D23", AppUtil.checkNull(vanData.get("VR_FIELD23")));
                vanData.put("VR_FIELD24", new String(recvBytes, idx,  9)); idx += 9;    debug(indicator, "D24", AppUtil.checkNull(vanData.get("VR_FIELD24")));
                vanData.put("VR_FIELD25", new String(recvBytes, idx,  9)); idx += 9;    debug(indicator, "D25", AppUtil.checkNull(vanData.get("VR_FIELD25")));
                vanData.put("VR_FIELD26", new String(recvBytes, idx, 16)); idx += 16;   debug(indicator, "D26", AppUtil.checkNull(vanData.get("VR_FIELD26")));
                vanData.put("VR_FIELD27", new String(recvBytes, idx,  2)); idx += 2;    debug(indicator, "D27", AppUtil.checkNull(vanData.get("VR_FIELD27")));
                vanData.put("VR_FIELD28", new String(recvBytes, idx,  2)); idx += 2;    debug(indicator, "D28", AppUtil.checkNull(vanData.get("VR_FIELD28")));
                vanData.put("VR_FIELD29", new String(recvBytes, idx,  5)); idx += 5;   debug(indicator, "D29", AppUtil.checkNull(vanData.get("VR_FIELD29")));
                vanData.put("VR_FIELD30", new String(recvBytes, idx, 25)); idx += 25;   debug(indicator, "D30", AppUtil.checkNull(vanData.get("VR_FIELD30")));
                vanData.put("VR_FIELD31", new String(recvBytes, idx, 12)); idx += 12;    debug(indicator, "D31", AppUtil.checkNull(vanData.get("VR_FIELD31")));
                //vanData.put("VR_FIELD32", new String(recvBytes, idx, 18)); idx += 18;    debug(indicator, "D32", AppUtil.checkNull(vanData.get("VR_FIELD32")));
               
            }
        }catch(Exception e){
        	throw new MyException("[KICCData::parseData]", MyException.SY24, "Exception:"+e, MyException.sysErrMsg);
        }

        return vanData;
    }

    private String makeHeader(String tid, String traceNo, int bodyLen)throws MyException{
    	try{
	        StringBuffer headerBuf = new StringBuffer();

            headerBuf.append(AppUtil.formatData(String.valueOf(bodyLen+123), AppUtil.RIGHT, 4, '0'));
            headerBuf.append("0");               //암호화 여부
            headerBuf.append("0412");            //전문버전
            headerBuf.append(AppUtil.formatData(tid, AppUtil.LEFT, 10, ' '));      //단말기번호
            headerBuf.append("SOFTF");           //취급기관코드
            headerBuf.append(AppUtil.formatData(traceNo, AppUtil.LEFT, 12, ' '));             //전문일련번호
            headerBuf.append("30");              //가맹점 Timeout
            headerBuf.append(AppUtil.formatData("KRPARTNER", AppUtil.LEFT, 20, ' '));     //프로그램 관리자명
            headerBuf.append("1566-3441    ");   //회사전화번호
            headerBuf.append("010-8651-7519");   //휴대폰번호
            headerBuf.append(AppUtil.formatData("", AppUtil.LEFT, 43, ' ')); //여유필드

            return headerBuf.toString();
		}catch(Exception e){
			throw new MyException("[KICCData::makeHeader]", MyException.SY21, "Exception:"+e, MyException.sysErrMsg);
        }
    }
    
    private void debug(String indicator, String name, String msg){
        if(DEBUG) System.out.println("[DEBUG:KICCData]"+indicator+":"+name+"\t"+msg.getBytes().length+"bytes\t=>\t:"+msg+":");
    }
}
