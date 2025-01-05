package api.server.gramstorage.service;

import api.server.common.exception.custom.BusinessException;
import api.server.gramstorage.enmus.GramStorageErrorCode;
import api.server.gramstorage.helpler.GramStorageHelper;
import api.server.gramstorage.request.GramInfoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class GramStorageService {

	private final ObjectMapper objectMapper;
	private final GramStorageHelper gramStorageHelper;

	/**
	 * JSON 데이터를 파일로 저장
	 *
	 * @param gramId 저장할 파일 이름
	 * @param data 저장할 데이터
	 */
	public void uploadExcel(String gramId, GramInfoRequest data) {
		String filePath = gramStorageHelper.getFilePath(gramId);
		Path path = Paths.get(filePath);

		try {
			Files.createDirectories(path.getParent());
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
		} catch (IOException e) {
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}


	/**
	 * JSON 파일에서 데이터를 읽어오기
	 *
	 * @param gramId 읽을 파일 이름
	 * @return GramInfo 객체
	 */
	public GramInfoRequest findGramInfo(String gramId) {
		String filePath = gramStorageHelper.getFilePath(gramId);
		Path path = Paths.get(filePath);

		if (!Files.exists(path)) {
			throw new BusinessException(GramStorageErrorCode.DATA_NOT_FOUND);
		}

		try {
			return objectMapper.readValue(path.toFile(), GramInfoRequest.class);
		} catch (IOException e) {
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}


	/**
	 * 저장된 모든 전문 ID 조회
	 *
	 * @return 전문 ID 리스트
	 */
	public List<String> findAllGramList() {
		Path path = Paths.get(gramStorageHelper.getStoragePath());
		if (!Files.exists(path)) {
			return new ArrayList<>();
		}

		try (Stream<Path> files = Files.list(path)) {
			return files.filter(Files::isRegularFile)
					.map(file -> file.getFileName().toString().replace(gramStorageHelper.getFileExtension(), ""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}
}
