package jobkorea.careerpath.common.config.mybatis;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mybatis.runtime-reload.enabled", havingValue = "false", matchIfMissing = true)
public class DefaultSqlSessionFactoryBeanProvider implements SqlSessionFactoryBeanProvider {
	@Override
	public SqlSessionFactoryBean create() {
		return new SqlSessionFactoryBean();
	}
}
