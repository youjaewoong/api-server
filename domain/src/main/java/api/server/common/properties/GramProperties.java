package api.server.common.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gram")
public class GramProperties {

    /** gram.file-path 값 매핑 */
    private String filePath;

    /** gram.type 값 매핑  */
    private String type;

}
