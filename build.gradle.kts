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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("com.github.ben-manes.versions")
}

val projectGroup: String by project
val projectVersion: String by project

group = projectGroup
version = projectVersion

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion: String by project

    api("io.ktor:ktor-server-auth:$ktorVersion")
}

publishing {
    repositories {
        fun mavenD1sDev(channel: String) {
            maven {
                name = "mavenD1sDevRepository$channel"
                url = uri("https://maven.d1s.dev/${channel.toLowerCaseAsciiOnly()}")

                credentials {
                    username = getEnvOrProperty("MAVEN_D1S_DEV_USERNAME", "mavenD1sDevUsername")
                    password = getEnvOrProperty("MAVEN_D1S_DEV_PASSWORD", "mavenD1sDevPassword")
                }
            }
        }

        mavenD1sDev("Releases")
        mavenD1sDev("Snapshots")
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }
}

tasks.withType<DokkaTask> {
    dokkaSourceSets {
        configureEach {
            val moduleDocsPath: String by project

            includes.setFrom(moduleDocsPath)
        }
    }
}

kotlin {
    explicitApi()
}

fun getEnvOrProperty(env: String, property: String): String? =
    System.getenv(env) ?: findProperty(property)?.toString()