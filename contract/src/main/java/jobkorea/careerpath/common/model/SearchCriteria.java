package jobkorea.careerpath.common.model;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class SearchCriteria {

	@Min(1)
	@Schema(description = "페이지", example = "1")
	@Builder.Default
	private int pageIndex = 1;

	@Min(-1)
	@Max(10000)
	@Schema(description = "사이즈", example = "10")
	@Builder.Default
	private int pageRowSize = 10;

}
