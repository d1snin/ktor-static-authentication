/*
 * Copyright 2022-2023 Mikhail Titov
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

private const val STATIC_AUTHENTICATION_CHALLENGE_KEY = "StaticTokenAuth"

private const val STATIC_AUTHENTICATION_TOKEN_PROPERTY = "static-auth.token"
private const val STATIC_AUTHENTICATION_REALM_PROPERTY = "static-auth.realm"
private const val DEFAULT_STATIC_AUTHENTICATION_REALM = "Ktor Server"

/**
 * [AuthenticationProvider] that provides Static authorization support.
 */
public class StaticTokenAuthenticationProvider(private val config: Config) : AuthenticationProvider(config) {

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val result = context.tryAuthenticateCall()

        if (result.isFailure) {
            context.sendChallengeOnFailure()
        }
    }

    private fun AuthenticationContext.tryAuthenticateCall() = runCatching {
        call.parseToken()?.let { parsedToken ->
            if (parsedToken == config.requiredToken) {
                setPrincipal()
            } else {
                error("Invalid token provided.")
            }
        }
    }

    private fun AuthenticationContext.sendChallengeOnFailure() {
        challenge(
            STATIC_AUTHENTICATION_CHALLENGE_KEY,
            AuthenticationFailedCause.InvalidCredentials
        ) { challenge, call ->
            val response = newUnauthorizedResponse()

            call.respond(response)

            challenge.complete()
        }
    }

    private fun ApplicationCall.parseToken() =
        this.request.header(HttpHeaders.Authorization)?.removePrefix("${AuthScheme.Bearer} ")

    private fun newUnauthorizedResponse() = UnauthorizedResponse(
        HttpAuthHeader.Parameterized(
            AuthScheme.Bearer,
            mapOf(HttpAuthHeader.Parameters.Realm to requireNotNull(config.realm))
        )
    )

    private fun AuthenticationContext.setPrincipal() {
        val principal = object : Principal {}

        this.principal(principal)
    }

    /**
     * [StaticTokenAuthenticationProvider] configuration.
     */
    public class Config internal constructor(name: String?) : AuthenticationProvider.Config(name) {

        public var token: String? = null

        public var realm: String = DEFAULT_STATIC_AUTHENTICATION_REALM

        internal val requiredToken = requireNotNull(token) {
            "Token is not set"
        }

        internal fun validate() {
            requiredToken
        }
    }
}

/***
 * Retrieves `static-auth.token` from [ApplicationConfig].
 */
public val ApplicationConfig.staticAuthToken: String get() = property(STATIC_AUTHENTICATION_TOKEN_PROPERTY).getString()

/***
 * Retrieves `static-auth.realm` from [ApplicationConfig].
 */
public val ApplicationConfig.staticAuthRealm: String get() = property(STATIC_AUTHENTICATION_REALM_PROPERTY).getString()

/**
 * Registers [StaticTokenAuthenticationProvider] with optional name.
 */
public fun AuthenticationConfig.staticToken(
    name: String? = null, configure: StaticTokenAuthenticationProvider.Config.() -> Unit = {}
) {
    val config = StaticTokenAuthenticationProvider.Config(name).apply(configure).apply {
        validate()
    }

    val provider = StaticTokenAuthenticationProvider(config)

    register(provider)
}