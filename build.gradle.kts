import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"
	id("com.github.davidmc24.gradle.plugin.avro") version "1.2.1"
}

group = "com.infinum"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	maven (
		url = "https://packages.confluent.io/maven/"
	)
}

dependencies {
	implementation("org.apache.kafka:kafka-clients:2.8.0")
	implementation("org.apache.kafka:kafka-streams:2.8.0")
	implementation("org.apache.avro:avro:1.10.2")
	implementation("io.confluent:kafka-avro-serializer:6.2.0")
	implementation("io.confluent:kafka-streams-avro-serde:6.2.0")
	implementation("io.confluent:kafka-schema-registry-client:6.2.0")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.generateAvroJava {
	source = fileTree("src/main/resources/avro")
	setCreateSetters("false")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
