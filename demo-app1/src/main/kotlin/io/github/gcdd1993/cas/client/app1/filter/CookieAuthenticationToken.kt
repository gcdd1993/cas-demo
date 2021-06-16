package io.github.gcdd1993.cas.client.app1.filter

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class CookieAuthenticationToken(
    private val principal: Any,
    authorities: Collection<GrantedAuthority>
) : AbstractAuthenticationToken(authorities) {
    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return principal
    }

    init {
        isAuthenticated = true
    }
}