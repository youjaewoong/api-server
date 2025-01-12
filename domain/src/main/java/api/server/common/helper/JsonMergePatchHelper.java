package api.server.common.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JsonMergePatchHelper 클래스는 JSON Merge Patch를 활용하여 객체의 일부 필드만 업데이트할 수 있는 유틸리티를 제공합니다.
 *
 * 주요 기능:
 * - JSON Merge Patch를 사용하여 기존 객체의 특정 필드만 업데이트.
 * - Jackson ObjectMapper를 사용하여 객체와 JSON 간의 변환 처리.
 *
 * 사용 사례:
 * - HTTP PATCH 요청의 JSON 데이터를 기존 객체에 병합하여 업데이트할 때 사용.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7396">JSON Merge Patch RFC 7396</a>
 */
@Component
@RequiredArgsConstructor
public class JsonMergePatchHelper {

	private final ObjectMapper mapper;

	/**
	 * JSON Merge Patch를 적용하여 기존 객체를 업데이트합니다.
	 *
	 * @param mergePatch JSON Merge Patch 객체
	 * @param targetBean 업데이트 대상 객체
	 * @param beanClass 업데이트 대상 객체의 클래스 타입
	 * @param <T> 업데이트 대상 객체의 타입
	 * @return 업데이트된 객체
	 * @throws Exception JSON Merge Patch 적용 또는 변환 중 오류가 발생한 경우
	 */
	public <T> T mergePatch(JsonMergePatch mergePatch, T targetBean, Class<T> beanClass) throws Exception {
		try {
			// 대상 객체를 JsonNode로 변환
			JsonNode target = mapper.convertValue(targetBean, JsonNode.class);

			// Merge Patch 적용
			JsonNode patched = mergePatch.apply(target);

			// JsonNode를 객체로 변환
			return mapper.convertValue(patched, beanClass);
		} catch (JsonPatchException | IllegalArgumentException exception) {
			// Merge Patch 적용 실패 시 예외 처리
			throw new Exception("Json Merge Patch to Object Convert Error", exception);
		}
	}
}
