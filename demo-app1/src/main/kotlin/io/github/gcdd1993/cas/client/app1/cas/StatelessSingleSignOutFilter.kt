package io.github.gcdd1993.cas.client.app1.cas

import org.jasig.cas.client.session.SessionMappingStorage
import org.jasig.cas.client.session.SingleSignOutHandler
import org.jasig.cas.client.util.AbstractConfigurationFilter
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * cas-client提供的SingleSignOutFliter只能基于Servlet Session进行单点登出请求的处理。
 * 这里进行改写，允许用户传入一个handler，自行定义单点登出处理的逻辑。
 */
class StatelessSingleSignOutFilter : AbstractConfigurationFilter() {
    private val handler = SingleSignOutHandler()
    private val handlerInitialized = AtomicBoolean(false)
    private var logoutTicketHandler: LogoutTicketHandler = object : LogoutTicketHandler {
        override fun handleLogoutTicket(serviceTicket: String) {

        }
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain
    ) {
        val request = servletRequest as HttpServletRequest
        val response = servletResponse as HttpServletResponse
        if (handler.process(request, response)) {
            filterChain.doFilter(servletRequest, servletResponse)
        }
    }

    private fun init() {
        handler.setEagerlyCreateSessions(false)
        handler.sessionMappingStorage = StatelessSessionMappingStorage()
        handler.init()
        handlerInitialized.set(true)
    }

    private fun setArtifactParameterName(name: String) {
        handler.setArtifactParameterName(name)
    }

    private fun setLogoutParameterName(name: String) {
        handler.setLogoutParameterName(name)
    }

    private fun setRelayStateParameterName(name: String) {
        handler.setRelayStateParameterName(name)
    }

    private fun setLogoutCallbackPath(logoutCallbackPath: String) {
        handler.setLogoutCallbackPath(logoutCallbackPath)
    }

    private fun setLogoutTicketHandler(logoutTicketHandler: LogoutTicketHandler) {
        this.logoutTicketHandler = logoutTicketHandler
    }

    interface LogoutTicketHandler {
        fun handleLogoutTicket(serviceTicket: String)
    }

    class Builder {
        private var instance: StatelessSingleSignOutFilter? = StatelessSingleSignOutFilter()
        fun build(): StatelessSingleSignOutFilter {
            val result = instance
            result!!.init()
            instance = null
            return result
        }

        fun artifactParameterName(name: String): Builder {
            instance!!.setArtifactParameterName(name)
            return this
        }

        fun logoutParameterName(name: String): Builder {
            instance!!.setLogoutParameterName(name)
            return this
        }

        fun relayStateParameterName(name: String): Builder {
            instance!!.setRelayStateParameterName(name)
            return this
        }

        fun logoutCallbackPath(logoutCallbackPath: String): Builder {
            instance!!.setLogoutCallbackPath(logoutCallbackPath)
            return this
        }

        fun logoutTicketHandler(logoutTicketHandler: LogoutTicketHandler): Builder {
            instance!!.logoutTicketHandler = logoutTicketHandler
            return this
        }
    }

    private inner class StatelessSessionMappingStorage : SessionMappingStorage {
        override fun removeSessionByMappingId(mappingId: String): HttpSession? {
            logoutTicketHandler.handleLogoutTicket(mappingId)
            return null
        }

        override fun removeBySessionById(sessionId: String?) {
            // do nothing
        }

        override fun addSessionById(mappingId: String?, session: HttpSession?) {
            // do nothing
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}