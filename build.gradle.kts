import org.gradle.api.tasks.WriteProperties
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import java.net.URI

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

group = providers.gradleProperty("pluginGroup").get()
version = System.getenv("LD_VERSION") ?: providers.gradleProperty("pluginVersion").get()
val enableDevelopmentTools = providers.gradleProperty("enableDevelopmentTools")
    .map(String::toBoolean)
    .orElse(false)
val platformType = providers.gradleProperty("platformType")
val platformVersion = providers.gradleProperty("platformVersion")
val pluginName = providers.gradleProperty("pluginName")
val pluginDescription = providers.gradleProperty("pluginDescription")
val pluginSinceBuild = providers.gradleProperty("pluginSinceBuild")
val pluginRepositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
val gradleWrapperVersion = providers.gradleProperty("gradleVersion")
val pluginVerifierCliPath = providers.environmentVariable("PLUGIN_VERIFIER_CLI_PATH")
    .orNull
    ?.takeIf(String::isNotBlank)
    ?.let(::file)

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
    named("main") {
        resources.exclude("META-INF/plugin.xml")
        resources.srcDir(layout.buildDirectory.dir("generated/plugin-descriptor"))
        resources.srcDir(layout.buildDirectory.dir("generated/build-properties"))
    }
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(libs.lc.sdk)
    implementation(libs.fastjson.legacy)
    implementation(libs.jsoup)
    implementation(libs.sentry)
    implementation(libs.commons.lang3)
    implementation(libs.flexmark)
    implementation(libs.flexmark.ext.attributes)
    implementation(libs.tiny.pinyin)

    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)

    val integrationTestImplementation = configurations.getByName("integrationTestImplementation")
    integrationTestImplementation(libs.junit.jupiter)
    integrationTestImplementation(libs.kodein)
    integrationTestImplementation(libs.kotlinx.coroutines)
    integrationTestImplementation(libs.kotlin.stdlib)
    add("integrationTestRuntimeOnly", libs.teamcity.service.messages)

    intellijPlatform {
        create(platformType, platformVersion)
        bundledPlugins("com.intellij.modules.jcef")
        testFramework(TestFrameworkType.Starter, configurationName = "integrationTestImplementation")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = pluginName
        version = project.version.toString()

        description = providers.fileContents(
            layout.projectDirectory.file(pluginDescription.get())
        ).asText.get()

        val changelog = project.changelog
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
            sinceBuild = pluginSinceBuild
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = provider {
            listOf(project.version.toString().substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }

    pluginVerification {
        pluginVerifierCliPath?.let(cliPath::set)
        ides {
            recommended()
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = pluginRepositoryUrl
}


tasks {
    val generateBuildProperties = register<WriteProperties>("generateBuildProperties") {
        destinationFile.set(
            layout.buildDirectory.file("generated/build-properties/META-INF/leetcode-editor-build.properties")
        )
        property("development.tools.enabled", enableDevelopmentTools)
    }

    val integrationTestPathingJar = register<Jar>("integrationTestPathingJar") {
        group = "verification"
        description = "Creates a manifest-only classpath JAR for IDEA Driver UI tests."
        dependsOn("integrationTestClasses")
        destinationDirectory.set(layout.buildDirectory.dir("integration-test/classpath"))
        archiveFileName.set("integration-test-pathing.jar")
        doNotTrackState("The IntelliJ runtime classpath is represented by the generated manifest.")
        manifest {
            attributes(
                "Class-Path" to provider {
                    sourceSets.getByName("integrationTest").runtimeClasspath.files
                        .map { it.canonicalFile.toURI().normalize() }
                        .distinct()
                        .joinToString(" ", transform = URI::toASCIIString)
                }
            )
        }
    }

    val generatedPluginDescriptor = layout.buildDirectory.file("generated/plugin-descriptor/META-INF/plugin.xml")
    val renderPluginDescriptor = register("renderPluginDescriptor") {
        group = "build setup"
        description = "Combines the product descriptor and action registrations into plugin.xml."
        val productTemplate = layout.projectDirectory.file(
            providers.gradleProperty("pluginDescriptorProductFile")
                .getOrElse("src/main/plugin-descriptor/product.xml")
        )
        val actionsFragment = layout.projectDirectory.file(
            providers.gradleProperty("pluginDescriptorActionsFile")
                .getOrElse("src/main/plugin-descriptor/actions.xml")
        )
        inputs.files(productTemplate, actionsFragment)
        outputs.file(generatedPluginDescriptor)
        doLast {
            val marker = "    <!-- @ACTIONS@ -->"
            val product = productTemplate.asFile.readText()
            val actions = actionsFragment.asFile.readText()

            check(product.lineSequence().count { it == marker } == 1) {
                "Expected exactly one actions marker in ${productTemplate.asFile}"
            }
            check(actions.trimStart().startsWith("<actions>") && actions.trimEnd().endsWith("</actions>")) {
                "Actions fragment must contain exactly one <actions> section: ${actionsFragment.asFile}"
            }
            check(!actions.contains("<idea-plugin")) {
                "Actions fragment must not contain an idea-plugin root: ${actionsFragment.asFile}"
            }

            val rendered = product.replace(marker, actions)
            check(
                Regex("<idea-plugin").findAll(rendered).count() == 1
                        && Regex("<actions>").findAll(rendered).count() == 1
            ) {
                "Generated plugin descriptor has an invalid root or actions section"
            }

            generatedPluginDescriptor.get().asFile.apply {
                parentFile.mkdirs()
                writeText(rendered)
            }
        }
    }

    named<PatchPluginXmlTask>("patchPluginXml") {
        dependsOn(renderPluginDescriptor)
        inputFile.set(generatedPluginDescriptor)
    }

    named("processResources") {
        dependsOn(renderPluginDescriptor, generateBuildProperties)
    }

    named<RunIdeTask>("runIde") {
        systemProperty("leetcode.development.tools", enableDevelopmentTools.get().toString())
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    wrapper {
        gradleVersion = gradleWrapperVersion.get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    named("buildSearchableOptions") {
        enabled = false
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
                classpath = files(tasks.named<Jar>("integrationTestPathingJar").flatMap {
                    it.archiveFile
                })
                dependsOn(tasks.named("integrationTestPathingJar"))
                dependsOn(tasks.buildPlugin)
                systemProperty("path.to.build.plugin", tasks.buildPlugin.flatMap { it.archiveFile })
                systemProperty("leetcode.test.wait.timeout.seconds", "180")
                maxParallelForks = 1
                jvmArgs("--enable-native-access=ALL-UNNAMED")
                useJUnitPlatform {
                    excludeEngines("junit-vintage")
                }
            }
        }
    }
}
