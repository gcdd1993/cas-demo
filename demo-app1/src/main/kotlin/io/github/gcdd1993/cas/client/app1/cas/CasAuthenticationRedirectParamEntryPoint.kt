package io.github.gcdd1993.cas.client.app1.cas

import org.springframework.http.HttpMethod
import org.springframework.security.cas.web.CasAuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 用于拦截需要进行CAS认证的路径，跳转到cas认证url，并将当前的url作为跳转参数拼接到认证url中。
 * 这个跳转参数最终将在CAS认证完成跳转会应用时，由[CasAuthenticationSuccessHandler]捕捉并处理。
 * 默认的跳转参数名为redirect。
 */
class CasAuthenticationRedirectParamEntryPoint : CasAuthenticationEntryPoint() {
    private var redirectParam = "redirect"

    override fun createServiceUrl(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {
        var serviceUrl = super.createServiceUrl(request, response)
        if (!HttpMethod.GET.matches(request.method)) {
            // 如果不是get请求，就不要附加redirect参数了
            return serviceUrl
        }
        serviceUrl += if (serviceUrl.contains("?") && !serviceUrl.endsWith("&")) {
            "&"
        } else {
            "?"
        }
        serviceUrl += redirectParam + "=" + URLUtil.urlEncode(getRequestURIWithQuery(request))
        return serviceUrl
    }

    private fun getRequestURIWithQuery(request: HttpServletRequest): String {
        val queryString = request.queryString
        return if (queryString == null || queryString.isEmpty()) {
            request.requestURI
        } else {
            request.requestURI.toString() + "?" + queryString
        }
    }

    fun setRedirectParam(redirectParam: String) {
        this.redirectParam = redirectParam
    }

    init {
        this.encodeServiceUrlWithSessionId = false
    }
}