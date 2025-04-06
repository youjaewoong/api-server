/*
 * Created on 2004. 9. 10.
 *
 */
package api.server.payment.gateway;


import api.server.common.helper.AppUtil;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public abstract class AppData extends AbstractData{
    public static final int F0100   =   1;      //신용카드 승인
    public static final int F0101   =   2;      //신용카드 승인(PCINS)
    public static final int F0102   =   3;      //BC UPOT 신용카드 승인 (자체 저장)
    public static final int F0120   =   4;      //신용카드 인증(수동매입)
    public static final int F0121   =   5;      //신용카드 인증(PCINS, Card verification)
    public static final int F0125   =   6;      //Capture
    public static final int F0126   =   7;      //Capture(PCINS)
    public static final int F0140   =   8;      //전화승인
    public static final int F0160   =   9;      //DCC 환율조회
    public static final int F0180   =  10 ;      //DCC 신용승인
    public static final int F0181   =  11;      //MCP 신용승인
    public static final int F0182   =  12;      //MCP 신용승인(PCINS)
    public static final int F0183   =  13;      //MCP 인증요청(PCINS)
    public static final int F0184   =  14;      //MCP 인증요청
    public static final int F0200   =  15;      //신용카드 취소
    public static final int F0201   =  16;      //신용카드 취소(PCINS)
    public static final int F0202   =  17;      //BC UPOT 신용카드 취소(자체 저장)
    public static final int F0400   =  18;      //일반환급요청
    public static final int F0204   =  19;      //MCP부분취소
    public static final int F0206   =  20;      //UPOP부분취소
    public static final int F0122   =  21;      //BC UPOT 신용카드 승인 (자체 저장)-수동매입
    
    //Edgar 부하테스트용 추가 (2023.03.15) 
    public static final int F9999   = 9999;

    protected final int HEADERLEN = 30;

	//에러 송신 전문 구성
	protected byte[] makeErrData(String errCode, String errMsg, String traceno){
            
                String bodymsg = AppUtil.formatData(traceno, AppUtil.LEFT, 12, ' ') + errMsg;
               
		byte[] appBody = bodymsg.getBytes();
                
                //byte[] appBody = errMsg.getBytes();
System.out.println("[AppData::makeErrData]"+errMsg);
		//전문헤더
		byte[] appHeader = makeHeader(errCode, appBody.length);

		//전체전문 구성
        int idx = 0;
		int tLen = appHeader.length + appBody.length;
		byte[] sendBytes = new byte[tLen];
        System.arraycopy(appHeader, 0, sendBytes, idx, appHeader.length); idx += appHeader.length;
		System.arraycopy(appBody, 0, sendBytes, idx, appBody.length); idx += appBody.length;

		return sendBytes;
	}

	protected byte[] makeHeader(String resCode, int bodyLen){
		int nIdx = 0;
		byte[] bHeader = new byte[HEADERLEN+4];

		//헤더내용
		StringBuffer headBuf = new StringBuffer();
                headBuf.append(AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RH01")), AppUtil.LEFT, 4, ' '));
		headBuf.append(AppUtil.formatData(AppUtil.checkNull(dataInfo.get("RH02")), AppUtil.LEFT, 4, ' '));
                headBuf.append(getResMsgType(AppUtil.checkNull(dataInfo.get("RH03"))));
		headBuf.append(AppUtil.formatData(resCode, AppUtil.LEFT, 4, ' '));
                headBuf.append(AppUtil.getDateTime());

		byte[] bTmp = headBuf.toString().getBytes();

		//전체길이
		int nTot = HEADERLEN + bodyLen;
		byte[] bTot = AppUtil.formatData(String.valueOf(nTot), AppUtil.RIGHT, 4, '0').getBytes();

        System.arraycopy(bTot, 0, bHeader, nIdx, 4); nIdx += 4;
		System.arraycopy(bTmp, 0, bHeader, nIdx, bTmp.length); nIdx += bTmp.length;

		return bHeader;
	}

    private String getResMsgType(String msgType){
        if(msgType.equals("0100")) return "0110";
        else if(msgType.equals("0101")) return "0111";
        else if(msgType.equals("0102")) return "0112";
        else if(msgType.equals("0120")) return "0130";
        else if(msgType.equals("0121")) return "0131";
        else if(msgType.equals("0125")) return "0135";
        else if(msgType.equals("0126")) return "0136";
        else if(msgType.equals("0140")) return "0150";
        else if(msgType.equals("0160")) return "0170";
        else if(msgType.equals("0180")) return "0190";
        else if(msgType.equals("0181")) return "0191";
        else if(msgType.equals("0182")) return "0192";
        else if(msgType.equals("0183")) return "0193";
        else if(msgType.equals("0184")) return "0194";
        else if(msgType.equals("0200")) return "0210";
        else if(msgType.equals("0201")) return "0211";
        else if(msgType.equals("0202")) return "0212";
        else if(msgType.equals("0204")) return "0214";
        else if(msgType.equals("0206")) return "0216";
        else if(msgType.equals("0400")) return "0410";
        else if(msgType.equals("0122")) return "0132";
        else if(msgType.equals("9999")) return "9999";  //Edgar 부하테스트용 (2023.03.15)
        
        return "    ";
    }
}
