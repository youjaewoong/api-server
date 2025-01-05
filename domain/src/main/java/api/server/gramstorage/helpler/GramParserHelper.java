package api.server.gramstorage.helpler;


import api.server.common.exception.custom.BusinessException;
import api.server.common.vo.FieldInfoVO;
import api.server.gramstorage.enmus.GramStorageErrorCode;
import api.server.gramstorage.request.GramInfoRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Slf4j
public class GramParserHelper {

	/**
	 * 엑셀 파일에서 데이터를 파싱하여 gramInfo 객체 생성
	 *
	 * @param file          업로드된 엑셀 파일
	 * @param gramId   전문 ID
	 * @param gramName 전문 이름
	 * @return gramInfo 객체
	 */
	public static GramInfoRequest parseExcelFile(MultipartFile file, String gramId, String gramName, String serviceId) {
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트를 가져옴
			List<FieldInfoVO> inFields = new ArrayList<>();
			List<FieldInfoVO> outFields = new ArrayList<>();

			for (Row row : sheet) {
				if (row.getRowNum() == 0) continue; // 헤더 행 스킵

				String type = row.getCell(0).getStringCellValue(); // "구분" (in/out)
				String fieldId = row.getCell(1).getStringCellValue(); // 필드 ID
				String fieldName = row.getCell(2).getStringCellValue(); // 필드 이름
				int length = (int) row.getCell(3).getNumericCellValue(); // 필드 길이
				int offset = (int) row.getCell(4).getNumericCellValue(); // 필드 오프셋

				FieldInfoVO fieldInfo = FieldInfoVO.builder()
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
					throw new BusinessException(GramStorageErrorCode.INVALID_REQUEST);
				}
			}

			return GramInfoRequest.builder()
					.gramId(gramId)
					.gramName(gramName)
					.serviceId(serviceId)
					.inFields(inFields)
					.outFields(outFields)
					.totalInLength(inFields.stream().mapToInt(FieldInfoVO::getLength).sum())
					.totalOutLength(outFields.stream().mapToInt(FieldInfoVO::getLength).sum())
					.build();

		} catch (IOException e) {
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}
}
