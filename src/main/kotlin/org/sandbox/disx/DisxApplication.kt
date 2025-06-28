package org.sandbox.disx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["org.sandbox.disx"])
class DisxApplication

fun main(args: Array<String>) {
    runApplication<DisxApplication>(*args)
}
