package io.github.gcdd1993.cas.client.app1.config

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.util.matcher.OrRequestMatcher


/**
 * @author gcdd1993
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableDefaultSecurityConfiguration(
    disableAuth = true,
    disableSslCheck = true
)
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.requestMatcher(
            OrRequestMatcher(
//                AntPathRequestMatcher("/api/user", HttpMethod.GET.name)
                // 跳过登录和权限校验的api
            )
        )
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests().anyRequest().permitAll()
    }
}