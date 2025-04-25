package api.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"api"})
public class ApiServerApplication {
	private static final Logger log = LoggerFactory.getLogger(ApiServerApplication.class);
	
	public static void main(String[] args) {
		log.info("totalMemory >>>>>>> {}", Runtime.getRuntime().totalMemory());
		log.info("maxMemory >>>>>>> {}", Runtime.getRuntime().maxMemory());
		System.setProperty("timestamp", String.valueOf(LocalDateTime.now()));

		SpringApplication app = new SpringApplication(ApiServerApplication.class);
		app.setDefaultProperties(Collections.singletonMap("spring.profiles.default", "local")); // default profiles local
		app.run(args);
	}
}