package api.server.common.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.json-path")
public class FilePathProperties {

    /** 로컬 개발 환경 경로 */
    private String local;

    /** JAR 실행 환경 경로 */
    private String jar;

}
