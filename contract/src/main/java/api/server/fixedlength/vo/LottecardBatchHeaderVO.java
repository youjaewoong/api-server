package api.server.fixedlength.vo;

import api.server.common.annotation.FixedLength;
import api.server.fixedlength.header.HeaderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 롯데카드 해더 배치 영역
 * <pre>
 * 공통해더 + 배치 영역
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LottecardBatchHeaderVO extends LottecardCommonHeaderVO implements HeaderType {

    /**
     * 명령 구분 (Operation)
     * <pre>
     * S: 파일 전송
     * L: 목록 조회 (현재 사용 안 함)
     * M: NDM Scripts 호출
     * C: 기타
     * D: 삭제
     * </pre>
     */
    @FixedLength(length = 1, offset = 200)
    private String operation;

    /**
     * 파일 전송 완료 대기 여부 (ReplyYN)
     * <pre>
     * Y: 파일 전송 완료 후 응답 리턴 (EAI 재처리 안 함)
     * N: 전송 요청만 수신, 처리 여부 무관
     * </pre>
     */
    @FixedLength(length = 1, offset = 201)
    private String replyYN;

    /**
     * 전송 파일 갯수 (FileCount)
     * <pre>
     * 파일 Get/Put 값이 'GET' 일 경우 항상 0으로 설정.
     * </pre>
     */
    @FixedLength(length = 3, offset = 202)
    private int fileCount;

    /**
     * 파일 크기 (FileSize)
     * <pre>
     * 파일 Get/Put 값이 'GET' 일 경우 항상 0으로 설정.
     * </pre>
     */
    @FixedLength(length = 14, offset = 205)
    private long fileSize;

    /**
     * 송신 파일명 (SourceFileName)
     * <pre>
     * 파일 Get/Put 값이 'PUT' 일 경우 생성된 파일명.
     * 파일 Get/Put 값이 'GET' 일 경우 타겟 시스템의 파일명.
     * </pre>
     */
    @FixedLength(length = 200, offset = 219)
    private String sourceFileName;

    /**
     * SFTP 압축 여부 (Compress)
     * <pre>
     * 항상 'N'만 가능.
     * </pre>
     */
    @FixedLength(length = 1, offset = 419)
    private String compress;

    /**
     * NDM Mode (NDMMode)
     * <pre>
     * NDM 동작 모드 정보.
     * </pre>
     */
    @FixedLength(length = 10, offset = 420)
    private String ndmMode;

    /**
     * 수신 파일명 (TargetFileName)
     * <pre>
     * 파일 Get/Put 값이 'PUT'일 경우 타겟 시스템에 전송할 파일명.
     * 파일 Get/Put 값이 'GET'일 경우 소스 시스템의 파일명.
     * </pre>
     */
    @FixedLength(length = 200, offset = 430)
    private String targetFileName;

    /**
     * 재처리 횟수 (RetryCount)
     * <pre>
     * 10분 간격으로 설정된 재시도 횟수.
     * Default: 5
     * </pre>
     */
    @FixedLength(length = 2, offset = 630)
    private int retryCount;

    /**
     * 파일 Get/Put 구분 (GetPut)
     * <pre>
     * EAI 로 전송 요청을 한 시스템 기준으로 파일을 타겟 시스템에서
     * 가져 와야하는 경우 GET, 타겟 시스템으로 보내야 할 경우 PUT
     * </pre>
     */
    @FixedLength(length = 3, offset = 632)
    private String getPut;

    /**
     * 체크 파일 확장자 (CheckFileExtension)
     * <pre>
     * 파일 Get/Put 값이 'GET' 일 경우 파일 생성 완료 여부를 알기
     * 위한 Check 파일의 확장자
     * 파일 Get/Put 값이 'GET'일 경우 파일을 다건 가져올 경우 파일을 생성 한 측에서 '대표파일명.FIN'을 생성
     * </pre>
     */
    @FixedLength(length = 5, offset = 635)
    private String checkFileExtension;

}
