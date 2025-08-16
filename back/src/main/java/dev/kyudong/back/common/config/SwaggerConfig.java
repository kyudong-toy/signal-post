package dev.kyudong.back.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		// Info 객체를 생성하고 원하는 정보를 설정합니다.
		Info info = new Info()
				.title("Signal Post API")
				.version("v0.0.1") // 버전 정보
				.description("1인 미디어 플랫폼 Signal Post의 API 명세서입니다.");

		// OpenAPI 객체를 생성하고, 위에서 만든 Info 객체를 설정합니다.
		return new OpenAPI()
				.components(new Components()) // components는 비워두어도 괜찮습니다.
				.info(info);
	}

}
