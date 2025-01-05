package api.server.common.property;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gram")
public class GramProperty {

    /** gram.file-path 값 매핑 */
    private String filePath;

    /** gram.type 값 매핑  */
    private String type;

}
