package api.server.payment.constant;

public class PaymentConstant {

    public static final int F0100 = 1;      //신용카드 승인
    public static final int F0101 = 2;      //신용카드 승인(PCINS)
    public static final int F0102 = 3;      //BC UPOT 신용카드 승인 (자체 저장)
    public static final int F0120 = 4;      //신용카드 인증(수동매입)
    public static final int F0121 = 5;      //신용카드 인증(PCINS, Card verification)
    public static final int F0125 = 6;      //Capture
    public static final int F0126 = 7;      //Capture(PCINS)
    public static final int F0140 = 8;      //전화승인
    public static final int F0160 = 9;      //DCC 환율조회
    public static final int F0180 = 10;      //DCC 신용승인
    public static final int F0181 = 11;      //MCP 신용승인
    public static final int F0182 = 12;      //MCP 신용승인(PCINS)
    public static final int F0183 = 13;      //MCP 인증요청(PCINS)
    public static final int F0184 = 14;      //MCP 인증요청
    public static final int F0200 = 15;      //신용카드 취소
    public static final int F0201 = 16;      //신용카드 취소(PCINS)
    public static final int F0202 = 17;      //BC UPOT 신용카드 취소(자체 저장)
    public static final int F0400 = 18;      //일반환급요청
    public static final int F0204 = 19;      //MCP부분취소
    public static final int F0206 = 20;      //UPOP부분취소
    public static final int F0122 = 21;      //BC UPOT 신용카드 승인 (자체 저장)-수동매입

    //Edgar 부하테스트용 추가 (2023.03.15)
    public static final int F9999 = 9999;

}
