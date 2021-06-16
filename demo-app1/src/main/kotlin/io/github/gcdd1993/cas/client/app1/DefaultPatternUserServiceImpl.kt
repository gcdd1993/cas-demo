package io.github.gcdd1993.cas.client.app1

import io.github.gcdd1993.cas.client.app1.config.DefaultPatternUserService
import org.slf4j.LoggerFactory
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken
import org.springframework.security.cas.authentication.CasAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author gcdd1993
 */
@Component
class DefaultPatternUserServiceImpl : DefaultPatternUserService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val secureRandom = SecureRandom();

    override fun handleCasAuthSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authenticationToken: CasAuthenticationToken,
        serviceTicket: String
    ) {
        log.info("cas auth success, tgt: {}", serviceTicket)
        val user = authenticationToken.userDetails

        val cookie = Cookie("token", generateToken())
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = Math.toIntExact(60 * 60 * 4)
        response.addCookie(cookie)

    }

    override fun handleCasRemoteSingleLogout(serviceTicket: String) {
        // 这里未删除cookie，所以在其他地方必须处理token过期
        log.info("cas remote single out success, tgt: {}", serviceTicket)
    }

    override fun handleLogout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        log.info("logout {}", authentication.principal.toString())
    }

    override fun loadUserDetails(token: CasAssertionAuthenticationToken): UserDetails {
        log.info("cas login success, pretend load user details")
        return User("gcdd1993", "", listOf())
    }

    override fun getUserByCookieToken(token: String): UserDetails? {
        log.info("get user by cookie token")
        return User("gcdd1993", "", listOf())
    }

    private fun generateToken(): String {
        val bytes = ByteArray(96)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

}