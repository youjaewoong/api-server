package api.server.common.config.mybatis;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mybatis.runtime-reload.enabled", havingValue = "true")
class RefreshableSqlSessionFactoryBeanProvider implements SqlSessionFactoryBeanProvider {
	public SqlSessionFactoryBean create() {
		var result = new RefreshableSqlSessionFactoryBean();
		result.setInterval(1000);
		return result;
	}
}
