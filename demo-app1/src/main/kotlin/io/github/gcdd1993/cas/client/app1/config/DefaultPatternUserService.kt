package io.github.gcdd1993.cas.client.app1.config

import io.github.gcdd1993.cas.client.app1.filter.CookieTokenUserService
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken
import org.springframework.security.cas.authentication.CasAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface DefaultPatternUserService :
    AuthenticationUserDetailsService<CasAssertionAuthenticationToken>, CookieTokenUserService {
    fun handleCasAuthSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authenticationToken: CasAuthenticationToken,
        serviceTicket: String
    )

    fun handleCasRemoteSingleLogout(serviceTicket: String)
    fun handleLogout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    )
}