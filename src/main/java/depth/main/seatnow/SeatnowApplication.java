package depth.main.seatnow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
public class SeatnowApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeatnowApplication.class, args);
	}

}
