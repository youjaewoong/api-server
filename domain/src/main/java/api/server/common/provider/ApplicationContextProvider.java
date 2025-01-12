package api.server.common.provider;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * ApplicationContextProvider 클래스는 Spring ApplicationContext를 정적 필드로 저장하여,
 * 애플리케이션 어디에서든 Spring Bean을 가져올 수 있도록 도와줍니다.
 *
 * 주요 기능:
 * - Spring의 ApplicationContextAware를 구현하여 ApplicationContext를 정적 변수로 저장.
 * - 전역적으로 ApplicationContext에 접근 가능.
 *
 * 사용 사례:
 * - Bean 객체를 주입받지 않고, 특정 클래스에서 동적으로 Bean을 가져와야 하는 경우.
 */
@Component
@Getter
public class ApplicationContextProvider implements ApplicationContextAware{

    /**
     * 애플리케이션의 전역 ApplicationContext 인스턴스.
     */
    @Getter
    private static ApplicationContext applicationContext;


    /**
     * ApplicationContext를 설정합니다.
     * Spring이 애플리케이션 컨텍스트 초기화 시 자동으로 호출합니다.
     *
     * @param ctx ApplicationContext 인스턴스
     * @throws BeansException ApplicationContext 설정 중 예외 발생 시
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

}