package com.placement.portal;

import com.placement.portal.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EntityScan(basePackages = "com.placement.portal.domain")
@EnableJpaRepositories(basePackages = "com.placement.portal.repository")
@EnableConfigurationProperties(JwtProperties.class)
@EnableMethodSecurity(prePostEnabled = true)
@EnableAsync
public class PlacementPortalBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlacementPortalBackendApplication.class, args);
	}
}
