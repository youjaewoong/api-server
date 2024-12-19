package api.server.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

@NoArgsConstructor
@Getter
public class ErrorResponse {

    private final String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // 사용자 정의 형식

    /** 사용자 정의 메시지 */
    private String message;

    /** 에러 메시지 */
    private String errorMessage;

    /** 사용자 정의 코드 */
    private String code;

    /** 상태값 400, 500 등.. */
    private int status;

    /** &#064;Validated  Parameter error 발생한 필드 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("errors")
    private List<CustomFieldError> customFieldErrors = new ArrayList<>();

    static public ErrorResponse create() {
        return new ErrorResponse();
    }

    public ErrorResponse code(String code) {
        this.code = code;
        return this;
    }

    public ErrorResponse status(int status) {
        this.status = status;
        return this;
    }

    public ErrorResponse message(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponse errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ErrorResponse errors(Errors errors) {
        setCustomFieldErrors(errors.getFieldErrors());
        return this;
    }

    /**
     * BindingResult.getFieldErrors() 메소드를 통해 전달받은 fieldErrors
     */
    public void setCustomFieldErrors(List<FieldError> fieldErrors) {
        fieldErrors.forEach(error -> {
            customFieldErrors.add(new CustomFieldError(
                    Objects.requireNonNull(error.getCodes())[0],
                    error.getRejectedValue(),
                    error.getDefaultMessage()
            ));
        });
    }

    /**
     * parameter 검증에 통과하지 못한 객체 정보
     */
    @Getter
    @AllArgsConstructor
    public static class CustomFieldError {
        private String field;
        private Object value;
        private String reason;
    }
}