package api.server.sample.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import api.server.common.constant.DateTimeConstant;
import api.server.sample.enums.SampleType;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "샘플 응답")
public class SampleResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "고유 ID", example = "1")
	private int id;

	@Schema(description = "사용자", example = "사용자 입니다.")
	private String name;

	@Schema(description = "제목", example = "제목 입니다.")
	private String title;

	@Schema(description = "내용", example = "내용 입니다.")
	private String contents;

	@Schema(description = "타입", example = "APPLY")
	private SampleType type;

	@Schema(description = "작성일", example = "2022-07-20 12:00:11")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstant.DEFAULT_PATTERN, timezone = DateTimeConstant.DEFAULT_ZONE)
	private LocalDateTime regDt;

	@Schema(description = "수정일", example = "2022-07-20 13:00:22")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstant.DEFAULT_PATTERN, timezone = DateTimeConstant.DEFAULT_ZONE)

	private LocalDateTime editDt;
}
