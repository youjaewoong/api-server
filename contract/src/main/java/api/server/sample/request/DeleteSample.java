package api.server.sample.request;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import api.server.sample.validator.SampleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "샘플 삭제 요청")
@SampleCode
public class DeleteSample {

	/**
	 * List 빈값 허용x
	 * 빈값,공백 허용x
	 * 숫자만 허용
	 */
	@NotEmpty
	@Schema(description = "게시글 번호", example = "[\"1\", \"에러\"]")
	private List<
			@NotBlank(message = "게시글 번호는 필수입니다.")
			@Pattern(regexp = "^[0-9]*$", message = "숫자만 입력해주세요.") String> ids;
}
