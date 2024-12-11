package jobkorea.careerpath.common.config;

/**
 * swagger config
 */

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.YearMonth;

@OpenAPIDefinition(info = @Info(
		title = "SAMPLE",
		description = "",
		version = "1.0"),
		servers = @Server(url = "/", description = "Default server url"))
@Configuration
public class OpenApiConfig {

	static {
		/**
		 * ParameterObject Class 내부에 YearMonth Type 을 선언할 경우, /v3/api-docs 호출시에 StackOverFlow 가 발생합니다.
		 * 이를 방지하기 위해 YearMonth Type 을 SimpleType 으로 표시하도록 조정합니다.
		 */
		SpringDocUtils.getConfig().addSimpleTypesForParameterObject(YearMonth.class);
	}
	@Bean
	@Profile({"local","dev"})
	public GroupedOpenApi localAndDevOpenApi() {
		return setGroupedOpenApi();
	}
	@Bean
	@Profile({"stg","prd"})
	public GroupedOpenApi stgAndPrdOpenApi() {
		String [] packagesToExclude = {"jobkorea.careerpath.sample"};
		return setGroupedOpenApi(packagesToExclude);
	}
	@Bean
	public OpenApiCustomiser openApiCustomiser() {
		return openApi -> openApi.getPaths()
				.values()
				.forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
					ApiResponses apiResponses = operation.getResponses();
					apiResponses.addApiResponse("400", new ApiResponse().description("Bad Request"));
					apiResponses.addApiResponse("500", new ApiResponse().description("Server Error"));
				}));
	}
	private GroupedOpenApi setGroupedOpenApi(String... packagesToExclude) {
		return GroupedOpenApi.builder()
				.group("SAMPLE 서비스")
				.pathsToMatch("/*/**")
				.packagesToExclude(packagesToExclude)
				.build();
	}
}
