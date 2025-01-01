package api.server.interfacestorage;

import api.server.interfacestorage.request.InterfaceInfo;
import api.server.interfacestorage.service.InterfaceParserService;
import api.server.interfacestorage.service.InterfaceStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InterfaceStorageController implements InterfaceStorageControllerApi {

    private final InterfaceParserService interfaceParserService;
    private final InterfaceStorageService interfaceStorageService;


    /**
     * 엑셀 파일 업로드 및 데이터 저장
     *
     * @param file          업로드된 엑셀 파일
     * @param interfaceId   인터페이스 ID
     * @param interfaceName 인터페이스 이름
     * @return 성공 메시지
     */
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("interfaceId") String interfaceId,
            @RequestParam("interfaceName") String interfaceName
    ) {
        InterfaceInfo interfaceInfo = InterfaceParserService.parseExcelFile(file, interfaceId, interfaceName);
        interfaceStorageService.saveJsonToFile(interfaceId, interfaceInfo);
        return ResponseEntity.ok("File uploaded and parsed successfully.");
    }

    /**
     * 특정 인터페이스 ID로 데이터 조회
     *
     * @param interfaceId 조회할 인터페이스 ID
     * @return InterfaceInfo 객체
     */
    public ResponseEntity<InterfaceInfo> getInterface(@PathVariable String interfaceId) {
        InterfaceInfo interfaceInfo = interfaceStorageService.readJsonFromFile(interfaceId);
        return ResponseEntity.ok(interfaceInfo);
    }

    /**
     * 저장된 모든 인터페이스 ID 리스트 조회
     *
     * @return 인터페이스 ID 리스트
     */
    public ResponseEntity<List<String>> listAllInterfaceIds() {
        List<String> interfaceIds = interfaceStorageService.listAllInterfaceIds();
        return ResponseEntity.ok(interfaceIds);
    }

}
