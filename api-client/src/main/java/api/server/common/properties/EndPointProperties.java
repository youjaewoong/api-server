package api.server.common.properties;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

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

    private static final String END_POINT_PREFIX = "endpoint.";

    private final Environment environment;

    @Value("${gram.type}")
    private String gramType;

    private String van;
    private int vanPort;

    public EndPointProperties(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (gramType == null) {
            throw new IllegalStateException("gramType 값이 설정되지 않았습니다.");
        }
        initializePocProperties();
    }

    private void initializePocProperties() {
        String pocKey = END_POINT_PREFIX + gramType + ".van";
        String pocPortKey = END_POINT_PREFIX + gramType + ".van-port";

        this.van = environment.getProperty(pocKey);
        this.vanPort = Optional.ofNullable(environment.getProperty(pocPortKey))
                .map(Integer::parseInt)
                .orElseThrow(() -> new IllegalStateException(pocPortKey + " 값이 설정되지 않았습니다."));
    }
}