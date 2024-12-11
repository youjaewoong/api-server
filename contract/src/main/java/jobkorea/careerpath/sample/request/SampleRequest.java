package jobkorea.careerpath.sample.request;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import jobkorea.careerpath.common.model.SearchCriteria;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "샘플 요청")
public class SampleRequest extends SearchCriteria {

	public SampleRequest(String id) {
		this.id = id;
	}

	@Schema(description = "고유ID", example = "")
	@Pattern(regexp = "^[0-9]*$", message = "숫자만 입력해주세요.")
	private String id;

	@Schema(description = "제목", example = "")
	@Size(min = 0, max = 20)
	private String title;

}
