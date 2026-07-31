import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

group = providers.gradleProperty("pluginGroup").get()
version = System.getenv("LD_VERSION") ?: providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

// Configure project's dependencies
repositories {
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    maven("https://jitpack.io")
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    implementation("com.shuzijun:lc-sdk:0.0.3")
    implementation("com.alibaba:fastjson:1.2.47")
    implementation("org.jsoup:jsoup:1.11.3")
    implementation("io.sentry:sentry:1.7.9") {
        exclude(module = "slf4j-api")
    }
    implementation("org.scilab.forge:jlatexmath:1.0.7")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("commons-lang:commons-lang:2.6")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("com.vladsch.flexmark:flexmark:0.62.2")
    implementation("com.vladsch.flexmark:flexmark-ext-attributes:0.62.2")
    implementation("io.github.biezhi:TinyPinyin:2.0.3.RELEASE")
    // api(fileTree(mapOf("dir" to "src/main/resources/lib", "include" to listOf("*.jar"))))

    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)

    val integrationTestImplementation = configurations.getByName("integrationTestImplementation")
    integrationTestImplementation(libs.junit.jupiter)
    integrationTestImplementation(libs.kodein)
    integrationTestImplementation(libs.kotlinx.coroutines)
    integrationTestImplementation(libs.kotlin.stdlib)
    add("integrationTestRuntimeOnly", libs.teamcity.service.messages)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        bundledPlugins("com.intellij.modules.jcef")
        testFramework(TestFrameworkType.Starter, configurationName = "integrationTestImplementation")

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        //bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        //plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        // Module Dependencies. Uses `platformBundledModules` property from the gradle.properties file for bundled IntelliJ Platform modules.
        //bundledModules(providers.gradleProperty("platformBundledModules").map { it.split(',') })

        testFramework(TestFrameworkType.Platform)
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = project.version.toString()

        description = providers.fileContents(
            layout.projectDirectory.file(providers.gradleProperty("pluginDescription").get())
        ).asText.get()

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = with(changelog) {
            val currentVersion = project.version.toString()
            val stableVersion = currentVersion.substringBefore('-')
            renderItem(
                (getOrNull(currentVersion)
                    ?: getOrNull("$currentVersion.0")
                    ?: getOrNull(stableVersion)
                    ?: getOrNull("$stableVersion.0")
                    ?: getUnreleased())
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML,
            )
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = provider {
            listOf(project.version.toString().substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}


tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    register("qualityGate") {
        group = "verification"
        description = "Runs plugin validation, unit tests, and IDEA Driver UI integration tests."
        dependsOn(
            verifyPluginProjectConfiguration,
            verifyPluginStructure,
            verifyPlugin,
            test,
            named("integrationTest"),
        )
    }
}

intellijPlatformTesting {
    testIdeUi {
        register("integrationTest") {
            task {
                val integrationTestSourceSet = sourceSets.getByName("integrationTest")
                testClassesDirs = integrationTestSourceSet.output.classesDirs
                classpath = integrationTestSourceSet.runtimeClasspath
                dependsOn(tasks.buildPlugin)
                systemProperty("path.to.build.plugin", tasks.buildPlugin.flatMap { it.archiveFile })
                useJUnitPlatform {
                    excludeEngines("junit-vintage")
                }
            }
        }
    }
}
