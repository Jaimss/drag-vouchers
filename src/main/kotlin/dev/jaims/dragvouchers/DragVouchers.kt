package dev.jaims.dragvouchers

import dev.jaims.dragvouchers.listener.InventoryClickListener
import dev.jaims.dragvouchers.manager.FileManager
import dev.jaims.dragvouchers.manager.voucherNamespaceKey
import dev.jaims.mcutils.bukkit.log
import dev.jaims.mcutils.bukkit.register
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin


class DragVouchers : JavaPlugin() {

    /**
     * Plugin startup logic
     */
    override fun onEnable() {
        log("&aEnabling DragVouchers @@version@@.")

        registerManagers()
        registerCommands()
        registerListeners()

        log("&aEnabled DragVouchers @@version@@!")
    }

    /**
     * plugin shutdown logic
     */
    override fun onDisable() {
        log("&cDisabling DragVouchers @@version@@.")

        log("&CDisabled DragVouchers @@version@@!")
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
        register(
            InventoryClickListener(this)
        )
    }

    /**
     * Register the commands for the plugin
     */
    private fun registerCommands() {

    }

}

