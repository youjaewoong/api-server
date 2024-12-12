package api.server.common.library;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import api.server.common.exception.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonMergePatchHelper {

	private final ObjectMapper mapper;

	public <T> T mergePatch(JsonMergePatch mergePatch, T targetBean, Class<T> beanClass) {
		try {
			JsonNode target = mapper.convertValue(targetBean, JsonNode.class);
			JsonNode patched = mergePatch.apply(target);
			return mapper.convertValue(patched, beanClass);
		} catch (JsonPatchException | IllegalArgumentException exception) {
			throw new InternalServerErrorException("Json Merge Patch to Object Convert Error", exception);
		}
	}
}
