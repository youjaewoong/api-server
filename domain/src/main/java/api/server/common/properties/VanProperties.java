package api.server.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "van.kicc")
public class VanProperties {
    private String host;
    private int port;
    private int threadPoolSize;
    private int timeoutMs;
    private String clientClass;
    private String serviceType;


}
