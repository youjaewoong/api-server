package api.server.fixedlength.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FixedLengthCommonHeaderService {

	private static final String COMMON_HEADER_PATH = "src/main/resources/lottecard/commHeader.json";

	public String saveCommonHeader(MultipartFile file) {
		try {
			// JSON 저장 경로의 디렉토리 생성 확인 및 처리
			File filePath = new File(COMMON_HEADER_PATH);
			File directory = filePath.getParentFile();
			if (directory != null && !directory.exists()) {
				boolean dirCreated = directory.mkdirs();
				if (!dirCreated) {
					throw new RuntimeException("디렉토리를 생성하지 못했습니다: " + directory.getAbsolutePath());
				}
			}

			// 공통 헤더 데이터 처리 (예: JSON 생성)
			Map<String, Map<String, Integer>> commonHeader = new LinkedHashMap<>();
			commonHeader.put("userId", Map.of("fieldLength", 10));
			commonHeader.put("userName", Map.of("fieldLength", 15));

			// totalLength 계산
			int totalLength = commonHeader.values().stream()
					.mapToInt(field -> field.get("fieldLength"))
					.sum();

			// JSON 데이터에 totalLength 추가
			Map<String, Object> jsonData = new LinkedHashMap<>(commonHeader);
			jsonData.put("totalLength", totalLength);

			// JSON 파일 생성 및 저장
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(filePath, jsonData);

			return "공통 헤더가 성공적으로 저장되었습니다.";
		} catch (IOException e) {
			throw new RuntimeException("JSON 파일 저장 중 오류 발생", e);
		}
	}

	public Map<String, Object> getCommonHeader() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(new File(COMMON_HEADER_PATH), Map.class);
		} catch (IOException e) {
			throw new RuntimeException("공통 헤더 데이터 읽기 중 오류 발생", e);
		}
	}
}
