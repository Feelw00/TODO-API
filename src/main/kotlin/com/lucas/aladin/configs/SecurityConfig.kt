package com.lucas.aladin.configs

import com.lucas.aladin.configs.filters.JwtAuthenticationEntryPoint
import com.lucas.aladin.configs.filters.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtFilter: JwtFilter, private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) {
    private val publicEndpoints = arrayOf(
        "/api-docs",
        "/api-docs/**",
        "/v1/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/users/login",
        "/users/signup",
    )

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }.httpBasic { it.disable() }.exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }.authorizeHttpRequests {
                it.requestMatchers(*publicEndpoints).permitAll().anyRequest().authenticated()
            }.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}