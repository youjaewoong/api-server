package api.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication(scanBasePackages = {"api", "common.standard"})
public class ApiServerApplication {
	public static void main(String[] args) {
		log.info("totalMemory >>>>>>> {}", Runtime.getRuntime().totalMemory());
		log.info("maxMemory >>>>>>> {}", Runtime.getRuntime().maxMemory());
		System.setProperty("timestamp", String.valueOf(LocalDateTime.now()));
		SpringApplication.run(ApiServerApplication.class, args);
	}
}
