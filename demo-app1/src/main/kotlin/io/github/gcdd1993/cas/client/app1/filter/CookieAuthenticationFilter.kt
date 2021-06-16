package io.github.gcdd1993.cas.client.app1.filter

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 */
class CookieAuthenticationFilter(private val userService: CookieTokenUserService) : OncePerRequestFilter() {
    private val tokenCookieName = "token"

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cookies = request.cookies
        if (cookies != null) {
            val token = Arrays.stream(cookies).filter { c: Cookie -> tokenCookieName == c.name }
                .filter { !StringUtils.isEmpty(it.value) }
                .findFirst()
                .map { it.value }
            if (token.isPresent) {
                val user = userService.getUserByCookieToken(token.get())
                if (user != null) {
                    val authenticationToken = CookieAuthenticationToken(user, user.authorities)
                    SecurityContextHolder.getContext().authentication = authenticationToken
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}