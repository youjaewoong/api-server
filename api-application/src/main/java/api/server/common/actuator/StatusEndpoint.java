package api.server.common.actuator;

import brave.internal.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "status")
@RequiredArgsConstructor
public class StatusEndpoint {

    private final HealthEndpoint healthEndpoint;

    private static final String OK_STATUS = "200 OK"; // 상수로 추출

    /**
     * actuator status endpoint 값을 추가합니다.
     *
     * @return 문자열 status
     */
    @ReadOperation
    public StatusHealth findStatus() {
        Status status = healthEndpoint.health().getStatus();
        return createStatusHealth(status); // 생성자 호출 가독성을 높임
    }

    private StatusHealth createStatusHealth(Status status) {
        return new StatusHealth(status); // 메서드로 분리하여 가독성 개선
    }

    @Getter
    private static class StatusHealth { // 이름 변경하여 명확성 제공
        @Nullable
        private final String status;

        private StatusHealth(Status status) {
            this.status = status.equals(Status.UP) ? OK_STATUS : null; // 논리 간소화 및 null 처리 명시화
        }
    }
}
