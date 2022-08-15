/*
 * Copyright 2022 Mikhail Titov and other contributors
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

package dev.d1s.ktor.auth

import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*

private const val STATIC_AUTHORIZATION_SCHEME = "Static"
private const val STATIC_AUTHORIZATION_PREFIX = "$STATIC_AUTHORIZATION_SCHEME "
private const val STATIC_AUTHORIZATION_CHALLENGE_KEY = "StaticAuth"
private const val STATIC_AUTHORIZATION_TOKEN_PROPERTY = "static-auth.token"
private const val STATIC_AUTHORIZATION_REALM_PROPERTY = "static-auth.realm"
private const val DEFAULT_STATIC_AUTHORIZATION_REALM = "Ktor Server"

public class StaticAuthenticationProvider(private val config: Config) : AuthenticationProvider(config) {

    private lateinit var configuredToken: String

    private lateinit var configuredRealm: String

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val appConfig = context.call.application.environment.config

        if (!::configuredToken.isInitialized) {
            configuredToken = config.token ?: appConfig.property(
                STATIC_AUTHORIZATION_TOKEN_PROPERTY
            ).getString()
        }

        if (!::configuredRealm.isInitialized) {
            configuredRealm = config.realm ?: appConfig.propertyOrNull(
                STATIC_AUTHORIZATION_REALM_PROPERTY
            )?.getString() ?: DEFAULT_STATIC_AUTHORIZATION_REALM
        }

        context.call.parseToken()?.let { parsedToken ->
            if (parsedToken == configuredToken) {
                context.principal(object : Principal {})
                return
            }
        }

        context.challenge(
            STATIC_AUTHORIZATION_CHALLENGE_KEY, AuthenticationFailedCause.InvalidCredentials
        ) { challenge, call ->
            call.respond(
                UnauthorizedResponse(
                    HttpAuthHeader.Parameterized(
                        STATIC_AUTHORIZATION_SCHEME, mapOf(HttpAuthHeader.Parameters.Realm to configuredRealm)
                    )
                )
            )

            challenge.complete()
        }
    }

    private fun ApplicationCall.parseToken() =
        this.request.header(HttpHeaders.Authorization)?.removePrefix(STATIC_AUTHORIZATION_PREFIX)

    public class Config internal constructor(name: String?) : AuthenticationProvider.Config(name) {

        public var token: String? = null

        public var realm: String? = null
    }
}

public fun AuthenticationConfig.static(
    name: String? = null, configure: StaticAuthenticationProvider.Config.() -> Unit = {}
) {
    register(
        StaticAuthenticationProvider(
            StaticAuthenticationProvider.Config(name).apply(configure)
        )
    )
}