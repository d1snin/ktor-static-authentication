[![](https://jitpack.io/v/dev.d1s/ktor-static-authentication.svg)](https://jitpack.io/#dev.d1s/ktor-static-authentication)

### Static authentication for Ktor

This library provides `StaticAuthenticationProvider` for Ktor that can be used to authenticate requests using
the `Static` schema and the corresponding static token.

### Installation

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    val ktorStaticAuthVersion: String by project

    implementation("dev.d1s:ktor-static-authentication:$ktorStaticAuthVersion")
}
```

### Usage

```kotlin
fun Application.configureSecurity() {
    authentication {
        // name is optional
        static(name = "static") {
            // also could be configured through the `static-auth.token` property
            token = "example_token"
            // also could be configured through the `static-auth.realm` property. Default realm is "Ktor Server"
            realm = "example_realm"
        }
    }
}
```