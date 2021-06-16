package io.github.gcdd1993.cas.client.app1

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

/**
 * @author gcdd1993
 */
@SpringBootApplication
class Demo1Application

private val log = LoggerFactory.getLogger(Demo1Application::class.java)

fun main(args: Array<String>) {
    try {
        runApplication<Demo1Application>(*args)
    } catch (ex: Exception) {
        log.error("app start failed.", ex)
        exitProcess(-1)
    }
}