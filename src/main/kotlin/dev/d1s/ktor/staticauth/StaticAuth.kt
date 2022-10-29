/*
 * Copyright 2022 Mikhail Titov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.d1s.ktor.staticauth

import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*

private const val STATIC_AUTHENTICATION_SCHEME = "Static"
private const val STATIC_AUTHENTICATION_PREFIX = "$STATIC_AUTHENTICATION_SCHEME "
private const val STATIC_AUTHENTICATION_CHALLENGE_KEY = "StaticAuth"
private const val STATIC_AUTHENTICATION_TOKEN_PROPERTY = "static-auth.token"
private const val STATIC_AUTHENTICATION_REALM_PROPERTY = "static-auth.realm"
private const val DEFAULT_STATIC_AUTHENTICATION_REALM = "Ktor Server"

/**
 * [AuthenticationProvider] that provides Static authorization support.
 */
public class StaticAuthenticationProvider(private val config: Config) : AuthenticationProvider(config) {

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        config.externalConfig = context.call.application.environment.config

        context.call.parseToken()?.let { parsedToken ->
            if (parsedToken == requireNotNull(config.token)) {
                context.setPrincipal()
                return
            }
        }

        context.challenge(
            STATIC_AUTHENTICATION_CHALLENGE_KEY, AuthenticationFailedCause.InvalidCredentials
        ) { challenge, call ->
            val response = newUnauthorizedResponse()

            call.respond(response)

            challenge.complete()
        }
    }

    private fun ApplicationCall.parseToken() =
        this.request.header(HttpHeaders.Authorization)?.removePrefix(STATIC_AUTHENTICATION_PREFIX)

    private fun newUnauthorizedResponse() = UnauthorizedResponse(
        HttpAuthHeader.Parameterized(
            STATIC_AUTHENTICATION_SCHEME,
            mapOf(HttpAuthHeader.Parameters.Realm to requireNotNull(config.realm))
        )
    )

    private fun AuthenticationContext.setPrincipal() {
        val principal = object : Principal {}

        this.principal(principal)
    }

    /**
     * [StaticAuthenticationProvider] configuration.
     */
    public class Config internal constructor(name: String?) : AuthenticationProvider.Config(name) {

        internal lateinit var externalConfig: ApplicationConfig

        public var token: String? = null
            get() = field ?: externalConfig.property(STATIC_AUTHENTICATION_TOKEN_PROPERTY).getString()

        public var realm: String? = null
            get() = field ?: externalConfig.propertyOrNull(STATIC_AUTHENTICATION_REALM_PROPERTY)?.getString()
            ?: DEFAULT_STATIC_AUTHENTICATION_REALM
    }
}

/**
 * Registers [StaticAuthenticationProvider] with optional name.
 *
 * @param name optional name to use.
 */
public fun AuthenticationConfig.static(
    name: String? = null, configure: StaticAuthenticationProvider.Config.() -> Unit = {}
) {
    register(
        StaticAuthenticationProvider(
            StaticAuthenticationProvider.Config(name).apply(configure)
        )
    )
}