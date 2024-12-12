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
 * dev, prd 환경에서만 동작합니다.
 */
@EnableEncryptableProperties
@Configuration
public class JasyptConfig {
    @Value("${jasypt.encryptor.secret-key}")
    private String secretKey;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {

        var config = new SimpleStringPBEConfig();
        config.setAlgorithm("PBEWithMD5AndDES"); // 암호화 알고리즘
        config.setPoolSize("2"); // 인스턴스 pool
        config.setPassword(new String(Base64Utils.decode(secretKey.getBytes())));
        config.setSaltGenerator(new StringFixedSaltGenerator("someFixedSalt"));

        var encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }
}
