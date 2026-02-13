package in.ashar.mooble;

import in.ashar.mooble.configuration.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableAsync
@SpringBootApplication
@EnableMethodSecurity
@EnableWebSecurity
@EnableConfigurationProperties(AppProperties.class)
@EnableCaching
public class MoobleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoobleApplication.class, args);
	}

}
