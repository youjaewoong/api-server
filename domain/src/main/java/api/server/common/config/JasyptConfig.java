package api.server.common.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;

/**
 * JasyptConfig 클래스는 애플리케이션에서 Jasypt를 사용한 암호화를 구성합니다.
 *
 * 주요 기능:
 * - Jasypt를 이용하여 문자열 암호화 및 복호화를 처리합니다.
 *
 * 설정:
 * - `jasypt.encryptor.secret-key` 프로퍼티를 사용하여 암호화 키를 설정합니다.
 * - `PBEWithMD5AndDES` 알고리즘과 고정된 솔트를 사용합니다.
 */
@EnableEncryptableProperties
@Configuration
public class JasyptConfig {

    /** Jasypt 암호화에 사용할 비밀 키 (Base64 인코딩) */
    @Value("${jasypt.encryptor.secret-key}")
    private String secretKey;

    /**
     * Jasypt 암호화/복호화를 위한 StringEncryptor Bean을 생성합니다.
     *
     * @return StringEncryptor 객체
     */
    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {

        // Jasypt 암호화 설정 객체 생성
        var config = new SimpleStringPBEConfig();
        config.setAlgorithm("PBEWithMD5AndDES"); // 암호화에 사용할 알고리즘
        config.setPoolSize("2"); // 인스턴스 풀 크기 설정
        config.setPassword(new String(Base64Utils.decode(secretKey.getBytes()))); // Base64 디코딩된 암호화 키
        config.setSaltGenerator(new StringFixedSaltGenerator("someFixedSalt")); // 고정된 솔트 값 사용

        // 암호화 객체 생성 및 설정 적용
        var encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }
}
