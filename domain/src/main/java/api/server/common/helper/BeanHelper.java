package api.server.common.helper;

import api.server.common.provider.ApplicationContextProvider;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;

@UtilityClass
public class BeanHelper {
    public static Object getBean(String beanName) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        return applicationContext.getBean(beanName);
    }
}