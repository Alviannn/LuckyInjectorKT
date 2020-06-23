package id.luckynetwork.alviann.injectorkt.bungee

import com.github.alviannn.sqlhelper.SQLBuilder
import com.github.alviannn.sqlhelper.SQLHelper
import id.luckynetwork.alviann.injectorkt.bungee.commands.MainCMD
import id.luckynetwork.alviann.injectorkt.loader.Loader
import id.luckynetwork.alviann.injectorkt.updater.Updater
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class BungeeInjector : Plugin() {

    companion object {

        @JvmStatic
        lateinit var instance: BungeeInjector

        @JvmStatic
        private val provider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)

        /**
         * loads the injector earlier
         */
        @JvmStatic
        fun loadEarly() {
            Loader.startInjecting(BungeeInjector::class.java)
            Loader.initConfig(BungeeInjector::class.java)
        }

        /**
         * gets the default SQLBuilder instance with the default values (host, port, username, and password)
         *
         * NOTE: this isn't finished yet as the database is empty and you need to fill it out alone like on the code below
         *
         * Example Usage
         * ```java
         * val builder = SpigotInjector.defaultSQLBuilder()
         * val helper = builder.setDatabase("the database name")
         *      .toSQL()
         * // other codes
         * ```
         */
        @JvmStatic
        fun getDefaultSQLBuilder(): SQLBuilder {
            Loader.initConfig(BungeeInjector::class.java)

            val configFile = Loader.CONFIG_FILE
            val config = provider.load(configFile)

            return SQLHelper.newBuilder(SQLHelper.Type.MYSQL)
                    .setHost(config.getString("sql.host"))
                    .setPort(config.getString("sql.port"))
                    .setUsername(config.getString("sql.username"))
                    .setPassword(config.getString("sql.password"))
                    .setHikari(true)
        }

    }

    private lateinit var updater: Updater

    var configFile: File? = null
    lateinit var config: Configuration

    override fun onEnable() {
        instance = this
        updater = Updater()

        loadEarly()

        Loader.initConfig(BungeeInjector::class.java)
        this.reloadConfig()

        // the auto update task
        proxy.scheduler.schedule(this, {
            if (!this.isAutoUpdate())
                return@schedule

            updater.fetchUpdate().whenComplete { result, error ->
                if (error != null) {
                    System.err.println(error.message)
                    return@whenComplete
                }

                if (!result)
                    return@whenComplete
                if (!updater.canUpdate())
                    return@whenComplete

                updater.initiateUpdate(instance.dataFolder.parentFile).join()
            }
        }, 1L, 30L, TimeUnit.SECONDS)

        proxy.pluginManager.registerCommand(this, MainCMD("luckyinjector"))
    }

    /**
     * checks if the auto-update feature is enabled,
     * by default it's `true`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun isAutoUpdate() = config.getBoolean("auto-update", true)

    /**
     * reloads the config instance
     */
    fun reloadConfig() {
        if (configFile == null)
            configFile = File(dataFolder, "config.yml")

        if (!configFile!!.exists())
            throw FileNotFoundException("Cannot find config.yml file!")

        config = provider.load(configFile)

        if (!config.contains("auto-update")) {
            config.set("auto-update", true)

            provider.save(config, configFile)
            config = provider.load(configFile)
        }
    }


}