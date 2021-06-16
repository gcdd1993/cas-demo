package io.github.gcdd1993.cas.client.app1.cas

import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

/**
 * 用于配置[org.springframework.security.cas.web.CasAuthenticationFilter]。
 * 默认情况下，当CAS跳转回应用的/login/cas节点时，应用将以固定的不带参数的/login/cas URL去验证ST (Service Ticket).
 * 当/login/cas附带跳转参数，用于登录成功后跳转时，将出现请求ST和验证ST所用的serviceId不同的情况，从而验证失败。
 * 这个类的作用是动态地获取当前的/login/cas路径后附带的参数，并附加在验证ST的请求中，从而使得整个流程可用。
 */
class DynamicServiceAuthenticationDetailsSource(
    serviceProperties: ServiceProperties
) : AuthenticationDetailsSource<HttpServletRequest, ServiceAuthenticationDetails> {
    private val ticketParamPattern = Pattern.compile("\\b&?ticket=[^?&]*")
    private val service = serviceProperties.service
    private val separator = if (service.contains("?")) "&" else "?"
    override fun buildDetails(context: HttpServletRequest): ServiceAuthenticationDetails {
        return ServiceAuthenticationDetails {
            val requestURI = context.requestURI
            val queryString = context.queryString
            assert(service.endsWith(requestURI))
            service + separator + ticketParamPattern.matcher(queryString).replaceFirst("")
        }
    }

}