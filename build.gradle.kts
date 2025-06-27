plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.sonarqube") version "4.4.1.3373"
    id("maven-publish")
    id("signing")
    id("java-library")
}

group = property("group") as String
version = property("version") as String
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Core
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-aop")

    // Event Bus - RabbitMQ
    api("org.springframework.boot:spring-boot-starter-amqp")

    // Distributed Lock - Redis
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.redisson:redisson-spring-boot-starter:${property("redissonVersion")}")

    // Kotlin Support
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // JSON Processing
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Configuration
    api("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:${property("mockkVersion")}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// SonarCloud 설정
sonar {
    properties {
        property("sonar.projectKey", property("sonarProjectKey") as String)
        property("sonar.organization", property("sonarOrganization") as String)
        property("sonar.host.url", property("sonarHostUrl") as String)
        property("sonar.sources", "src/main")
        property("sonar.tests", "src/test")
        property("sonar.language", "kotlin")
        property("sonar.sourceEncoding", "UTF-8")

        // 컴파일 스킵 설정 (deprecated 경고 해결)
        property("sonar.gradle.skipCompile", "true")

        // 코드 커버리지 설정
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")

        // 제외할 파일들 (필요에 따라 수정)
        property("sonar.coverage.exclusions", "**/*Config.kt,**/*Application.kt")
    }
}

// Maven Central 배포 설정
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set(property("projectName") as String)
                description.set(property("projectDescription") as String)
                url.set(property("projectUrl") as String)

                licenses {
                    license {
                        name.set(property("licenseName") as String)
                        url.set(property("licenseUrl") as String)
                    }
                }

                developers {
                    developer {
                        id.set(property("developerId") as String)
                        name.set(property("developerName") as String)
                        email.set(property("developerEmail") as String)
                    }
                }

                scm {
                    connection.set(property("scmConnection") as String)
                    developerConnection.set(property("scmDeveloperConnection") as String)
                    url.set(property("scmUrl") as String)
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
