package api.server.gramstorage.helpler;

import api.server.common.property.GramProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GramStorageHelper {

    private final GramProperty gramProperty;
    public static final String FILE_EXTENSION = ".json";   // 파일 확장자

    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    public String getStoragePath() {
        return gramProperty.getFilePath() + "/" + gramProperty.getType();
    }

    public String getFilePath(String interfaceId) {
        return getStoragePath() + "/" + interfaceId + getFileExtension();
    }
}
