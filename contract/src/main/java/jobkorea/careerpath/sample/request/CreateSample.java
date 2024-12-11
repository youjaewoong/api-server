package jobkorea.careerpath.sample.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import jobkorea.careerpath.sample.enums.SampleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "샘플 등록 요청")
public class CreateSample {

	@NotBlank(message = "사용자명은 필수입니다.")
	@Size(min = 0, max = 10)
	@Schema(description = "사용자", example = "유관순")
	private String name;

	@NotBlank(message = "제목은 필수입니다.")
	@Size(min = 0, max = 20)
	@Schema(description = "제목", example = "제목 입니다.")
	private String title;

	@NotBlank(message = "내용은 필수입니다.")
	@Size(min = 0, max = 400)
	@Schema(description = "내용", example = "내용 입니다.")
	private String contents;

	@Schema(description = "타입", example = "APPLY")
	private SampleType type;
}
