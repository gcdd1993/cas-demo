package io.github.gcdd1993.cas.client.app1.cas

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.cas.authentication.CasAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 用于配置[org.springframework.security.cas.web.CasAuthenticationFilter]。
 * 不使用这个类时，默认逻辑是从本地缓存中获得之前被拦截的请求url，创建session并跳转回此url。
 * 当应用集群式部署时，基于本地缓存进行跳转不再有效。使用这个类将读取url中的redirect参数
 * （可配置，需与[CasAuthenticationRedirectParamEntryPoint]中的配置对应），并
 * 跳转到指定的url。
 * 同时这个类也提供了一个对于CAS认证成功的回调[.setCasAuthSuccessCallback]，应用可在这里加入token创建相关的逻辑。
 */
class CasAuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    var casAuthSuccessCallback: CasAuthenticationSuccessCallback =
        object : CasAuthenticationSuccessCallback {
            override fun handleCasAuthSuccess(
                request: HttpServletRequest,
                response: HttpServletResponse,
                authenticationToken: CasAuthenticationToken,
                serviceTicket: String
            ) {
            }

        }

    @Throws(IOException::class, ServletException::class)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (authentication is CasAuthenticationToken && authentication.isAuthenticated) {
            val ticketParam = URLUtil.parseQuery(request.queryString)["ticket"]
            check(!(ticketParam == null || ticketParam.size != 1)) { "None or multiple service tickets presented in url" }
            val serviceTicket = ticketParam[0]
            try {
                casAuthSuccessCallback.handleCasAuthSuccess(request, response, authentication, serviceTicket)
            } catch (e: Exception) {
                log.error("Error calling casAuthSuccessCallback", e)
            }
        }
        super.handle(request, response, authentication)
    }

    /**
     * 这个类与其父类的唯一差别是跳过了使用sessionId对跳转url进行encode的步骤。
     */
    private class NoSessionRedirectStrategy : DefaultRedirectStrategy() {
        @Throws(IOException::class)
        override fun sendRedirect(request: HttpServletRequest, response: HttpServletResponse, url: String?) {
            val redirectUrl: String = calculateRedirectUrl(request.contextPath, url)
            if (logger.isDebugEnabled) {
                logger.debug("Redirecting to '$redirectUrl'")
            }

            // 绕过http 1.0兼容的重定向路径转绝对路径逻辑，手动设置重定向相关信息。
            response.status = HttpStatus.SEE_OTHER.value()
            response.setHeader("Location", response.encodeRedirectURL(redirectUrl))
        }
    }

    interface CasAuthenticationSuccessCallback {
        fun handleCasAuthSuccess(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authenticationToken: CasAuthenticationToken,
            serviceTicket: String
        )
    }

    init {
        redirectStrategy = NoSessionRedirectStrategy()
        targetUrlParameter = "redirect"
    }
}