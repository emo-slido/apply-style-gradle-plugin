/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.automattic.android.applystyle

import org.gradle.api.Project
import org.gradle.api.Plugin

import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.plugins.quality.CodeQualityExtension
import java.io.File
import java.io.IOException
import java.nio.file.Path

/**
 * A custom plugin that standardizes the styles across various projects.
 *
 * For now the plugin deals only with the configuration of Detekt.
 */
class ApplyStylePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("extractDetektConf") { task ->
            task.doLast {
                val detektExtension = project.getDetektExtension() as DetektExtension
                val file = detektExtension.config.singleFile ?: path(project.buildDir,"detekt.yml")
                project.extractDetektConf(file, true)
                println("Wrote detekt configuration file to $file")
            }
        }

        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello from plugin '$PLUGIN_NAME'")
            }
        }

        project.plugins.withId("kotlin-android") {
            project.applyDetektPlugin()
        }
    }

    /**
     * Applies the detekt plugin to the project and configures the project properly.
     */
    private fun Project.applyDetektPlugin() {
        debug("==> applying detekt plugin")
        project.pluginManager.apply(DetektPlugin::class.java)

        project.applyDetektDefaults()
        project.afterEvaluate {
            project.writeDetektConf()
        }
    }

    /**
     * Applies our detekt default settings.
     */
    private fun Project.applyDetektDefaults() {
        val detektExtension = project.getDetektExtension() as DetektExtension
        with (detektExtension) {
            autoCorrect = false
            buildUponDefaultConfig = false
            disableDefaultRuleSets = false
            ignoreFailures = false
            parallel = false
            debug = false
            reports.html.enabled = true
            reports.xml.enabled = true
            reports.txt.enabled = true

            // Check if the project has a custom baseline under `${project.rootDir}/config/detekt/baseline.xml`
            // NOTE: it would be better if each project would have its own baseline but for now wordpress has a global
            //       baseline and for the sake of simplicity we will use that one for now.
            if (baseline == null) {
                val baseLineFile = path(project.rootDir,"config", "detekt", "baseline.xml")
                if (baseLineFile.exists()) {
                    debug("==> using detekt baseline $baseLineFile")
                    baseline = baseLineFile
                }
            }
        }
    }

    /**
     * Returns the Detekt extension instance (the gradle config for the plugin).
     * @return the Detekt extension masked as a CodeQualityExtension
     */
    private fun Project.getDetektExtension(): CodeQualityExtension {
        // This is a hack but we can't declare the function to return DetektExtension because when we run the tests
        // we don't have the detekt plugin loaded! If we change the return type to DetektExtension then the tests
        // crash with a runtime exception:
        //   Caused by: org.gradle.internal.instantiation.ClassGenerationException: Could not generate a decorated class for type ApplyStylePlugin.
        //   Caused by: java.lang.NoClassDefFoundError: io/gitlab/arturbosch/detekt/extensions/DetektExtension
        //   Caused by: java.lang.ClassNotFoundException: io.gitlab.arturbosch.detekt.extensions.DetektExtension
        return project.extensions.getByType(DetektExtension::class.java)
    }

    /**
     * Writes the detekt configuration file.
     */
    private fun Project.writeDetektConf() {
        val detektExtension = project.getDetektExtension() as DetektExtension
        if (!detektExtension.config.isEmpty) {
            // The project is explicit about a detekt configuration file and has requested to use its own
            debug("==> project uses custom detekt configuration file ${detektExtension.config.singleFile}")
            return
        }

        // The gradle config is missing a `detekt.config` directive we will use our built-in file instead
        val file = path(project.buildDir,"detekt.yml")
        if (!file.exists()) {
            // Extract the config file as it doesn't exist
            project.extractDetektConf(file,false)
        }

        // Request to use our built-in file
        detektExtension.config = project.files(file)
        debug("==> configured detekt to use config $file")
    }

    /**
     * Extracts the detekt configuration file from the plugin's resources into a file on disk.
     * @param file the configuration file to write
     * @param force indicates if the configuration file needs to be overwritten
     * @return the file written
     */
    private fun Project.extractDetektConf(file: File, force: Boolean) {
        if (!force && file.exists()) {
            debug("==> reusing detekt config file $file")
            return
        }

        file.parentFile.mkdirs()
        file.createNewFile()
        val resource = ApplyStylePlugin::class.java.getResource("/detekt.yml")
            ?: throw IOException("Couldn't find $PLUGIN_NAME built-in detekt config file")
        file.writeText(resource.readText())
        debug("==> wrote detekt config $file")
    }

    /**
     * Builds a path to a file in the given project in a system dependent way by using the platform's path separator.
     * @param folder the base folder
     * @param rest the other parts of the folder's path
     * @return a file reference pointing to the folder path described (the folder might not exist)
     */
    private fun path(folder: File, vararg rest: String): File {
        return Path.of(folder.toString(), *rest).toFile()
    }

    /**
     * Custom debug function that gets activated when the environment variable `A8C_DEBUG=1` is set.
     * @param message the message to print
     */
    private fun debug(message: Any?) {
        val debugEnv = System.getenv("A8C_DEBUG") ?: "0"
        if (debugEnv == "0" || debugEnv == "" || debugEnv.equals("false", ignoreCase = true)) {
            return
        }
        println(message)
    }

    companion object {
        const val PLUGIN_NAME = "a8c-apply-style-gradle-plugin"
    }
}
