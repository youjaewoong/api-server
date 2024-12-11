package jobkorea.careerpath.sample;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "sampleSearchClient", name = "albamon-search", url = "${feign.albamon-search.url}")
public interface SampleSearchClient {

	@GetMapping(value = "search")
	String selectSampleSearch(@RequestParam("select") String select,
							  @RequestParam("from") String from,
							  @RequestParam("where") String where,
							  @RequestParam("limit") int limit);

	@GetMapping(value = "volumes")
	String selectVolume();
}
