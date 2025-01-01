package api.server.interfacestorage.service;


import api.server.common.exception.custom.BusinessException;
import api.server.interfacestorage.enmus.InterfaceStorageErrorCode;
import api.server.interfacestorage.request.FieldInfo;
import api.server.interfacestorage.request.InterfaceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterfaceParserService {

	/**
	 * 엑셀 파일에서 데이터를 파싱하여 InterfaceInfo 객체 생성
	 *
	 * @param file          업로드된 엑셀 파일
	 * @param interfaceId   인터페이스 ID
	 * @param interfaceName 인터페이스 이름
	 * @return InterfaceInfo 객체
	 */
	public static InterfaceInfo parseExcelFile(MultipartFile file, String interfaceId, String interfaceName) {
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트를 가져옴
			List<FieldInfo> inFields = new ArrayList<>();
			List<FieldInfo> outFields = new ArrayList<>();

			for (Row row : sheet) {
				if (row.getRowNum() == 0) continue; // 헤더 행 스킵

				String type = row.getCell(0).getStringCellValue(); // "구분" (in/out)
				String fieldId = row.getCell(1).getStringCellValue(); // 필드 ID
				String fieldName = row.getCell(2).getStringCellValue(); // 필드 이름
				int length = (int) row.getCell(3).getNumericCellValue(); // 필드 길이
				int offset = (int) row.getCell(4).getNumericCellValue(); // 필드 오프셋

				FieldInfo fieldInfo = FieldInfo.builder()
						.fieldId(fieldId)
						.fieldName(fieldName)
						.length(length)
						.offset(offset)
						.build();

				if ("in".equalsIgnoreCase(type)) {
					inFields.add(fieldInfo);
				} else if ("out".equalsIgnoreCase(type)) {
					outFields.add(fieldInfo);
				} else {
					throw new BusinessException(InterfaceStorageErrorCode.INVALID_REQUEST);
				}
			}

			return InterfaceInfo.builder()
					.interfaceId(interfaceId)
					.interfaceName(interfaceName)
					.inFields(inFields)
					.outFields(outFields)
					.totalInLength(inFields.stream().mapToInt(FieldInfo::getLength).sum())
					.totalOutLength(outFields.stream().mapToInt(FieldInfo::getLength).sum())
					.build();

		} catch (IOException e) {
			throw new BusinessException(InterfaceStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}
}
