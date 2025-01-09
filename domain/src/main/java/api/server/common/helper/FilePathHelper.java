package api.server.common.helper;

import api.server.common.properties.FilePathProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilePathHelper {

    private final FilePathProperties filePathProperties;

    private static final String FILE_EXTENSION = ".json"; // 파일 확장자


    /**
     * 현재 실행 환경에 따른 기본 경로를 반환합니다.
     *s
     * @return 기본 경로 (개발 또는 JAR 환경)
     */
    public String getBasePath() {
        if (isRunningInJar()) {
            return filePathProperties.getJar();
        }
        return filePathProperties.getLocal();
    }


    /**
     * 특정 파일 ID에 대한 전체 파일 경로를 반환합니다.
     *
     * @param fileName 파일 이름
     * @return 전체 파일 경로
     */
    public String getFilePath(String fileName) {
        return getBasePath() + '/' +  fileName + FILE_EXTENSION;
    }


    /**
     * 현재 JAR 파일 내에서 실행 중인지 확인합니다.
     *
     * @return JAR 환경 여부
     */
    private boolean isRunningInJar() {
        try {
            URL resource = this.getClass().getResource("");
            if (resource != null) {
                String protocol = resource.getProtocol();
                return "jar".equals(protocol); // JAR 환경인지 확인
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to determine JAR environment", e);
            return false;
        }
    }


    /**
     * 파일 확장자를 반환합니다.
     *
     * @return 파일 확장자
     */
    public String getFileExtension() {
        return FILE_EXTENSION;
    }

}
