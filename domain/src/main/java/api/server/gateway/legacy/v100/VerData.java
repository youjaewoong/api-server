/*
 * Created on 2004. 9. 10.
 *
 */
package api.server.gateway.legacy.v100;


import api.server.common.helper.AppUtil;
import api.server.exception.custom.BusinessException;
import api.server.gateway.legacy.AppData;

import java.util.Map;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public class VerData extends AppData {
    
    private final boolean DEBUG = false;

	public Map<String, String> parseData(byte[] recvBytes) throws BusinessException {

        //헤더 파싱
        String field01 = new String(recvBytes,  0,  4);	dataInfo.put("RH01", field01);	//전문버전
        String field02 = new String(recvBytes,  4,  4); dataInfo.put("RH02", field02);  //Msg ID
        String field03 = new String(recvBytes,  8,  4); dataInfo.put("RH03", field03);  //Message Type
        String field04 = new String(recvBytes, 12,  4); dataInfo.put("RH04", field04);  //응답코드
        String field05 = new String(recvBytes, 16, 14); dataInfo.put("RH05", field05);  //전송일시

        try{
			//전문별 분석
			int format = getFormat(field03); dataInfo.put("RH00", String.valueOf(format));

			switch(format){
                case F0100:     //신용카드 승인
                case F0120:     //신용카드 인증(수동매입)
                case F0101:     //신용카드 승인(PCINS)
                case F0121:     //신용카드 인증(PCINS)
                case F0102:     //BC UPOT 신용카드 승인(자체 저장)
                case F0122:     //BC UPOT 신용카드 승인(자체 저장 수동매입)    
                case F0400:     //일반 환급요청
                case F9999:     //Edgar 부하테스트 (2023.03.15)
                {
                    String field06 = new String(recvBytes,  30, 10); dataInfo.put("RB01", field06);     //가맹점 아이디
                    String field07 = new String(recvBytes,  40, 24); dataInfo.put("RB02", field07);     //PG 일련번호
                    String field08 = new String(recvBytes,  64,  3); dataInfo.put("RB03", field08);     //통화코드
                    String field09 = new String(recvBytes,  67, 12); dataInfo.put("RB04", field09);     //거래금액
                    String field10 = new String(recvBytes,  79, 12); dataInfo.put("RB05", field10);     //봉사료
                    String field11 = new String(recvBytes,  91, 12); dataInfo.put("RB06", field11);     //세금
                    String field12 = new String(recvBytes, 103,  2); dataInfo.put("RB07", field12);     //할부기간
                    String field13 = new String(recvBytes, 105,  4); dataInfo.put("RB08", field13);     //카드구분
                    String field14 = new String(recvBytes, 109, 20); dataInfo.put("RB09", field14);     //카드번호
                    String field15 = new String(recvBytes, 129,  4); dataInfo.put("RB10", field15);     //유효기간
                    String field16 = new String(recvBytes, 133,  5); dataInfo.put("RB11", field16);     //CVC/4DBC
                    String field17 = new String(recvBytes, 138, 20); dataInfo.put("RB12", field17);     //Holder
                    String field18 = new String(recvBytes, 158,  2); dataInfo.put("RB13", field18);     //국가코드
                    /*
                     해외카드 3DS 구분
                     Y : 3DS1.0, Z : 3DS2.0, N : 비인증, V : eWallete
                     eWallet로부터의 인증 ResponseData cavv, track2Data의 필드 정의
                     cavv는 3DS 구분 'Y'일 경우와 동일한 cavv 필드 이용
                     track2data는 3DS 구분 'Y'일 경우의 xid 필드 이용 (37byte 필드 길이 충족)
                    */
                    String field19 = new String(recvBytes, 160,  1); dataInfo.put("RB14", field19);     //3DS
                    String field20 = new String(recvBytes, 161, 40); dataInfo.put("RB15", field20);     //cavv
                    String field21 = new String(recvBytes, 201, 40); dataInfo.put("RB16", field21);     //xid
                    String field22 = new String(recvBytes, 241,  1); dataInfo.put("RB17", field22);     //paresstatus
                    String field23 = new String(recvBytes, 242,  2); dataInfo.put("RB18", field23);     //eci flag
                    String field24 = new String(recvBytes, 244, 25); dataInfo.put("RB25", field24);     //dynamic description name (dcc쪽 필드와 이름 동일하게)
                    String field25 = new String(recvBytes, 269, 13); dataInfo.put("RB26", field25);     //dynamic description tel (dcc쪽 필드와 이름 동일하게)
                    
                    if(format == F0101 || format == F0121){
                        //PCINS 용
                        String field26 = new String(recvBytes, 282, 30); dataInfo.put("RB90", field26);     //가맹점 Ref
                        String field27 = new String(recvBytes, 312, 15); dataInfo.put("RB91", field27);     //고객접속 IP
                        String field28 = new String(recvBytes, 327, 64); dataInfo.put("RB92", field28);     //고객명
                        String field29 = new String(recvBytes, 391, 32); dataInfo.put("RB93", field29);     //고객 연락처
                        String field30 = new String(recvBytes, 423, 64); dataInfo.put("RB94", field30);     //고객 이메일
                        String field31 = new String(recvBytes, 487, 10); dataInfo.put("RB95", field31);     //파트너코드
                    }
                    if(format == F0102  || format == F0122) {
                        String field26 = new String(recvBytes, 282, 14); dataInfo.put("RB70", field26);     //거래일시
                        String field27 = new String(recvBytes, 296, 12); dataInfo.put("RB71", field27);     //승인번호
                        String field28 = new String(recvBytes, 308,  2); dataInfo.put("RB72", field28);     //발급사코드
                        String field29 = new String(recvBytes, 310, 12); dataInfo.put("RB73", field29);     //Traceno (삼성UPOP용)
                    }
                    
                    break;
                }
                case F0125:     //Capture
                case F0126:     //Capture(PCINS)
                {
                    String field06 = new String(recvBytes,  30, 10); dataInfo.put("RB01", field06);     //가맹점 아이디
                    String field07 = new String(recvBytes,  40, 24); dataInfo.put("RB02", field07);     //PG 일련번호
                    String field08 = new String(recvBytes,  64,  3); dataInfo.put("RB03", field08);     //통화코드
                    String field09 = new String(recvBytes,  67, 12); dataInfo.put("RB04", field09);     //거래금액
                    String field10 = new String(recvBytes,  79, 12); dataInfo.put("RB05", field10);     //봉사료
                    String field11 = new String(recvBytes,  91, 12); dataInfo.put("RB06", field11);     //세금
                    String field13 = new String(recvBytes, 103,  2); dataInfo.put("RB07", field13);     //할부기간
                    
                    if(format == F0126){
                        //PCINS 용
                        String field14 = new String(recvBytes, 105, 30); dataInfo.put("RB90", field14);     //가맹점 Ref
                    }  
                    break;
                }
                case F0140:     //Referral Approval
                {
                    String field06 = new String(recvBytes,  30, 10); dataInfo.put("RB01", field06);     //가맹점 아이디
                    String field07 = new String(recvBytes,  40, 24); dataInfo.put("RB02", field07);     //PG 일련번호
                    String field08 = new String(recvBytes,  64,  3); dataInfo.put("RB03", field08);     //통화코드
                    String field09 = new String(recvBytes,  67, 12); dataInfo.put("RB04", field09);     //거래금액
                    String field10 = new String(recvBytes,  79, 12); dataInfo.put("RB05", field10);     //봉사료
                    String field11 = new String(recvBytes,  91, 12); dataInfo.put("RB06", field11);     //세금
                    String field13 = new String(recvBytes, 103,  2); dataInfo.put("RB07", field13);     //할부기간
                    String field12 = new String(recvBytes, 105, 12); dataInfo.put("RB50", field12);     //취소금액
                    String field14 = new String(recvBytes, 117, 12); dataInfo.put("RB51", field14);     //승인번호
                    String field15 = new String(recvBytes, 129,  8); dataInfo.put("RB52", field15);     //거래일자

                    break;
                }
                case F0160:     //DCC 환율조회
                case F0180:     //DCC 신용승인
                {
                    String field06 = new String(recvBytes,  30, 10); dataInfo.put("RB01", field06);     //가맹점 아이디
                    String field07 = new String(recvBytes,  40, 24); dataInfo.put("RB02", field07);     //PG 일련번호
                    String field08 = new String(recvBytes,  64,  3); dataInfo.put("RB03", field08);     //통화코드
                    String field09 = new String(recvBytes,  67, 12); dataInfo.put("RB04", field09);     //거래금액
                    String field10 = new String(recvBytes,  79, 12); dataInfo.put("RB05", field10);     //봉사료
                    String field11 = new String(recvBytes,  91, 12); dataInfo.put("RB06", field11);     //세금
                    String field12 = new String(recvBytes, 103,  2); dataInfo.put("RB07", field12);     //할부기간
                    String field13 = new String(recvBytes, 105,  4); dataInfo.put("RB08", field13);     //카드구분
                    String field14 = new String(recvBytes, 109, 20); dataInfo.put("RB09", field14);     //카드번호
                    String field15 = new String(recvBytes, 129,  4); dataInfo.put("RB10", field15);     //유효기간
                    String field16 = new String(recvBytes, 133,  5); dataInfo.put("RB11", field16);     //CVC/4DBC
                    String field17 = new String(recvBytes, 138, 20); dataInfo.put("RB12", field17);     //Holder
                    String field18 = new String(recvBytes, 158,  2); dataInfo.put("RB13", field18);     //국가코드
                    /* 
                     해외카드 3DS 구분
                     Y : 3DS1.0, Z : 3DS2.0, N : 비인증, V : eWallete 
                     eWallet로부터의 인증 ResponseData cavv, track2Data의 필드 정의
                     cavv는 3DS 구분 'Y'일 경우와 동일한 cavv 필드 이용
                     track2data는 3DS 구분 'Y'일 경우의 xid 필드 이용 (37byte 필드 길이 충족)
                    */
                    String field19 = new String(recvBytes, 160,  1); dataInfo.put("RB14", field19);     //3DS
                    String field20 = new String(recvBytes, 161, 40); dataInfo.put("RB15", field20);     //cavv
                    String field21 = new String(recvBytes, 201, 40); dataInfo.put("RB16", field21);     //xid
                    String field22 = new String(recvBytes, 241,  1); dataInfo.put("RB17", field22);     //paresstatus
                    String field23 = new String(recvBytes, 242,  2); dataInfo.put("RB18", field23);     //eci flag
                    String field24 = new String(recvBytes, 244, 12); dataInfo.put("RB19", field24);     //Tracking Code
                    String field25 = new String(recvBytes, 256, 12); dataInfo.put("RB20", field25);     //자국통화금액
                    String field26 = new String(recvBytes, 268, 10); dataInfo.put("RB21", field26);     //Exchange Rate
                    String field27 = new String(recvBytes, 278,  3); dataInfo.put("RB22", field27);     //자국통화코드
                    String field28 = new String(recvBytes, 281,  3); dataInfo.put("RB23", field28);     //기본결제 통화
                    String field29 = new String(recvBytes, 284,  9); dataInfo.put("RB24", field29);     //원화 통화금액
                    String field30 = new String(recvBytes, 293, 25); dataInfo.put("RB25", field30);     //dynamic description name
                    String field31 = new String(recvBytes, 318, 13); dataInfo.put("RB26", field31);     //dynamic description tel
                    break;
                }
                case F0181:     //MCP 신용승인
                case F0182:     //MCP 신용승인(PCINS)
                case F0183:     //MCP 인증요청(PCINS)
                case F0184:     //MCP 인증요청
                {
                    String field06 = new String(recvBytes,  30, 10); dataInfo.put("RB01", field06);     //가맹점 아이디
                    String field07 = new String(recvBytes,  40, 24); dataInfo.put("RB02", field07);     //PG 일련번호
                    String field08 = new String(recvBytes,  64,  3); dataInfo.put("RB03", field08);     //Base 통화코드
                    String field09 = new String(recvBytes,  67, 12); dataInfo.put("RB04", field09);     //Base 거래금액
                    String field10 = new String(recvBytes,  79, 12); dataInfo.put("RB05", field10);     //봉사료
                    String field11 = new String(recvBytes,  91, 12); dataInfo.put("RB06", field11);     //세금
                    String field12 = new String(recvBytes, 103,  2); dataInfo.put("RB07", field12);     //할부기간
                    String field13 = new String(recvBytes, 105,  4); dataInfo.put("RB08", field13);     //카드구분
                    String field14 = new String(recvBytes, 109, 20); dataInfo.put("RB09", field14);     //카드번호
                    String field15 = new String(recvBytes, 129,  4); dataInfo.put("RB10", field15);     //유효기간
                    String field16 = new String(recvBytes, 133,  5); dataInfo.put("RB11", field16);     //CVC/4DBC
                    String field17 = new String(recvBytes, 138, 20); dataInfo.put("RB12", field17);     //Holder
                    String field18 = new String(recvBytes, 158,  2); dataInfo.put("RB13", field18);     //국가코드
                    /* 
                     베트남 eWallete 추가로 인한 내부전문의 필드 재정의..
                     3DS 구분 'E' 로 구분 처리 MPI : M, eWallete : E, 비인증 : 'N'
                     eWallet로부터의 인증 ResponseData cavv, track2Data의 필드 정의
                     cavv는 3DS 구분 'M'일 경우와 동일한 cavv 필드 이용
                     track2data는 3DS 구분 'M'일 경우의 xid 필드 이용 (37byte 필드 길이 충족)
                    */
                    String field19 = new String(recvBytes, 160,  1); dataInfo.put("RB14", field19);     //3DS
                    String field20 = new String(recvBytes, 161, 40); dataInfo.put("RB15", field20);     //cavv
                    String field21 = new String(recvBytes, 201, 40); dataInfo.put("RB16", field21);     //xid
                    String field22 = new String(recvBytes, 241,  1); dataInfo.put("RB17", field22);     //paresstatus
                    String field23 = new String(recvBytes, 242,  2); dataInfo.put("RB18", field23);     //eci flag
                    String field24 = new String(recvBytes, 244, 25); dataInfo.put("RB19", field24);     //dynamic description name
                    String field25 = new String(recvBytes, 269, 13); dataInfo.put("RB20", field25);     //dynamic description tel
                    String field26 = new String(recvBytes, 282,  3); dataInfo.put("RB21", field26);     //선택통화 코드
                    String field27 = new String(recvBytes, 285, 12); dataInfo.put("RB22", field27);     //선택통화 금액
                    String field28 = new String(recvBytes, 297,  1); dataInfo.put("RB23", field28);     //선택통화 금액 Minor Unit
                    String field29 = new String(recvBytes, 298, 10); dataInfo.put("RB24", field29);     //Converting Rate
                    String field30 = new String(recvBytes, 308,  3); dataInfo.put("RB25", field30);     //Base통화 코드
                    String field31 = new String(recvBytes, 311, 12); dataInfo.put("RB26", field31);     //원화금액
                    String field32 = new String(recvBytes, 323,  1); dataInfo.put("RB27", field32);     //DCC status
                    String field33 = new String(recvBytes, 324, 20); dataInfo.put("RB28", field33);     //RATEID
                    
                    if(format == F0182 || format == F0183){
                        //PCINS 용
                        String field34 = new String(recvBytes, 344, 30); dataInfo.put("RB90", field34);     //가맹점 Ref
                        String field35 = new String(recvBytes, 374, 15); dataInfo.put("RB91", field35);     //고객접속 IP
                        String field36 = new String(recvBytes, 389, 64); dataInfo.put("RB92", field36);     //고객명
                        String field37 = new String(recvBytes, 453, 32); dataInfo.put("RB93", field37);     //고객 연락처
                        String field38 = new String(recvBytes, 485, 64); dataInfo.put("RB94", field38);     //고객 이메일
                        String field39 = new String(recvBytes, 549, 10); dataInfo.put("RB95", field39);     //파트너코드
                        String field40 = new String(recvBytes, 559,  3); dataInfo.put("RB96", field40);     //사이트 통화 코드
                        String field41 = new String(recvBytes, 562, 12); dataInfo.put("RB97", field41);     //사이트 통화 금액
                    }
                    
                    break;
                }
                case F0200:     //신용카드 취소
                case F0201:     //신용카드 취소(PCINS)
                case F0202:     //BC UPOT 취소(자체 취소)
                case F0206:     //BC UPOT 취소(부분 취소)  
                {
                    String field06 = new String(recvBytes,  30, 10); dataInfo.put("RB01", field06);     //가맹점 아이디
                    String field07 = new String(recvBytes,  40, 24); dataInfo.put("RB02", field07);     //PG 일련번호
                    String field08 = new String(recvBytes,  64,  3); dataInfo.put("RB03", field08);     //통화코드
                    String field09 = new String(recvBytes,  67, 12); dataInfo.put("RB04", field09);     //거래금액
                    String field10 = new String(recvBytes,  79, 12); dataInfo.put("RB05", field10);     //봉사료
                    String field11 = new String(recvBytes,  91, 12); dataInfo.put("RB06", field11);     //세금
                    String field13 = new String(recvBytes, 103,  2); dataInfo.put("RB07", field13);     //할부기간
                    String field12 = new String(recvBytes, 105, 12); dataInfo.put("RB50", field12);     //취소금액
                    String field14 = new String(recvBytes, 117, 12); dataInfo.put("RB51", field14);     //승인번호
                    String field15 = new String(recvBytes, 129,  8); dataInfo.put("RB52", field15);     //거래일자
                    
                    if(format == F0201){
                        //PCINS 용
                        String field16 = new String(recvBytes, 137, 30); dataInfo.put("RB90", field16);     //가맹점 Ref
                    } else if (format == F0202 || format == F0206) {
                        String field16 = new String(recvBytes, 137, 12); dataInfo.put("RB73", field16);     //Traceno (삼성UPOP용)
                        if (recvBytes.length >= 150) {
                            String field17 = new String(recvBytes, 149, 1); dataInfo.put("RB74", field17);  //BCUPOP 웹 통신여부
                        }
                    }                     
                    break;
                }
                case F0204:
                {
                    String field06 = new String(recvBytes,  30, 10); dataInfo.put("RB01", field06);     //가맹점 아이디
                    String field07 = new String(recvBytes,  40, 24); dataInfo.put("RB02", field07);     //PG 일련번호
                    String field08 = new String(recvBytes,  64,  3); dataInfo.put("RB03", field08);     //통화코드
                    String field09 = new String(recvBytes,  67, 12); dataInfo.put("RB04", field09);     //거래금액
                    String field10 = new String(recvBytes,  79, 12); dataInfo.put("RB05", field10);     //봉사료
                    String field11 = new String(recvBytes,  91, 12); dataInfo.put("RB06", field11);     //세금
                    String field13 = new String(recvBytes, 103,  2); dataInfo.put("RB07", field13);     //할부기간
                    String field12 = new String(recvBytes, 105, 12); dataInfo.put("RB50", field12);     //취소금액
                    String field14 = new String(recvBytes, 117, 12); dataInfo.put("RB51", field14);     //승인번호
                    String field15 = new String(recvBytes, 129,  8); dataInfo.put("RB52", field15);     //거래일자
                    String field16 = new String(recvBytes, 137,  3); dataInfo.put("RB53", field16);     //자국통화
                    String field17 = new String(recvBytes, 140, 12); dataInfo.put("RB54", field17);     //자국통화금액
                    break;
                }
                default:
                	// throw new BusinessException("[VerData::praseData]", BusinessException.SY02, "Undefined code("+field03+")", BusinessException.sysErrMsg);
		}
	}catch(BusinessException e){
		throw e;
	}catch(Exception e){
		// throw new BusinessException("[VerData::praseData] {}", e.toString());
	}
        if(DEBUG){
            for(int i=0; i<dataInfo.size(); i++){
                String name = "RB" + AppUtil.formatData(i, AppUtil.RIGHT, 2, '0');
                if(dataInfo.get(name) != null){
                    String value = AppUtil.checkNull(dataInfo.get(name));
                    System.out.println("[DEBUG:VerData]"+name+"\t"+value.getBytes().length+"bytes\t=>\t:"+dataInfo.get(name)+":");
                }
            }
            
        }
        
		return dataInfo;
	}

	public byte[] makeData() throws BusinessException{
        byte[] sendBytes = null;
		StringBuffer sendBuf = new StringBuffer();

		try{
            //전문별 구성
			int format = Integer.parseInt((String)dataInfo.get("RH00"));

			switch(format){
                case F0100:      //신용카드 승인
                case F0101:      //신용카드 승인(PCINS)
                case F0102:     //BC UPOT 신용카드 승인(자체 저장)    
                case F0120:      //신용카드 인증(수동매입)
                case F0121:      //신용카드 인증(PCINS)
                case F0140:      //전화승인
                case F0181:      //MCP 승인요청
                case F0182:      //MCP 승인요청(PCINS)
                case F0183:      //MCP 인증요청(PCINS)
                case F0184:      //MCP 인증요청    
                case F0400:      //신용카드 환급
                case F9999:      //Edgar 부하테스트 (2023.03.15) 
                    sendBuf.append((String)dataInfo.get("SB01"));   //가맹점 아이디
                    sendBuf.append((String)dataInfo.get("SB02"));   //PG 일련번호
                    sendBuf.append((String)dataInfo.get("SB03"));   //통화코드
                    sendBuf.append((String)dataInfo.get("SB04"));   //거래금액
                    sendBuf.append((String)dataInfo.get("SB05"));   //봉사료
                    sendBuf.append((String)dataInfo.get("SB06"));   //세금
                    sendBuf.append((String)dataInfo.get("SB07"));   //할부기간
                    sendBuf.append((String)dataInfo.get("SB08"));   //카드구분
                    sendBuf.append((String)dataInfo.get("SB09"));   //카드번호
                    sendBuf.append((String)dataInfo.get("SB10"));   //유효기간
                    sendBuf.append((String)dataInfo.get("SB11"));   //Holder
                    sendBuf.append((String)dataInfo.get("SB12"));   //국가코드
                    sendBuf.append((String)dataInfo.get("SB13"));   //승인번호
                    sendBuf.append((String)dataInfo.get("SB14"));   //거래일시
                    sendBuf.append((String)dataInfo.get("SB15"));   //Seqno(tracking code)
                    if(format == F0182 || format == F0183){
                        sendBuf.append((String)dataInfo.get("SB16"));   //MDR 통화코드
                        sendBuf.append((String)dataInfo.get("SB17"));   //MDR
                        sendBuf.append((String)dataInfo.get("SB18"));   //MDR금액
                        sendBuf.append((String)dataInfo.get("SB19"));   //정산일자
                    }
                    break;
                case F0125:     //Capture
                case F0126:     //Capture(PCINS)
                    sendBuf.append((String)dataInfo.get("SB01"));   //가맹점 아이디
                    sendBuf.append((String)dataInfo.get("SB02"));   //PG 일련번호
                    sendBuf.append((String)dataInfo.get("SB03"));   //통화코드
                    sendBuf.append((String)dataInfo.get("SB04"));   //거래금액
                    sendBuf.append((String)dataInfo.get("SB05"));   //봉사료
                    sendBuf.append((String)dataInfo.get("SB06"));   //세금
                    sendBuf.append((String)dataInfo.get("SB07"));   //할부기간
                    sendBuf.append((String)dataInfo.get("SB08"));   //승인번호
                    sendBuf.append((String)dataInfo.get("SB09"));   //거래일시
                    sendBuf.append((String)dataInfo.get("SB10"));   //Seqno(tracking code)
                    break;
                case F0160:     //DCC 환율조회
                case F0180:     //DCC 승인요청
                    sendBuf.append((String)dataInfo.get("SB01"));   //가맹점 아이디
                    sendBuf.append((String)dataInfo.get("SB02"));   //PG 일련번호
                    sendBuf.append((String)dataInfo.get("SB03"));   //통화코드
                    sendBuf.append((String)dataInfo.get("SB04"));   //거래금액
                    sendBuf.append((String)dataInfo.get("SB05"));   //봉사료
                    sendBuf.append((String)dataInfo.get("SB06"));   //세금
                    sendBuf.append((String)dataInfo.get("SB07"));   //할부기간
                    sendBuf.append((String)dataInfo.get("SB08"));   //카드구분
                    sendBuf.append((String)dataInfo.get("SB09"));   //카드번호
                    sendBuf.append((String)dataInfo.get("SB10"));   //유효기간
                    sendBuf.append((String)dataInfo.get("SB11"));   //Holder
                    sendBuf.append((String)dataInfo.get("SB12"));   //국가코드
                    sendBuf.append((String)dataInfo.get("SB13"));   //승인번호
                    sendBuf.append((String)dataInfo.get("SB14"));   //거래일시
                    sendBuf.append((String)dataInfo.get("SB15"));   //Seqno(tracking code)
                    sendBuf.append((String)dataInfo.get("SB16"));   //DCC 결제정보
                    sendBuf.append((String)dataInfo.get("SB17"));   //Foreign Curerncy Number
                    sendBuf.append((String)dataInfo.get("SB18"));   //Exchange Rate
                    sendBuf.append((String)dataInfo.get("SB19"));   //Inverted Rate
                    sendBuf.append((String)dataInfo.get("SB20"));   //외환 영문코드
                    sendBuf.append((String)dataInfo.get("SB21"));   //Mark Up Percent
                    sendBuf.append((String)dataInfo.get("SB22"));   //Commission Percent
                    sendBuf.append((String)dataInfo.get("SB23"));   //Commission Value
                    sendBuf.append((String)dataInfo.get("SB24"));   //Terminal Id
                    sendBuf.append((String)dataInfo.get("SB25"));   //Merchannt No
                    break;
                case F0200:     //신용카드 취소
                case F0201:     //신용카드 취소(PCINS)
                case F0202:     //BC UPOT 취소(자체 취소)    
                    sendBuf.append((String)dataInfo.get("SB01"));   //가맹점 아이디
                    sendBuf.append((String)dataInfo.get("SB02"));   //PG 일련번호
                    sendBuf.append((String)dataInfo.get("SB03"));   //통화코드
                    sendBuf.append((String)dataInfo.get("SB04"));   //거래금액
                    sendBuf.append((String)dataInfo.get("SB05"));   //봉사료
                    sendBuf.append((String)dataInfo.get("SB06"));   //세금
                    sendBuf.append((String)dataInfo.get("SB07"));   //할부기간
                    sendBuf.append((String)dataInfo.get("SB08"));   //취소금액
                    sendBuf.append((String)dataInfo.get("SB09"));   //승인번호
                    sendBuf.append((String)dataInfo.get("SB10"));   //거래일시
                    sendBuf.append((String)dataInfo.get("SB11"));   //Seqno(tracking code)
                    break;
               case F0204:  
               case F0206:
                    sendBuf.append((String)dataInfo.get("SB01"));   //가맹점 아이디
                    sendBuf.append((String)dataInfo.get("SB02"));   //PG 일련번호
                    sendBuf.append((String)dataInfo.get("SB03"));   //통화코드
                    sendBuf.append((String)dataInfo.get("SB04"));   //거래금액
                    sendBuf.append((String)dataInfo.get("SB05"));   //봉사료
                    sendBuf.append((String)dataInfo.get("SB06"));   //세금
                    sendBuf.append((String)dataInfo.get("SB07"));   //할부기간
                    sendBuf.append((String)dataInfo.get("SB08"));   //취소금액
                    sendBuf.append((String)dataInfo.get("SB09"));   //승인번호
                    sendBuf.append((String)dataInfo.get("SB10"));   //거래일시
                    sendBuf.append((String)dataInfo.get("SB11"));   //Seqno(tracking code)
                    break;     

            }

			byte[] appBody = sendBuf.toString().getBytes();
			byte[] appHeader = null;

			appHeader = makeHeader(AppUtil.checkNull(dataInfo.get("RH04")), appBody.length);

			//전체전문 구성
            int idx = 0;
			int tLen = appHeader.length + appBody.length;
			sendBytes = new byte[tLen];
            System.arraycopy(appHeader, 0, sendBytes, idx, appHeader.length); idx += appHeader.length;
			System.arraycopy(appBody, 0, sendBytes, idx, appBody.length); idx += appBody.length;

        }catch(Exception e){
			// throw new BusinessException("[VerData::makeData]", BusinessException.SY04, "Exception:"+e, BusinessException.sysErrMsg);
		}

		return sendBytes;
	}

	private int getFormat(String code){
            if(code.equals("0100")) return F0100;
            else if(code.equals("0101")) return F0101;
            else if(code.equals("0102")) return F0102;
            else if(code.equals("0120")) return F0120;
            else if(code.equals("0121")) return F0121;
            else if(code.equals("0122")) return F0122;// 2021.12.22 unionpay authorize 추가 walter            
            else if(code.equals("0125")) return F0125;
            else if(code.equals("0126")) return F0126;
            else if(code.equals("0140")) return F0140;
            else if(code.equals("0160")) return F0160;
            else if(code.equals("0180")) return F0180;
            else if(code.equals("0181")) return F0181;
            else if(code.equals("0182")) return F0182;
            else if(code.equals("0183")) return F0183;
            else if(code.equals("0184")) return F0184;
            else if(code.equals("0200")) return F0200;
            else if(code.equals("0201")) return F0201;
            else if(code.equals("0202")) return F0202;
            else if(code.equals("0204")) return F0204;
            else if(code.equals("0206")) return F0206;
            else if(code.equals("0400")) return F0400;
            else if(code.equals("9999")) return F9999;
            else return -1;
	}


}
