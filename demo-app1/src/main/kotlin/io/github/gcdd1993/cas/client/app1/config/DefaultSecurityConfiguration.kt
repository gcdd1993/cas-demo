package io.github.gcdd1993.cas.client.app1.config

import io.github.gcdd1993.cas.client.app1.cas.CasAuthenticationRedirectParamEntryPoint
import io.github.gcdd1993.cas.client.app1.cas.CasAuthenticationSuccessHandler
import io.github.gcdd1993.cas.client.app1.cas.DynamicServiceAuthenticationDetailsSource
import io.github.gcdd1993.cas.client.app1.cas.StatelessSingleSignOutFilter
import io.github.gcdd1993.cas.client.app1.filter.CookieAuthenticationFilter
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportAware
import org.springframework.core.type.AnnotationMetadata
import org.springframework.http.HttpMethod
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.authentication.CasAuthenticationToken
import org.springframework.security.cas.web.CasAuthenticationEntryPoint
import org.springframework.security.cas.web.CasAuthenticationFilter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.AnyRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
class DefaultSecurityConfiguration(
    private val userService: DefaultPatternUserService
) : WebSecurityConfigurerAdapter(), ImportAware {
    private var disableAuth = false
    private var disableSslCheck = false

    @Value("\${authn.cas.prefix}")
    private lateinit var casPrefix: String

    @Value("\${authn.cas.service}")
    private lateinit var thisService: String

    @Value("\${authn.cas.logout-success-url}")
    private lateinit var logoutSuccessUrl: String

    private val insecureHttpsURLConnectionFactory = InsecureHttpsURLConnectionFactory()

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        val disableAuth = disableAuth
        http.csrf().disable() // 由于API通常部署在反向代理后面，CORS检查没有意义
            .cors().disable()
        // 通常为了高可用部署会禁用spring的session生成，采用cookie验证的方案。
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        // 用户访问/logout时，登出本地session，同时跳转到统一登出
        http.logout { logout ->
            logout
                .logoutRequestMatcher(AntPathRequestMatcher("/server/logout", HttpMethod.GET.name)) // 这个地方加入的service参数，代表经CAS登出后再跳回到应用本身的指定页面。
                .logoutSuccessUrl(casPrefix + "/logout?service=" + URLEncoder.encode(logoutSuccessUrl, StandardCharsets.UTF_8))
                .addLogoutHandler(userService::handleLogout)
                .addLogoutHandler(CookieClearingLogoutHandler("token"))
        }

        // 拦截/login/cas，并处理Service Ticket的认证
        http.addFilter(casAuthenticationFilter())
        // 拦截CAS服务发来的SLO统一登出消息
        http.addFilterBefore(singleSignOutFilter(), LogoutFilter::class.java)
        // 进行基于cookie的认证
        http.addFilterBefore(cookieAuthenticationFilter(), LogoutFilter::class.java)
        // 允许一些路径完全绕过认证（optional）
        http.authorizeRequests { authorize ->
            authorize
                .antMatchers(
                    "/server/login/cas",
                    "/server/back_channel_logout"
                ).permitAll()
                .antMatchers("/server/**").authenticated()
            if (disableAuth) {
                authorize.anyRequest().permitAll()
            } else {
                authorize.anyRequest().authenticated()
            }
        }
        // 配置不同的路径在未认证时采用不同的返回
        http.exceptionHandling { configurer ->
            configurer
                .defaultAuthenticationEntryPointFor(
                    casAuthenticationEntryPoint(),
                    AntPathRequestMatcher("/server/landing", HttpMethod.GET.name)
                )
                .defaultAuthenticationEntryPointFor(
                    casGatewayAuthenticationEntryPoint(),
                    OrRequestMatcher(
                        listOf(
                            AntPathRequestMatcher("/api/**", HttpMethod.GET.name),
                            AntPathRequestMatcher("/server/probe", HttpMethod.GET.name)
                        )
                    )
                )
                .defaultAuthenticationEntryPointFor(
                    Http403ForbiddenEntryPoint(),
                    AnyRequestMatcher.INSTANCE
                )
        }
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(casAuthenticationProvider())
    }

    @Bean
    fun serviceProperties(): ServiceProperties {
        return ServiceProperties()
            .apply {
                service = thisService
            }
    }

    @Bean
    @Throws(Exception::class)
    fun casAuthenticationFilter(): CasAuthenticationFilter {
        val filter = CasAuthenticationFilter()
        filter.setFilterProcessesUrl("/server/login/cas")
        filter.setAuthenticationManager(authenticationManager())
        filter.setAuthenticationDetailsSource(DynamicServiceAuthenticationDetailsSource(serviceProperties()))
        val successHandler = CasAuthenticationSuccessHandler()
            .apply {
                casAuthSuccessCallback = object : CasAuthenticationSuccessHandler.CasAuthenticationSuccessCallback {
                    override fun handleCasAuthSuccess(
                        request: HttpServletRequest,
                        response: HttpServletResponse,
                        authenticationToken: CasAuthenticationToken,
                        serviceTicket: String
                    ) {
                        userService.handleCasAuthSuccess(request, response, authenticationToken, serviceTicket)
                    }
                }
            }
        filter.setAuthenticationSuccessHandler(successHandler)
        filter.setAllowSessionCreation(false)
        return filter
    }

    @Bean
    fun singleSignOutFilter(): StatelessSingleSignOutFilter {
        return StatelessSingleSignOutFilter.builder()
            .logoutCallbackPath("/server/back_channel_logout")
            .logoutTicketHandler(object : StatelessSingleSignOutFilter.LogoutTicketHandler {
                override fun handleLogoutTicket(serviceTicket: String) {
                    userService.handleCasRemoteSingleLogout(serviceTicket)
                }
            })
            .build()
    }

    @Bean
    fun cookieAuthenticationFilter(): CookieAuthenticationFilter {
        return CookieAuthenticationFilter(userService)
    }

    @Bean
    fun casAuthenticationEntryPoint(): CasAuthenticationEntryPoint {
        return CasAuthenticationRedirectParamEntryPoint()
            .apply {
                serviceProperties = serviceProperties()
                loginUrl = "$casPrefix/login"
            }
    }

    /**
     * 这个entry point用于非界面交互的场景，在CAS会话不存在的情况下直接返回失败。
     */
    @Bean
    fun casGatewayAuthenticationEntryPoint(): CasAuthenticationEntryPoint {
        return CasAuthenticationRedirectParamEntryPoint()
            .apply {
                serviceProperties = serviceProperties()
                loginUrl = "$casPrefix/login?gateway=true"
            }
    }

    @Bean
    fun casAuthenticationProvider(): CasAuthenticationProvider {
        val ticketValidator = Cas30ServiceTicketValidator(casPrefix)
        if (disableSslCheck) {
            ticketValidator.setURLConnectionFactory(insecureHttpsURLConnectionFactory)
        }
        val provider = CasAuthenticationProvider()
        provider.setServiceProperties(serviceProperties())
        provider.setTicketValidator(ticketValidator)
        provider.setAuthenticationUserDetailsService(userService)
        provider.setKey("cas_auth_provider")
        return provider
    }

    @Bean
    fun singleSignOutHttpSessionListener(): SingleSignOutHttpSessionListener {
        return SingleSignOutHttpSessionListener()
    }

    override fun setImportMetadata(importMetadata: AnnotationMetadata) {
        val annotation = importMetadata.annotations.get(EnableDefaultSecurityConfiguration::class.java)
        if (annotation.isPresent) {
            disableAuth = annotation.getBoolean("disableAuth")
            disableSslCheck = annotation.getBoolean("disableSslCheck")
        }
    }
}