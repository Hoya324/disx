java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

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

    // Event Bus - Kafka
    api("org.springframework.kafka:spring-kafka")

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

// JitPack을 위한 JAR 태스크 설정 (bootJar 비활성화)
tasks.jar {
    enabled = true
    archiveClassifier = ""
}

tasks.bootJar {
    enabled = false
}

// SonarCloud 설정
sonar {
    properties {
        property("sonar.projectKey", "Hoya324_disx")
        property("sonar.organization", "disxhoya324")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.gradle.skipCompile", "true")
    }
}

// Maven Central 배포 설정
publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "disx"

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
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}
