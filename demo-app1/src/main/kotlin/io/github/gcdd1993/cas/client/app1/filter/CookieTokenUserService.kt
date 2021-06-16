package io.github.gcdd1993.cas.client.app1.filter

import org.springframework.security.core.userdetails.UserDetails

interface CookieTokenUserService {
    fun getUserByCookieToken(token: String): UserDetails?
}