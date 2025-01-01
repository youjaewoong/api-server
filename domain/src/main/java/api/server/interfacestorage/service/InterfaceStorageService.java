package api.server.interfacestorage.service;

import api.server.common.exception.custom.BusinessException;
import api.server.interfacestorage.constant.InterfaceConstants;
import api.server.interfacestorage.enmus.InterfaceStorageErrorCode;
import api.server.interfacestorage.request.InterfaceInfo;
import api.server.interfacestorage.util.InterfaceStorageUtil;
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
public class InterfaceStorageService {

	private final ObjectMapper objectMapper;


	/**
	 * JSON 데이터를 파일로 저장
	 *
	 * @param interfaceId 저장할 파일 이름
	 * @param data 저장할 데이터
	 */
	public void saveJsonToFile(String interfaceId, InterfaceInfo data) {
		String filePath = InterfaceStorageUtil.getFilePath(interfaceId);
		Path path = Paths.get(filePath);

		try {
			Files.createDirectories(path.getParent());
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
		} catch (IOException e) {
			throw new BusinessException(InterfaceStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}


	/**
	 * JSON 파일에서 데이터를 읽어오기
	 *
	 * @param interfaceId 읽을 파일 이름
	 * @return InterfaceInfo 객체
	 */
	public InterfaceInfo readJsonFromFile(String interfaceId) {
		String filePath = InterfaceStorageUtil.getFilePath(interfaceId);
		Path path = Paths.get(filePath);

		if (!Files.exists(path)) {
			throw new BusinessException(InterfaceStorageErrorCode.DATA_NOT_FOUND);
		}

		try {
			return objectMapper.readValue(path.toFile(), InterfaceInfo.class);
		} catch (IOException e) {
			throw new BusinessException(InterfaceStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}


	/**
	 * 저장된 모든 인터페이스 ID 조회
	 *
	 * @return 인터페이스 ID 리스트
	 */
	public List<String> listAllInterfaceIds() {
		Path path = Paths.get(InterfaceStorageUtil.getStoragePath());
		if (!Files.exists(path)) {
			return new ArrayList<>();
		}

		try (Stream<Path> files = Files.list(path)) {
			return files.filter(Files::isRegularFile)
					.map(file -> file.getFileName().toString().replace(InterfaceConstants.FILE_EXTENSION, ""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new BusinessException(InterfaceStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}
}
