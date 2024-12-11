package jobkorea.careerpath.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jobkorea.careerpath.common.constant.DateTimeConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CommonResponse {

    @Schema(description = "작성일", example = "2022-07-20 12:00:11")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstant.DEFAULT_PATTERN, timezone = DateTimeConstant.DEFAULT_ZONE)
    private LocalDateTime createDate;

    @Schema(description = "수정일", example = "2022-07-20 13:00:22")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConstant.DEFAULT_PATTERN, timezone = DateTimeConstant.DEFAULT_ZONE)
    private LocalDateTime updateDate;
}
