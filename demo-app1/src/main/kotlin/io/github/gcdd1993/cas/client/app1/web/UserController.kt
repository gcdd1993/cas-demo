package io.github.gcdd1993.cas.client.app1.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author gcdd1993
 */
@RestController
@RequestMapping("/api")
class UserController {

    @GetMapping("/user")
    fun listUser(): List<String> {
        return listOf(
            "gcdd19931",
            "gcdd19932",
            "gcdd19933",
            "gcdd19934"
        )
    }
}