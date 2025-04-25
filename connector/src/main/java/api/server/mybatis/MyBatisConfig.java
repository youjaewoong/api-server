package api.server.mybatis;

import api.server.mybatis.type.DefaultEnumTypeHandler;
import api.server.mybatis.type.YesNoBooleanTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "db.active", havingValue = "true", matchIfMissing = false)
public class MyBatisConfig {

    /**
     * DataSource 설정
     * 참고: 실제 환경에서는 application.properties/yml에서 설정값 주입 권장
     */
    @Bean
    public DataSource dataSource() {
        // 설정 주입 (예시)
        // dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // dataSource.setUrl("jdbc:mysql://localhost:3306/mydb");
        // dataSource.setUsername("username");
        // dataSource.setPassword("password");
        return new DriverManagerDataSource();
    }

    /**
     * MyBatis SqlSessionFactory 설정
     *
     * @param dataSource Spring에서 관리하는 데이터소스 주입
     * @return SqlSessionFactory
     * @throws Exception SqlSessionFactoryBean 초기화 실패 시 발생
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

        // Spring의 DataSource를 MyBatis에 전달
        sqlSessionFactoryBean.setDataSource(dataSource);

        // MyBatis 설정 객체 생성
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        // Enum 타입 처리 핸들러 설정
        configuration.setDefaultEnumTypeHandler(DefaultEnumTypeHandler.class);

        // 데이터베이스 필드와 Java 객체 필드 간 underscore_case => camelCase 매핑 활성화
        configuration.setMapUnderscoreToCamelCase(true);

        // YesNoBooleanTypeHandler 등록
        configuration.getTypeHandlerRegistry().register(Boolean.class, YesNoBooleanTypeHandler.class);

        sqlSessionFactoryBean.setConfiguration(configuration);

        // MyBatis Mapper XML 파일 경로 설정
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:sql/**/*.xml"));

        return sqlSessionFactoryBean.getObject();
    }
}