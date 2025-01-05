package api.server.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FixedLength {
    int length();          // 필드 길이
    int offset() default 0; // 필드의 시작 위치
    boolean required() default false; // 필수 여부
}