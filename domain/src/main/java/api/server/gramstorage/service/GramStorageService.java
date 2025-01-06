package api.server.gramstorage.service;

import api.server.common.exception.custom.BusinessException;
import api.server.common.helper.FilePathHelper;
import api.server.gramstorage.enmus.GramStorageErrorCode;
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

/**
 * GramStorageService
 * JSON 데이터를 파일로 저장, 읽기 및 조회하는 서비스를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GramStorageService {

	private final ObjectMapper objectMapper; // JSON 직렬화 및 역직렬화를 위한 ObjectMapper
	private final FilePathHelper filePathHelper;


	/**
	 * JSON 데이터를 파일로 저장합니다.
	 *
	 * @param gramId 저장할 파일의 ID (파일명으로 사용됨)
	 * @param data   저장할 데이터 (JSON 포맷)
	 */
	public void uploadExcel(String gramId, GramInfoRequest data) {
		// 데이터 유효성 검사
		if (data == null) {
			throw new BusinessException(GramStorageErrorCode.INVALID_REQUEST);
		}

		// 파일 경로 생성
		String filePath = filePathHelper.getFilePath(gramId);
		Path path = Paths.get(filePath).toAbsolutePath(); // 절대 경로로 변환

		try {
			// 파일 경로에 부모 디렉토리가 없으면 생성
			Files.createDirectories(path.getParent());
			// 데이터를 JSON 파일로 저장 (Pretty Print 형식)
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
		} catch (IOException e) {
			// 파일 저장 실패 시 로그를 출력하고 BusinessException 발생
			log.error("Failed to store file at path: {}", filePath, e);
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}

	/**
	 * JSON 파일에서 데이터를 읽어옵니다.
	 *
	 * @param gramId 읽을 파일의 ID (파일명으로 사용됨)
	 * @return GramInfoRequest 객체로 반환
	 */
	public GramInfoRequest findGramInfo(String gramId) {
		// 파일 경로 생성
		String filePath = filePathHelper.getFilePath(gramId);
		Path path = Paths.get(filePath).toAbsolutePath();

		// 파일 존재 여부 및 읽기 가능 여부 확인
		if (!Files.exists(path) || !Files.isReadable(path)) {
			log.warn("File not found or not readable: {}", filePath);
			throw new BusinessException(GramStorageErrorCode.DATA_NOT_FOUND);
		}

		try {
			// JSON 파일을 GramInfoRequest 객체로 역직렬화
			return objectMapper.readValue(path.toFile(), GramInfoRequest.class);
		} catch (IOException e) {
			// 파일 읽기 실패 시 로그를 출력하고 BusinessException 발생
			log.error("Failed to read file at path: {}", filePath, e);
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}

	/**
	 * 저장된 모든 전문 ID 리스트를 조회합니다.
	 *
	 * @return 저장된 전문 ID 리스트
	 */
	public List<String> findAllGramList() {
		// 저장 경로 가져오기
		Path path = Paths.get(filePathHelper.getBasePath()).toAbsolutePath();

		// 디렉토리가 존재하지 않거나 유효하지 않으면 빈 리스트 반환
		if (!Files.exists(path) || !Files.isDirectory(path)) {
			log.warn("Storage path is invalid or not a directory: {}", path);
			return new ArrayList<>();
		}

		try (Stream<Path> files = Files.list(path)) {
			// 디렉토리 내 모든 파일을 필터링하여 ID 리스트 생성
			return files.filter(Files::isRegularFile) // 정규 파일만 필터링
					.map(file -> file.getFileName().toString()
							.replace(filePathHelper.getFileExtension(), "")) // 확장자 제거
					.collect(Collectors.toList());
		} catch (IOException e) {
			// 파일 리스트 조회 실패 시 로그를 출력하고 BusinessException 발생
			log.error("Failed to list files in directory: {}", path, e);
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}
}
