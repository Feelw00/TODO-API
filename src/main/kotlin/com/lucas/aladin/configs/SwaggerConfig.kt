package com.lucas.aladin.configs

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "JWT 기반 TODO 백엔드 API 구현",
        description = "SQLite3과 JWT 인증을 기반으로 한 TODO 관리 API 구현",
        version = "1.0",
    )
)
@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val schemeName = "bearerAuth"
        val securityRequirement = SecurityRequirement().addList(schemeName)
        val securityScheme = SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER).name("Authorization")
        val components = Components().addSecuritySchemes(schemeName, securityScheme)
        return OpenAPI().components(components).addSecurityItem(securityRequirement)
    }
}
