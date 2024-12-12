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

    /**
     * actuator status endpoint 값을 추가합니다.
     * @return 문자열 status
     */
    @ReadOperation
    public Health findStatus() {
        return new Health(healthEndpoint.health().getStatus());
    }
    @Getter
    private static class Health {
        @Nullable
        private String status;
        private Health(Status status) {
            if (status.equals(Status.UP)) {
                this.status = "200 OK";
            }
        }
    }
}
