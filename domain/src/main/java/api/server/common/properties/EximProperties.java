package api.server.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "exim")
public class EximProperties {

    private String jdbcUrl;
    private String username;
    private String password;
    private int minConnections;
    private int maxConnections;
    private int maxLifetime;

}
