package api.server.common.message;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        // 메시지 파일 경로 설정
        messageSource.setBasenames("i18n/messages", "i18n/errors");

        // 메시지 파일 인코딩 설정
        messageSource.setDefaultEncoding("UTF-8");

        // 시스템 로케일로 대체 여부 설정
        messageSource.setFallbackToSystemLocale(false);

        // 캐시 유지 시 설정 (초 단위)
        messageSource.setCacheSeconds(-1); // -1 비활성화

        // 메시지 찾기 실패 시 경고 로그 남기기
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }
}