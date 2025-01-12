package api.server.common.helper;

import api.server.common.provider.ApplicationContextProvider;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;


/**
 * BeanHelper 클래스는 Spring ApplicationContext에서 Bean을 동적으로 가져오는 유틸리티를 제공합니다.
 *
 * 주요 기능:
 * - Bean 이름으로 ApplicationContext에서 Bean 인스턴스를 가져옵니다.
 *
 * 사용 사례:
 * - 특정 Bean을 주입받지 않고 동적으로 가져와야 할 때 사용.
 */
@UtilityClass
public class BeanHelper {

    /**
     * Spring ApplicationContext에서 주어진 이름의 Bean을 반환합니다.
     *
     * @param beanName 가져올 Bean의 이름
     * @return 해당 이름의 Bean 객체
     * @throws BeansException Bean을 찾을 수 없거나 가져올 수 없는 경우 예외 발생
     */
    public static Object getBean(String beanName) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        return applicationContext.getBean(beanName);
    }
}