package dev.jaims.dragvouchers

import dev.jaims.dragvouchers.command.DragVoucherCommand
import dev.jaims.dragvouchers.listener.InventoryClickListener
import dev.jaims.dragvouchers.manager.FileManager
import dev.jaims.dragvouchers.manager.voucherNamespaceKey
import dev.jaims.mcutils.bukkit.log
import me.mattstudios.mf.base.CommandManager
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin


class DragVouchers : JavaPlugin() {

    /**
     * Plugin startup logic
     */
    override fun onEnable() {
        log("&aEnabling DragVouchers 1.0.1.")

        registerManagers()
        registerCommands()
        registerListeners()

        log("&aEnabled DragVouchers 1.0.1!")
    }

    /**
     * plugin shutdown logic
     */
    override fun onDisable() {
        log("&cDisabling DragVouchers 1.0.1.")

        log("&CDisabled DragVouchers 1.0.1!")
    }

    /**
     * Register the Plugin Managers
     */
    private fun registerManagers() {
        FileManager(this)
        voucherNamespaceKey = NamespacedKey(this, "dragvoucher")
    }

    /**
     * Register the listeners for the plugin
     */
    private fun registerListeners() {
        server.pluginManager.registerEvents(InventoryClickListener(this), this)
        log("registered listeners")
    }

    /**
     * Register the commands for the plugin
     */
    private fun registerCommands() {
        val cmdManager = CommandManager(this)
        cmdManager.register(
            DragVoucherCommand(this)
        )
    }

}

