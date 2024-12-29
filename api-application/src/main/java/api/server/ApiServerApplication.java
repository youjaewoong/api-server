package api.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@SpringBootApplication(scanBasePackages = {"api", "common.standard"})
public class ApiServerApplication {
	public static void main(String[] args) {
		log.info("totalMemory >>>>>>> {}", Runtime.getRuntime().totalMemory());
		log.info("maxMemory >>>>>>> {}", Runtime.getRuntime().maxMemory());
		System.setProperty("timestamp", String.valueOf(LocalDateTime.now()));

		SpringApplication app = new SpringApplication(ApiServerApplication.class);
		app.setDefaultProperties(Collections.singletonMap("spring.profiles.default", "local")); // default profiles local
		app.run(args);
	}
}