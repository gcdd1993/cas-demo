package io.github.gcdd1993.cas.client.app1.config

import org.springframework.http.HttpStatus
import org.springframework.security.web.util.UrlUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletResponse

@RestController
class DefaultPatternController {
    @GetMapping("/api/server/landing")
    protected fun landing(
        @RequestParam( required = false, defaultValue = "/") to: String?,
        response: HttpServletResponse
    ) {
        // 不允许重定向到绝对路径，以防御某些类型的跨站攻击
        if (!UrlUtils.isValidRedirectUrl(to) ||
            UrlUtils.isAbsoluteUrl(to) ||
            to!!.startsWith("//")
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect path")
        }
        response.status = HttpServletResponse.SC_TEMPORARY_REDIRECT
        response.addHeader("Location", to)
    }

    @GetMapping("/server/probe")
    protected fun probe() {
        // Do nothing, just for auth checking purpose
    }
}