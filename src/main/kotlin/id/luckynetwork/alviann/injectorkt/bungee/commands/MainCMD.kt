package id.luckynetwork.alviann.injectorkt.bungee.commands

import id.luckynetwork.alviann.injectorkt.bungee.BungeeInjector
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

class MainCMD(name: String) : Command(name) {

    @Suppress("DEPRECATION")
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender is ProxiedPlayer) {
            sender.sendMessage("§cThis command is only for console!")
            return
        }

        val plugin = BungeeInjector.instance

        when {
            args.isEmpty() -> {
                sender.sendMessage("§cUsage: /luckyinjector reload)")
            }
            args[0].equals("reload", true) -> {
                plugin.reloadConfig()
                sender.sendMessage("§aSuccessfully reloaded config file!")
            }
            else -> {
                sender.sendMessage("§cUsage: /luckyinjector reload")
            }
        }
    }

}