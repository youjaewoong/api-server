
package api.server.payment.my;

public class MyException extends Exception{
    public static final String SY00     = "SY00";   //최대 쓰레드 수 초과
    public static final String SY01     = "SY01";   //전문버전별 Worker클래스 로딩 실패
    public static final String SY02     = "SY02";   //수신데이터 파싱 실패
    public static final String SY03     = "SY03";   //전문별 처리 실패
    public static final String SY04     = "SY04";   //응답데이터 구성 실패
    public static final String SY05     = "SY05";   //전문 전체 길이 오류
    public static final String SY06     = "SY06";   //전문 본문 길이 오류
    public static final String SY20     = "SY20";   //VAN 연결 실패
    public static final String SY21     = "SY21";   //VAN 요청전문 구성 실패
    public static final String SY22     = "SY22";   //VAN 요청전문 송신 실패
    public static final String SY23     = "SY23";   //VAN 응답전문 수신 실패
    public static final String SY24     = "SY24";   //VAN 응답전문 분석 실패
    public static final String SY25     = "SY25";   //VAN 연결갯수 초과

    public static final String sysErrMsg = "System Error!";
        private String traceno;
	private String code;
	private String loc;
	private String dispMsg;

	public MyException() {
		super();
	}

	public MyException(String s) {
		super(s);
	}

	public MyException(int code, String s){
		super(s);
		this.code = String.valueOf(code);
	}

	public MyException(String loc, String s){
		super(s);
		this.loc = loc;
	}

	public MyException(String loc, String code, String s){
		super(s);
		this.code = code;
		this.loc = loc;
		this.dispMsg = s;
	}

	public MyException(String loc, String code, String errMsg, String dispMsg){
		super(errMsg);
		this.code = code;
		this.loc = loc;
		this.dispMsg = dispMsg;
	}
        public MyException(String loc, String code, String errMsg, String dispMsg, String traceno){
		super(errMsg);
		this.code = code;
		this.loc = loc;
		this.dispMsg = dispMsg;
                this.traceno = traceno;
	}

	public String getLoc(){
		return loc;
	}

	public String getCode(){
		if(code.equals("")) return "XXXX";
		return code;
	}

    public void setDispMsg(String value){
        dispMsg = value;
    }
    
   public String getDispMsg(){
		return dispMsg;
	}
    public void setTraceno(String traceno){
        traceno = traceno;
    }

    public String getTraceno(){
            return traceno;
    }
    public String getErrMsg(String errCode){
        if(errCode.equals(SY00)){
            return "최대 쓰레드 수 초과";
        }else if(errCode.equals(SY01)){
            return "전문버전별 Worker클래스 로딩 실패";
        }else if(errCode.equals(SY02)){
            return "수신데이터 파싱 실패";
        }else if(errCode.equals(SY03)){
            return "전문별 처리 실패";
        }else return "";
    }
}
	 