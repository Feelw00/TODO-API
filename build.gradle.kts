plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("kapt") version "1.9.25"
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.lucas"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot 기본
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")


	// SQLite + JDBC + Dialect
	runtimeOnly("org.xerial:sqlite-jdbc:3.45.3.0")
	implementation("org.hibernate.orm:hibernate-community-dialects:6.5.0.Final")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

	// Swagger (SpringDoc)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// Flyway for SQLite
	implementation("org.flywaydb:flyway-core:10.17.2")

	// MapStruct
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")

	// Kotest
	testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
	testImplementation("io.kotest:kotest-assertions-core:5.9.0")
	testImplementation("io.kotest:kotest-framework-engine:5.9.0")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")

	// Kotlin 기본
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// 테스트
	testImplementation("io.mockk:mockk:1.13.10")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "junit")
	}
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()

	reports.junitXml.required.set(false)
	reports.html.required.set(false)
}
