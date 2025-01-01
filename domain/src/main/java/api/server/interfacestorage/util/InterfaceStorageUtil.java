package api.server.interfacestorage.util;

import api.server.interfacestorage.constant.InterfaceConstants;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InterfaceStorageUtil {

    /** 파일 저장 기본 경로 (resources/interface) */
    public static String INTERFACE_PATH;

    /** 파일 타입 (lottecard) */
    public static String FILE_TYPE;


    @Value("${file.interface-path}")
    public void setInterfacePath(String interfacePath) {
        INTERFACE_PATH = interfacePath;
    }

    @Value("${file.type}")
    public void setFileType(String fileType) {
        FILE_TYPE = fileType;
    }

    public static String getStoragePath() {
        return INTERFACE_PATH + "/" + FILE_TYPE;
    }

    public static String getFilePath(String interfaceId) {
        return getStoragePath() + "/" + interfaceId + InterfaceConstants.FILE_EXTENSION;
    }
}
