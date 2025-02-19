package api.server.common.properties;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * <pre>
 *  EndPointProperties 클래스는 애플리케이션에서 엔드포인트 설정을 로드하고 관리하는 역할을 합니다.
 *  Environment 객체를 활용하여 application.yml 에서 설정된 값을 동적으로 가져옵니다.
 * </pre>
 */
@Slf4j
@Getter
@Component
@ToString
public class EndPointProperties {

    private final Environment environment;

    private static final String END_POINT = "endpoint.";

    @Value("${gram.type}")
    private String gramType;

    private String eai;
    private String api;
    private String fax;
    private int faxPort;

    public EndPointProperties(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (gramType == null) {
            throw new IllegalStateException("gramType 값이 설정되지 않았습니다.");
        }
        this.eai = environment.getProperty(END_POINT + gramType + ".eai");
        this.api = environment.getProperty(END_POINT + gramType + ".api");
        this.fax = environment.getProperty(END_POINT + gramType + ".fax");
        this.faxPort = Integer.parseInt(Objects.requireNonNull(environment.getProperty(END_POINT + gramType + ".fax-port")));
    }

}