package jobkorea.careerpath;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication(scanBasePackages = {"jobkorea", "common.standard"})
public class JobkoreaCareerpathApiApplication {
	public static void main(String[] args) {
		log.info("totalMemory >>>>>>> {}", Runtime.getRuntime().totalMemory());
		log.info("maxMemory >>>>>>> {}", Runtime.getRuntime().maxMemory());
		System.setProperty("timestamp", String.valueOf(LocalDateTime.now()));
		SpringApplication.run(JobkoreaCareerpathApiApplication.class, args);
	}
}
