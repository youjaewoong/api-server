package api.server.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kms")
public class KmsProperties {
    private String kmsHost;
    private int kmsPort;
    private String propSvcId;
    private String cekSvcId01;
    private String lastKeyIndex;

}
