package api.server.gramstorage.service;

import api.server.common.exception.custom.BusinessException;
import api.server.gramstorage.enmus.GramStorageErrorCode;
import api.server.gramstorage.helpler.GramFilePathHelper;
import api.server.gramstorage.request.GramInfoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * GramStorageService
 * JSON 데이터를 클래스 파일로 읽기 및 조회하는 서비스를 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "stg", "prd"})
public class GramStorageServiceImpl implements GramStorageService {

	private final ObjectMapper objectMapper; // JSON 직렬화 및 역직렬화를 위한 ObjectMapper
	private final GramFilePathHelper gramFilePathHelper;


	/**
	 * JSON 데이터를 파일로 저장합니다.
	 *
	 * @param gramId 저장할 파일의 ID (파일명으로 사용됨)
	 * @param data   저장할 데이터 (JSON 포맷)
	 */
	public void uploadExcel(String gramId, GramInfoRequest data) {
		log.info("파일업로드가 제공되지 않습니다. {}, {}", gramId, data);
	}


	/**
	 * JSON 파일에서 데이터를 읽어옵니다.
	 *
	 * @param gramId 읽을 파일의 ID (파일명으로 사용됨)
	 * @return GramInfoRequest 객체로 반환
	 */
	public GramInfoRequest findGramInfo(String gramId) {
		// 파일 경로 생성
		String resourcePath = gramFilePathHelper.getFilePath(gramId);
		log.info("resourcePath ::: {}", resourcePath);

		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				log.warn("Resource not found in classpath: {}", resourcePath);
				throw new BusinessException(GramStorageErrorCode.DATA_NOT_FOUND, resourcePath);
			}
			// JSON 파일을 GramInfoRequest 객체로 역직렬화
			return objectMapper.readValue(inputStream, GramInfoRequest.class);
		} catch (IOException e) {
			log.error("Failed to read resource from classpath: {}", resourcePath, e);
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}


	/**
	 * 저장된 모든 전문 ID 리스트를 조회합니다 (클래스패스에서).
	 *
	 * @return 저장된 전문 ID 리스트
	 */
	public List<String> findAllGramList() {
		String resourcePattern = gramFilePathHelper.getFilePath("*");
		log.info("resourcePattern ::: {}", resourcePattern);
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources(resourcePattern);

			return Arrays.stream(resources)
					.map(resource -> {
						try {
							String fileName = resource.getFilename();
							return fileName != null
									? fileName.replace(gramFilePathHelper.getFileExtension(), "")
									: null;
						} catch (Exception e) {
							log.error("Failed to process resource: {}", resource, e);
							return null;
						}
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} catch (IOException e) {
			log.error("Failed to list resources in classpath: {}", resourcePattern, e);
			throw new BusinessException(GramStorageErrorCode.FILE_STORAGE_FAILED);
		}
	}
}
