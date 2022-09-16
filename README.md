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

### License

```
   Copyright 2022 Mikhail Titov and other contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```