package api.server.common.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class GramProperties {

    /** gram.file-path 값 */
    @Value("${gram.file-path}")
    private String filePath;

    /** gram.type 값 */
    @Value("${gram.type}")
    private String type;

    /** 로컬 개발 시 JSON 파일 경로 */
    @Value("${gram.json-path.local}")
    private String jsonPathLocal;

    /** JAR 빌드 시 JSON 파일 경로 */
    @Value("${gram.json-path.jar}")
    private String jsonPathJar;

}
