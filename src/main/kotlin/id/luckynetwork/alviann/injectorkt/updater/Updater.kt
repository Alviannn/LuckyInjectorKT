package id.luckynetwork.alviann.injectorkt.updater

import com.github.alviannn.sqlhelper.utils.Closer
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

@Suppress("MemberVisibilityCanBePrivate")
class Updater : AutoCloseable {

    private val threadPool = Executors.newCachedThreadPool()

    var latestVersion: String? = null
    var latestDownloadUrl: String? = null

    /**
     * fetches for a new LuckyInjectorKT update
     *
     * ```java
     * val updater = Updater()
     * try {
     *      val hasUpdate = updater.fetchUpdate().join();
     *      // other codes
     * } catch (e: Error) {
     *      e.printStackTrace()
     * }
     * ```
     */
    fun fetchUpdate(): CompletableFuture<Boolean> = CompletableFuture.supplyAsync(Supplier {
        val client = OkHttpClient.Builder()
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .callTimeout(30L, TimeUnit.SECONDS)
                .build()

        val request = Request.Builder()
                .url("https://raw.githubusercontent.com/Alviannn/LuckyInjectorKT/master/update-info.json")
                .header("User-Agent", "LuckyInjectorKT-Updater")
                .get()
                .build()

        var updateInfoFetched = false
        try {
            Closer().use {
                val response = it.add(client.newCall(request).execute())
                val body = it.add(response.body) ?: throw NullPointerException("Response body is null")

                val reader = it.add(body.charStream())
                val updateInfo = JsonParser.parseReader(reader).asJsonObject

                latestVersion = updateInfo.get("version").asString
                latestDownloadUrl = updateInfo.get("download-url").asString

                updateInfoFetched = true
            }
        } catch (e: Error) {
            e.printStackTrace()
        }

        return@Supplier updateInfoFetched
    }, threadPool)

    /**
     * checks if the plugin can update with the new version
     */
    fun canUpdate(): Boolean {
        val loader = this::class.java.classLoader
        var currentVersion: String? = null

        try {
            Closer().use {
                val stream = it.add(loader.getResourceAsStream("version.info"))
                        ?: throw FileNotFoundException("Cannot find version.info file!")

                val scanner = Scanner(stream)
                if (scanner.hasNext())
                    currentVersion = scanner.nextLine()
            }
        } catch (e: Error) {
            e.printStackTrace()
        }

        if (currentVersion == null)
            return true
        if (latestVersion == null)
            return false

        return !currentVersion.equals(latestVersion)
    }

    /**
     * starts updating the plugin
     */
    fun initiateUpdate(pluginsFolder: File): CompletableFuture<Void> = CompletableFuture.runAsync(Runnable {
        var updateSuccess = false

        try {
            if (latestDownloadUrl == null)
                throw NullPointerException("Latest download URL is null")

            Closer().use {
                val url = URL(latestDownloadUrl)
                val stream = url.openStream()
                        ?: throw NullPointerException("Cannot fetch update file!")

                val updatedFile = File(pluginsFolder, File(url.file).name)
                if (!updatedFile.exists()) {
                    // creates the plugin folder if needed
                    if (!pluginsFolder.exists())
                        pluginsFolder.mkdirs()

                    // downloads the file
                    Files.copy(stream, updatedFile.toPath())
                    updateSuccess = true
                }
            }
        } catch (e: Error) {
            e.printStackTrace()
        }

        if (!updateSuccess)
            return@Runnable

        val pluginFile = this.getCurrentPluginFile()
        if (pluginFile == null || !pluginFile.exists())
            return@Runnable

        // deletes the current plugin file once the file is successfully updated
        pluginFile.delete()
    }, threadPool)

    /**
     * gets the plugin jar file
     */
    private fun getCurrentPluginFile(): File? {
        try {
            return File(Updater::class.java.protectionDomain.codeSource.location.toURI())
        } catch (e: Error) {
        }
        return null
    }

    override fun close() {
        threadPool.shutdownNow()
    }

}