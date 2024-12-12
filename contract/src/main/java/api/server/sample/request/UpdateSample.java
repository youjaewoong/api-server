package api.server.sample.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import api.server.sample.enums.SampleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "샘플 수정 요청")
public class UpdateSample {

	@Min(0)
	@Pattern(regexp = "^[0-9]*$", message = "숫자만 입력해주세요.")
	@Schema(description = "고유ID", example = "1")
	private String id;

	@NotBlank(message = "사용자명은 필수입니다.")
	@Size(min = 0, max = 10)
	@Schema(description = "사용자", example = "유관순")
	private String name;

	@NotBlank(message = "제목은 필수입니다.")
	@Size(min = 0, max = 20)
	@Schema(description = "제목", example = "변경된 제목 입니다.")
	private String title;

	@NotBlank(message = "내용은 필수입니다.")
	@Size(min = 0, max = 400)
	@Schema(description = "내용", example = "변경된 내용 입니다.")
	private String contents;

	@Schema(description = "타입", example = "APPROVAL")
	private SampleType type;
}
