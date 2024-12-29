package api.server.common.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.Base64Utils;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class JasyptConfigTest {

    @Nested
    @DisplayName("암호화 및 복호화하여 추출 및 검증합니다.")
    class JasyptConfig {
        @Test
        @DisplayName("jasypt 암호화 및 복호화 검증")
        public void jasyptEncryptAndDecrypt() {

            // give
            Map<String, String> map = Map.of(
                    "id", "my-user",
                    "password", "my-password");

            var config = new StandardPBEStringEncryptor();
            config.setPassword("gos1004"); // target key
            config.setAlgorithm("PBEWithMD5AndDES");
            config.setSaltGenerator(new StringFixedSaltGenerator("someFixedSalt"));
            for (String key : map.keySet()) {
                // when
                String encrypted = config.encrypt(map.get(key));
                log.info("key : {} value : {}", key, encrypted);
                String decrypted = config.decrypt(encrypted);
                // then
                assertThat(map.get(key)).isEqualTo(decrypted);
            }
        }

        @Test
        @DisplayName("Base64 암호화 및 복호화 검증")
        @SneakyThrows
        public void Base64EncryptAndDecrypt() {

            // give
            String message = "";

            // when
            var data = Base64Utils.encodeToString(message.getBytes());
            var targetData = new String(Base64Utils.decode(data.getBytes()));

            log.info("key : {}", data);

            // then
            assertThat(message).isEqualTo(targetData);
        }
    }
}