plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    `maven-publish`
    signing
    `java-library`
}

group = "io.github.disx"
version = "1.0.0"
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
    api("org.redisson:redisson-spring-boot-starter:3.24.3")

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
    testImplementation("io.mockk:mockk:1.13.8")
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

// Maven Central 배포 설정
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Disx - Distributed Event Framework")
                description.set("A lightweight Kotlin-first distributed event framework with Outbox pattern and distributed locks")
                url.set("https://github.com/Hoya324/disx") // TODO: 설정

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {// TODO: 설정
                        id.set("disx")
                        name.set("Disx")
                        email.set("disx0626@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Hoya324/disx.git") // TODO: 설정
                    developerConnection.set("scm:git:ssh://github.com/Hoya324/disx.git") // TODO: 설정
                    url.set("https://github.com/Hoya324/disx")// TODO: 설정
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
