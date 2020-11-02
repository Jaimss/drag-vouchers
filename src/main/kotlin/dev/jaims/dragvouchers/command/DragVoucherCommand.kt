package dev.jaims.dragvouchers.command

import dev.jaims.dragvouchers.DragVouchers
import dev.jaims.dragvouchers.manager.buildVoucherItem
import dev.jaims.mcutils.bukkit.send
import me.mattstudios.mf.annotations.Alias
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.base.CommandBase
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * The Drag Vouchers command
 * handles every part of the plugin
 */
@Command("dragvouchers")
@Alias("dv")
class DragVoucherCommand(private val plugin: DragVouchers) : CommandBase() {

    /**
     * Send the command usage to a [sender]
     */
    private fun sendUsage(sender: CommandSender) {
        sender.send(plugin.config.getStringList("lang.command.dv.usage"))
    }

    @Default
    fun default(sender: CommandSender) {
        sendUsage(sender)
    }

    /**
     * Reload the config file
     */
    @SubCommand("reload")
    fun reload(sender: CommandSender) {
        if (!sender.hasPermission("dragvouchers.reload")) {
            sender.send(plugin.config.getString("lang.no-permission") ?: "&cYou do not have permission!")
            return
        }
        plugin.reloadConfig()
    }

    /**
     * Give a voucher with [name] to a [targetName] name
     */
    @SubCommand("give")
    fun give(sender: CommandSender, name: String, targetName: String) {
        if (!sender.hasPermission("dragvouchers.give")) {
            sender.send(plugin.config.getString("lang.no-permission") ?: "&cYou do not have permission!")
            return
        }
        // get a target player from their name and send an error if they aren't found
        val target = Bukkit.getServer().getPlayer(targetName) ?: run {
            val noPlayerFoundMessage =
                plugin.config.getString("lang.no-player-found") ?: "&cNo player found with name {target}!"
            sender.send(noPlayerFoundMessage.replace("{target}", targetName))
            return
        }

        // get a voucher item from its name and send an error if it doesn't exist
        val voucherItem = buildVoucherItem(plugin, name) ?: run {
            val errorMessage = plugin.config.getString("lang.command.dv.no-voucher-found")
                ?: "&cNo voucher found with the name {name}!"
            sender.send(errorMessage.replace("{name}", name))
            return
        }

        val dropItems = target.inventory.addItem(voucherItem)
        dropItems.forEach { (_, i) -> target.location.world?.dropItemNaturally(target.location, i) }
        val giveSuccessMessage =
            plugin.config.getString("lang.command.dv.voucher-given") ?: "&aSuccessfully gave {target} a {name} voucher!"
        val voucherReceivedMessage =
            plugin.config.getString("lang.command.dv.voucher-received") ?: "&aYou received a {name} voucher!"
        sender.send(giveSuccessMessage.replace("{target}", targetName).replace("{name}", name))
        target.send(voucherReceivedMessage.replace("{name}", name))
    }

}