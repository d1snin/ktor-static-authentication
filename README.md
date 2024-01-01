[![](https://maven.d1s.dev/api/badge/latest/releases/dev/d1s/ktor-static-authentication?color=40c14a&name=maven.d1s.dev&prefix=v)](https://github.com/d1snin/ktor-static-authentication)

### Static authentication for Ktor

This library provides `StaticTokenAuthenticationProvider` for Ktor that can be used to authenticate requests using
pre-configured static token.
Token must be passed within the request headers as follows: `Authorization: Bearer <static_token>`

### Installation

```kotlin
repositories {
    maven(url = "https://maven.d1s.dev/releases")
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
        staticToken(name = "static-token") {
            token = "example_token"
            realm = "example_realm"
        }
    }
}
```

### How to contribute

See [CONTRIBUTING.md](./CONTRIBUTING.md)

### Code of Conduct

See [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

### License

```
Copyright 2022-2024 Mikhail Titov

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