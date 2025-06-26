package org.sandbox.disx

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<DisxApplication>().with(TestcontainersConfiguration::class).run(*args)
}
