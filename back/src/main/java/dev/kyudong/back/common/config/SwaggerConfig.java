package dev.kyudong.back.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Signal Post API 명세서",
				description = "1인 미디어 플랫폼 Signal Post의 API 명세서입니다.",
				version = "v0.0.2"
		)
)
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		String jwtSchemeName = "jwtAuth";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

		Components components = new Components()
				.addSecuritySchemes(jwtSchemeName, new SecurityScheme()
						.name(jwtSchemeName)
						.type(SecurityScheme.Type.HTTP) 	// HTTP 방식
						.scheme("bearer") 					// bearer 토큰 방식
						.bearerFormat("JWT")); 				// 토큰 형식은 JWT

		return new OpenAPI()
				.components(components)
				.addSecurityItem(securityRequirement);
	}

}
