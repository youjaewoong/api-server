package jobkorea.careerpath.common.config;

import javax.sql.DataSource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jobkorea.careerpath.common.config.mybatis.SqlSessionFactoryBeanProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@MapperScan(
		basePackages = {"jobkorea"},
		annotationClass = Mapper.class,
		sqlSessionFactoryRef = "jkdbSqlSessionFactory"
)
public class DatabaseConfig {

	/*
	 [Hikari Connection Pool Configuration]
	 */
	@Bean("jkdbHikariConfig")
	@ConfigurationProperties(prefix = "spring.datasource-jkdb.hikari")
	public HikariConfig jkdbHikariConfig() {
		return new HikariConfig();
	}

	/*
	 [DataSource Configuration]
	 */
	@Bean("jkdbDataSource")
	public DataSource jkdbDataSource(@Qualifier("jkdbHikariConfig") HikariConfig hikariConfig) {
		return new HikariDataSource(hikariConfig);
	}

	/*
	 [MyBatis SQL Mapping XML Location pattern]
	 */
	@Value("${spring.datasource-jkdb.mybatis.mapper-locations}")
	private String jkdbMyBatisMapperLocation;

	/*
	 [MyBatis Config Location pattern]
	 */
	@Value("${spring.datasource-jkdb.mybatis.config-location}")
	private String jkdbMyBatisConfigLocation;

	/*
	 [MyBatis SQL Session Factory Configuration]
	 */
	@Bean("jkdbSqlSessionFactory")
	public SqlSessionFactory jkdbSqlSessionFactory(
			SqlSessionFactoryBeanProvider factoryBeanProvider,
			@Qualifier("jkdbDataSource") DataSource dataSource,
			ApplicationContext applicationContext) throws Exception {
		return createSqlSessionFactory(factoryBeanProvider, dataSource, applicationContext, jkdbMyBatisMapperLocation, jkdbMyBatisConfigLocation);
	}

	/**
	 * SqlSessionFactory 를 생성하여 반환합니다.
	 *
	 * @param factoryBeanProvider SQL Session Factory Bean Provider
	 * @param dataSource javax.sql.DataSource
	 * @param applicationContext org.springframework.context.ApplicationContext
	 * @param resourceLocation location of mybatis mapping xml file
	 * @return SQL Session Factory
	 * @throws Exception Factory Bean Exception
	 */
	private SqlSessionFactory createSqlSessionFactory(SqlSessionFactoryBeanProvider factoryBeanProvider,
													  DataSource dataSource,
													  ApplicationContext applicationContext,
													  String resourceLocation,
													  String configLocation) throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = factoryBeanProvider.create();
		sqlSessionFactoryBean.setDataSource(dataSource);
		var resources = applicationContext.getResources(resourceLocation);
		log.debug("[DB-CONFIG] {} = {} ", resourceLocation, resources);
		sqlSessionFactoryBean.setMapperLocations(resources);
		sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource(configLocation));
		return sqlSessionFactoryBean.getObject();

	}

	/*
	 [Spring Transaction Manager Configuration]
	 */
	@Primary
	@Bean("jkdbTransactionManager")
	public DataSourceTransactionManager jkdbTransactionManager(@Qualifier("jkdbDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

}//end:class
