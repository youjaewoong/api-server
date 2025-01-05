package api.server.gramstorage;

import api.server.gramstorage.helpler.GramParserHelper;
import api.server.gramstorage.request.GramInfoRequest;
import api.server.gramstorage.service.GramStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GramStorageController implements GramStorageControllerApi {

    private final GramStorageService gramStorageService;


    /**
     * 엑셀 파일 업로드 및 데이터 저장
     *
     * @param file      업로드된 엑셀 파일
     * @param gramId    전문 ID
     * @param gramName  전문 이름
     * @param serviceId 전문 서비스 ID
     * @return 성공 메시지S
     */
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("gramId") String gramId,
            @RequestParam("gramName") String gramName,
            @RequestParam("serviceId") String serviceId
    ) {
        GramInfoRequest gramInfo = GramParserHelper.parseExcelFile(file, gramId, gramName, serviceId);
        gramStorageService.uploadExcel(gramId, gramInfo);
        return ResponseEntity.ok("File uploaded and parsed successfully.");
    }


    /**
     * 특정 전문 ID로 데이터 조회
     *
     * @param  gramId    조회할 전문 ID
     * @return gramInfo  객체
     */
    public ResponseEntity<GramInfoRequest> findGramInfo(@PathVariable String gramId) {
        GramInfoRequest gramInfo = gramStorageService.findGramInfo(gramId);
        return ResponseEntity.ok(gramInfo);
    }


    /**
     * 저장된 모든 전문 ID 리스트 조회
     *
     * @return 전문 ID 리스트
     */
    public ResponseEntity<List<String>> findAllGramList() {
        List<String> gramIds = gramStorageService.findAllGramList();
        return ResponseEntity.ok(gramIds);
    }

}
