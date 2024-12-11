package jobkorea.careerpath.common.config.mybatis;

import org.mybatis.spring.SqlSessionFactoryBean;

public interface SqlSessionFactoryBeanProvider {
	SqlSessionFactoryBean create();
}
