package id.luckynetwork.alviann.injectorkt.spigot.commands

import id.luckynetwork.alviann.injectorkt.spigot.SpigotInjector
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MainCMD : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command?, label: String?, args: Array<out String>): Boolean {
        if (sender is Player) {
            sender.sendMessage("§cThis command is only for console!")
            return true
        }

        val plugin = SpigotInjector.instance

        when {
            args.isEmpty() -> {
                sender.sendMessage("§cUsage: /luckyinjector reload")
            }
            args[0].equals("reload", true) -> {
                plugin.reloadConfig()
                sender.sendMessage("§aSuccessfully reloaded config file!")
            }
            else -> {
                sender.sendMessage("§cUsage: /luckyinjector reload")
            }
        }

        return true
    }


}