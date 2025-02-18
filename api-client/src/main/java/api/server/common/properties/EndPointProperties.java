package api.server.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * <pre>
 *  EndPointProperties 클래스는 애플리케이션에서 엔드포인트 설정을 로드하고 관리하는 역할을 합니다.
 *  Environment 객체를 활용하여 application.yml 에서 설정된 값을 동적으로 가져옵니다.
 * </pre>
 */
@Getter
@Component
public class EndPointProperties {

    private final Environment environment;

    @Value("${gram.type}")
    private String gramType;

    private String eai;
    private String api;

    public EndPointProperties(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (gramType == null) {
            throw new IllegalStateException("gramType 값이 설정되지 않았습니다.");
        }
        this.eai = environment.getProperty("endpoint." + gramType + ".eai");
        this.api = environment.getProperty("endpoint." + gramType + ".api");
    }

}