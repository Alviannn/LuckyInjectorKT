package id.luckynetwork.alviann.injectorkt.spigot

import com.github.alviannn.sqlhelper.SQLBuilder
import com.github.alviannn.sqlhelper.SQLHelper
import id.luckynetwork.alviann.injectorkt.loader.Loader
import id.luckynetwork.alviann.injectorkt.spigot.commands.MainCMD
import id.luckynetwork.alviann.injectorkt.updater.Updater
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

class SpigotInjector : JavaPlugin() {

    companion object {

        @JvmStatic
        lateinit var instance: SpigotInjector

        /**
         * loads the injector earlier
         */
        @JvmStatic
        fun loadEarly() {
            Loader.startInjecting(SpigotInjector::class.java)
            Loader.initConfig(SpigotInjector::class.java)
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
            Loader.initConfig(SpigotInjector::class.java)

            val configFile = Loader.CONFIG_FILE
            val config = YamlConfiguration.loadConfiguration(configFile)

            return SQLHelper.newBuilder(SQLHelper.Type.MYSQL)
                    .setHost(config.getString("sql.host"))
                    .setPort(config.getString("sql.port"))
                    .setUsername(config.getString("sql.username"))
                    .setPassword(config.getString("sql.password"))
                    .setHikari(true)
        }

    }

    private lateinit var updater: Updater

    override fun onEnable() {
        instance = this
        updater = Updater()

        loadEarly()

        Loader.initConfig(SpigotInjector::class.java)
        this.reloadConfig()

        // the auto update task
        server.scheduler.runTaskTimer(this, {
            if (!this.isAutoUpdate())
                return@runTaskTimer

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
        }, 20L, 600L)

        this.getCommand("luckyinjector").executor = MainCMD()
    }

    /**
     * checks if the auto-update feature is enabled,
     * by default it's `true`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun isAutoUpdate() = config.getBoolean("auto-update", true)

    override fun reloadConfig() {
        super.reloadConfig()

        if (!config.contains("auto-update")) {
            config.set("auto-update", true)
            this.saveConfig()
            super.reloadConfig()
        }
    }

}