package id.luckynetwork.alviann.injectorkt.loader

import com.github.alviannn.lib.dependencyhelper.DependencyHelper
import com.github.alviannn.sqlhelper.utils.Closer
import java.io.File
import java.nio.file.Files

object Loader {

    @JvmStatic
    val DATA_FOLDER = File("plugins", "LuckyInjectorKT")

    @JvmStatic
    val CONFIG_FILE = File(DATA_FOLDER, "config.yml")

    /**
     * injects the SQL Helper to server
     *
     * @param clazz the main class
     */
    fun startInjecting(clazz: Class<*>) {
        val helper = DependencyHelper(clazz)

        DATA_FOLDER.mkdirs()

        val dirPath = DATA_FOLDER.toPath()
        val dependencies = mapOf(
                Pair("SQLHelper-2.5.5.jar", "https://github.com/Alviannn/SQLHelper/releases/download/2.5.5/SQLHelper-2.5.5.jar"),
                Pair("relocated-netty-all-4.1.50.jar", "https://github.com/Lucky-Development-Department/Jar-Repository/raw/master/repo/netty/relocated-netty-all-4.1.50.jar")
        )

        helper.download(dependencies, dirPath)
        helper.load(dependencies, dirPath)
    }

    /**
     * initializes the config file to be used by the injector
     *
     * @param clazz the main class
     */
    fun initConfig(clazz: Class<*>) {
        DATA_FOLDER.mkdirs()
        if (!CONFIG_FILE.exists())
            return

        Closer().use {
            val resource = it.add(clazz.classLoader.getResourceAsStream("config.yml"))
                    ?: throw NullPointerException("Cannot find config.yml file!")

            Files.copy(resource, CONFIG_FILE.toPath())
        }
    }

}