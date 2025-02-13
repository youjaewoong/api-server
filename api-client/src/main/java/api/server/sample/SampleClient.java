package api.server.sample;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import api.server.sample.response.SampleFeignResponse;

@FeignClient(contextId = "sampleClient", name = "feign-sample", url = "${feign.sample.url}")
public interface SampleClient {

	@GetMapping(value = "users")
	List<SampleFeignResponse> selectSampleFeign();

}
