package api.server.gramstorage.helpler;

import api.server.common.constant.ProfileConstant;
import api.server.common.properties.GramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@RequiredArgsConstructor
@Slf4j
public class GramFilePathHelper {

    private final GramProperties gramProperties;

    private static final String FILE_EXTENSION = ".json"; // 파일 확장자


    /**
     * 현재 실행 환경에 따른 기본 경로를 반환합니다.
     *s
     * @return 기본 경로 (개발 또는 JAR 환경)
     */
    public String getBasePath() {
        if (isLocal()) {
            return gramProperties.getJsonPathLocal();
        }
        return gramProperties.getJsonPathJar();
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
    private boolean isLocal() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null) {
            return true;
        }
        return ProfileConstant.LOCAL.equals(profile);
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
