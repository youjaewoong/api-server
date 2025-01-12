package api.server.common.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    @Value("${db.active:false}")
    private boolean dbActive;

    private final StringEncryptor jasyptStringEncryptor;

    /**
     * 데이터소스 비활성화 시 동작
     *
     * @return DisabledDataSource
     */
    @Bean
    @ConditionalOnProperty(name = "db.active", havingValue = "false", matchIfMissing = true)
    public DataSource disabledDataSource() {
        return new DisabledDataSource(dbActive);
    }


    /**
     * 데이터소스 설정: 로컬 환경
     *
     * @return DataSource
     */
    @Bean
    @Profile("local")
    @ConditionalOnProperty(name = "db.active", havingValue = "true", matchIfMissing = false)
    public DataSource localDataSource() {
        return createDataSource(
                "DB_LOCAL",
                "jdbc:log4jdbc:postgresql://localhost:5432/mydb",
                "API(JvV71oWlyNo=)",
                "API(bvgRMU+VWcH0y+3ebtA4oA==)",
                5,
                3000,
                3000
        );
    }


    /**
     * 데이터소스 설정: 개발 환경
     *
     * @return DataSource
     */
    @Bean
    @Profile("dev")
    @ConditionalOnProperty(name = "db.active", havingValue = "true", matchIfMissing = false)
    public DataSource devDataSource() {
        return createDataSource(
                "DB_DEV",
                "jdbc:log4jdbc:postgresql://54.180.36.144:5432/postgres",
                "API(JvV71oWlyNo=)",
                "API(bvgRMU+VWcH0y+3ebtA4oA==)",
                10,
                3000,
                3000
        );
    }


    /**
     * 데이터소스 설정: 스테이징 및 프로덕션 환경
     *
     * @return DataSource
     */
    @Bean
    @Profile({"stg", "prd"})
    @ConditionalOnProperty(name = "db.active", havingValue = "true", matchIfMissing = false)
    public DataSource stgOrProdDataSource() {
        return createDataSource(
                "DB_STG_PRD",
                "jdbc:log4jdbc:postgresql://staging.or.prod.host:5432/proddb",
                "API(stagingOrProdUser)",
                "API(stagingOrProdPassword)",
                20,
                5000,
                5000
        );
    }


    /**
     * 공통 데이터소스 생성 메서드
     *
     * @param poolName          커넥션 풀 이름
     * @param jdbcUrl           데이터베이스 URL
     * @param encryptedUsername 암호화된 사용자 이름
     * @param encryptedPassword 암호화된 비밀번호
     * @param maximumPoolSize   최대 연결 풀 크기
     * @param connectionTimeout 연결 타임아웃 (ms)
     * @param validationTimeout 유효성 검사 타임아웃 (ms)
     * @return DataSource
     */
    private DataSource createDataSource(
            String poolName,
            String jdbcUrl,
            String encryptedUsername,
            String encryptedPassword,
            int maximumPoolSize,
            int connectionTimeout,
            int validationTimeout) {

        HikariConfig hikariConfig = new HikariConfig();

        // HikariCP 설정
        hikariConfig.setPoolName(poolName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy"); //  JDBC 드라이버 클래스 이름

        // Jasypt 복호화
        hikariConfig.setUsername(jasyptStringEncryptor.decrypt(encryptedUsername.replace("API(", "").replace(")", "")));
        hikariConfig.setPassword(jasyptStringEncryptor.decrypt(encryptedPassword.replace("API(", "").replace(")", "")));

        // HikariCP 추가 설정
        hikariConfig.setMaximumPoolSize(maximumPoolSize);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setValidationTimeout(validationTimeout);

        // 테스트 쿼리 추가
        hikariConfig.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(hikariConfig);
    }
}
