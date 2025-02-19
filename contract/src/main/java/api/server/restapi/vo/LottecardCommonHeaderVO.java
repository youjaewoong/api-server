package api.server.restapi.vo;

import api.server.common.constant.GramInfoConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LottecardCommonHeaderVO {

    /**
     * 전문길이
     * <pre>
     * 전문길이 CHAR 5 Bytes(1번항목~Data끝 Bytes)
     * - 본항목도 길이에 포함됨.
     *
     * 영문명: GRAM_LNTH
     * 타입: N
     * 길이: 5
     * 오프셋: 0
     * </pre>
     */
    @Schema(hidden = true)
    private String gramLnth;


    /**
     * 거래번호
     * <pre>
     * Global Unique ID
     * - UI : 시스템일시(14)+ "UIF"(3)+사원번호(7)+일련번호(7)
     * - FW : 시스템일자(8)+시스템명(9)+전문생성일시(9)+일련번호(5)
     * - FEP : 시스템일자(8)+시스템명(9)+전문생성일시(9)+일련번호(5)
     * - ARS : 시스템일자(8)+시스템명(9)+전문생성일시(9)+"ARSID"
     * - VOT : 시스템일자(8)+시스템명(9)+전문생성일시(9)+"VOTID"
     *
     * 영문명: GUID
     * 타입: C
     * 길이: 31
     * 오프셋: 5
     * </pre>
     */
    @Schema(hidden = true)
    private String guid ;


    /**
     * 전문진행번호
     * <pre>
     * 전문처리후 타 시스템 전송시 1씩 증가 (최초 생성시 00)
     *
     * 영문명: GRAM_PRG_NO
     * 타입: N<
     * 길이: 2
     * 오프셋: 36
     * </pre>
     */
    @Schema(hidden = true)
    private String gramPrgNo = "00";


    /**
     * 전문번호
     * <pre>
     * 업무구분코드(2) + 인터페이스Type(1)+TRAN_ID(5)
     * - 업무구분코드 : 요청(주관)하는 업무파트의 업무구분코드
     * - 인터페이스 유형 구분코드
     *    [Online]  S : Sync, A : Async, D : Deferred,
     *                R : Deferred-Response, N : Notify
     *    [Batch]   B : Batch   M : NDM
     *    [DB동기화] C : CDC
     * - TRAN_ID(5) :
     *    [업무계] 전문관리시스템에서 등록시 자동 채번
     *    [승인계] 첫 한자리는 'A' 로 고정,뒤 4자리를 AS-IS 처럼 특정 번호체계로 사용
     *
     * 영문명: GRAM_NO
     * 타입: C
     * 길이: 8
     * 오프셋: 38
     * </pre>
     */
    @Schema(hidden = true)
    private String gramNo;


    /**
     * 요청응답구분
     * <pre>
     * S : 요청, R : 응답
     * 영문명: AK_RSP_DC
     * 타입: C
     * 길이: 1
     * 오프셋: 46
     * </pre>
     */
    @Schema(hidden = true)
    private String akRspDc = GramInfoConstant.SEND;


    /**
     * TARGET 업무구분 코드
     * <pre>
     * 수신측 업무구분코드(2)
     *
     * 영문명: RSP_BIZ_DC
     * 타입: C
     * 길이: 2
     * 오프셋: 47
     * </pre>
     */
    @Schema(hidden = true)
    private String rspBizDc;


    /**
     * 전문요청일시
     * <pre>
     * 최초요청측의 요청일시(yyyyMMddHHmmssSSS)
     *
     * 영문명: GRAM_AK_DTTI
     * 타입: C
     * 길이: 17
     * 오프셋: 49
     * </pre>
     */
    @Schema(hidden = true)
    private String gramAkDtti;


    /**
     * IP주소
     * <pre>
     * 최초 요청측 IP 주소
     *
     * 영문명: IP_ADD
     * 타입: C
     * 길이: 16
     * 오프셋: 66
     * </pre>
     */
    @Schema(hidden = true)
    private String ipAdd;


    /**
     * TARGET 서비스 ID
     * <pre>
     * 시스템의 서비스 ID
     * F/W에서 어떤 클래스의 메소드를 수행할 지 판단
     *
     * 영문명: TGT_SV_ID
     * 타입: C
     * 길이: 12
     * 오프셋: 82
     * </pre>
     */
    @Schema(hidden = true)
    private String tgtSvId;


    /**
     * 거래고유번호1
     * <pre>
     * 결번처리용. 거래고유번호1만 있으면 해당 번호만 결번 의미
     * 거래고유번호2와 함께 사용되면 From-Sequenece 번호 의미 (승인계에서 사용)
     *
     * 영문명: DE_NATV_NO1
     * 타입: C
     * 길이: 9
     * 오프셋: 94
     * </pre>
     */
    @Schema(hidden = true)
    private String deNatvNo1;


    /**
     * 거래고유번호2
     * <pre>
     * - 결번처리 범위용 : To-Sequence 번호 의미
     * - 통신망관리전문일 경우 :
     *   NETWORK-CODE "301"+공란(6) : TEST CALL
     *                "062"+공란(6) : 장애통보
     *                "063"+공란(6) : 복구통보
     * - 승인계에서 사용
     *
     * 영문명: DE_NATV_NO2
     * 타입: C
     * 길이: 9
     * 오프셋: 103
     * </pre>
     */
    @Schema(hidden = true)
    private String deNatvNo2;


    /**
     * 원거래 보장내용
     * <pre>
     * 승인계 원거래 보장용(승인계에서 사용)
     *
     * 영문명: ORI_GRN_CN
     * 타입: C
     * 길이: 20
     * 오프셋: 112
     * </pre>
     */
    @Schema(hidden = true)
    private String oriGrnCn;


    /**
     * 시뮬레이션 거래여부
     * <pre>
     * 시뮬레이션 거래일 경우 처리되었을 경우의 응답전문이 전송되나
     * 실제 시스템에서는 rollback 처리한다.
     *
     * 영문명: SML_DE_YN
     * 타입: C
     * 길이: 1
     * 오프셋: 132
     * </pre>
     */
    @Schema(hidden = true)
    private String smlDeYn = GramInfoConstant.SML_DE_N;


    /**
     * 전문응답일시
     * <pre>
     * 응답측의 응답일시(yyyyMMddHHmmssSSS)
     *
     * 영문명: GRAM_RSP_DTTI
     * 타입: C
     * 길이: 17
     * 오프셋: 133
     * </pre>
     */
    @Schema(hidden = true)
    private String gramRspDtti;


    /**
     * 처리결과코드
     * <pre>
     * 거래처리 결과
     *  - 0 : 정상
     *  - 1 : 오류
     *  - 8 : 단말에서 비정상 전문 수신시, 개발자에게 리턴하기 위한 코드값
     *  - 9 : 트랜젝션은 정상처리되었으나, 이외에서 오류 발생한 경우
     *
     * 영문명: PC_RC
     * 타입: C
     * 길이: 1
     * 오프셋: 150
     * </pre>
     */
    @Schema(hidden = true)
    private String pcRc ;


    /**
     * 메시지응답코드
     * <pre>
     * 정상, 오류 응답코드
     * 메시지유형(1)+메시지구분(1)+업무구분코드(2)+일련번호(4)
     *  - 메시지유형(1)
     *     B: 비즈니스
     *     S: 시스템
     *     D: 화면
     *  - 메시지구분(1)
     *     N : Normal
     *     I  : Information
     *     W : Warning
     *     E : Error
     *
     * 영문명: FST_ERR_SYS_DC
     * 타입: C
     * 길이: 8
     * 오프셋: 151
     * </pre>
     */
    @Schema(hidden = true)
    private String fstErrSysDc;


    /**
     * 채널헤더 존재여부
     * <pre>
     * 공통부헤더 이후에 채널헤더부가 오는지 여부
     * 대외 연계 등과 같은 시스템간의 통신 시에는 채널헤더가 없고,
     * 단말, 인터넷, ARS등과 같은 사용자 조작 전문의 경우 채널헤더부가 존재한다.
     *
     * 영문명: CHNL_HD_EXS_YN
     * 타입: C
     * 길이: 1
     * 오프셋: 159
     * </pre>
     */
    @Schema(hidden = true)
    private String chnlHdExsYn;


    /**
     * 여분필드
     * <pre>
     * 시스템공통부 여백
     * - 194~200자리(7) : 송수신조회일련번호로 사용
     *
     * 영문명: EXR_FLD
     * 타입: C
     * 길이: 40
     * 오프셋: 160
     * </pre>
     */
    @Schema(hidden = true)
    private String exrFld;
}
