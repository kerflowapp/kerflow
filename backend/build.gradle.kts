import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("io.sentry.jvm.gradle") version "5.12.2"
}

group = "com.kerflowapp"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.ai:spring-ai-bom:1.1.8"))
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.9.10")
    implementation("org.postgresql:postgresql")

    // Cognito
    implementation(platform("software.amazon.awssdk:bom:2.54.7"))
    implementation("software.amazon.awssdk:cognitoidentity")
    implementation("software.amazon.awssdk:cognitoidentityprovider")

    // Email
    implementation("com.resend:resend-java:4.6.0")

    // Stripe
    implementation("com.stripe:stripe-java:33.1.0")

    // HTML scraping
    implementation("org.jsoup:jsoup:1.18.3")

    // Mappers
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.apache.commons:commons-lang3:3.20.0")

    compileOnly("org.projectlombok:lombok")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sentry {
    includeSourceContext = !System.getenv("SENTRY_AUTH_TOKEN").isNullOrBlank()
    org = System.getenv("SENTRY_ORG") ?: ""
    projectName = System.getenv("SENTRY_PROJECT") ?: "kerflow-backend"
    authToken = System.getenv("SENTRY_AUTH_TOKEN") ?: ""
}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<BootJar>("bootJar") {
    archiveClassifier.set("boot")
}

tasks.named<Jar>("jar") {
    enabled = false
}
