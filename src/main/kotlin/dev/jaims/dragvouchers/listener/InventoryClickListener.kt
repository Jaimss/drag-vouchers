package dev.jaims.dragvouchers.listener

import dev.jaims.dragvouchers.DragVouchers
import dev.jaims.dragvouchers.manager.voucherNamespaceKey
import dev.jaims.mcutils.bukkit.log
import dev.jaims.mcutils.bukkit.send
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import javax.print.attribute.standard.Severity

class InventoryClickListener(private val plugin: DragVouchers) : Listener {

    /**
     * Handle the main features of the plugin
     *
     * the bulk of the plugin is contained in here
     */
    @EventHandler
    fun InventoryClickEvent.onClick() {
        val player = whoClicked as? Player ?: return

        // they have to have a carried item and drop it on an item in the inventory
        val carriedItem = cursor ?: return
        val clickedItem = currentItem ?: return

        val clickedItemMeta = clickedItem.itemMeta ?: return

        // the carried item needs to have the persistent data key we gave
        val carriedItemMeta = carriedItem.itemMeta ?: return
        val carriedItemDataContainer = carriedItemMeta.persistentDataContainer
        if (!carriedItemDataContainer.has(voucherNamespaceKey, PersistentDataType.STRING)) return
        // now we get the voucher name from that persistent data container
        val voucherName = carriedItemDataContainer.get(voucherNamespaceKey, PersistentDataType.STRING)

        // get the configuration section of the voucher name
        val voucherConfigSection = plugin.config.getConfigurationSection("vouchers.${voucherName}") ?: run {
            plugin.log("A voucher with $voucherName for a name was clicked and is not in the config.", Severity.WARNING)
            return
        }

        // cancel the event when they click on an item
        isCancelled = true

        // get each sub section for requirements
        val requirementsSection = voucherConfigSection.getConfigurationSection("requirements") ?: return

        // permission requirement
        val permission = requirementsSection.getString("permission") ?: ""
        if (permission != "" && permission != "none") {
            if (!player.hasPermission(permission)) {
                val noPermMessage = plugin.config.getString("lang.no-permission") ?: "&cYou do not have permission!"
                player.send(noPermMessage)
                return
            }
        }

        // item type list requirement
        val allowedItems = requirementsSection.getStringList("item-type")
        // if the type is in the list, we are good to go
        if (allowedItems.isNotEmpty()) {
            if (!allowedItems.map { it.toLowerCase() }.contains(clickedItem.type.key.key.toLowerCase())) return
        }

        // enchantment requirements list
        val requiredEnchantments = requirementsSection.getStringList("enchantments").associate {
            if (!it.contains(":")) {
                null to null
            }
            Enchantment.getByKey(
                NamespacedKey.minecraft(
                    it.split(":").firstOrNull()?.toLowerCase() ?: "null"
                )
            ) to it.split(":").getOrNull(1)?.toIntOrNull()
        }
        // loop through all the requirements to see if it has one of the required enchantments
        // if we have one of the required enchantments, we are good to go
        var hasOneEnchantment = requiredEnchantments.isEmpty()
        val enchantments = clickedItem.enchantments
        for ((ench, level) in requiredEnchantments) {
            if (ench == null || level == null) continue
            if (enchantments[ench] == level) hasOneEnchantment = true
        }
        if (!hasOneEnchantment) return

        // at this point, we have met all requirements
        //
        // we now move on to handling the actual item
        player.setItemOnCursor(null)

        val dataConfigSection = voucherConfigSection.getConfigurationSection("data") ?: run {
            plugin.log(
                "The data section couldn't be found for voucher $voucherName. This means nothing is happening when it is used!",
                Severity.WARNING
            )
            return
        }
        val changeToMaterialName = dataConfigSection.getString("item-type") ?: "none"
        if (changeToMaterialName != "" && changeToMaterialName != "none") {
            val changeToMaterial = Material.matchMaterial(changeToMaterialName) ?: run {
                plugin.log("The material couldn't be found for $changeToMaterialName.", Severity.WARNING)
                return
            }
            clickedItem.type = changeToMaterial
        }

        // add enchantments to the item
        val enchantmentsToAdd = dataConfigSection.getStringList("enchant").associate {
            if (!it.contains(":")) {
                null to null
            }
            Enchantment.getByKey(
                NamespacedKey.minecraft(
                    it.split(":").firstOrNull()?.toLowerCase() ?: ""
                )
            ) to it.split(":").getOrNull(1)?.toIntOrNull()
        }
        enchantmentsToAdd.forEach { (ench, level) ->
            if (ench != null && level != null) {
                clickedItemMeta.addEnchant(ench, level, true)
            }
        }

        // change item durability
        val durabilityModification = dataConfigSection.getString("durability") ?: "none"
        if (durabilityModification != "none" && durabilityModification != "") {
            val operatorString = durabilityModification[0].toString()
            val amt = durabilityModification.drop(1).toIntOrNull() ?: run {
                plugin.log("The durability for $voucherName is not an integer!", Severity.ERROR)
                return
            }

            val damageableClickedItemMeta = (clickedItemMeta as Damageable)

            when (operatorString) {
                "+" -> damageableClickedItemMeta.damage = damageableClickedItemMeta.damage + amt
                "-" -> damageableClickedItemMeta.damage = damageableClickedItemMeta.damage - amt
                "=" -> damageableClickedItemMeta.damage = amt
            }
        }

        // run the commands in the list
        val playerCommands = dataConfigSection.getStringList("commands.player")
        val consoleCommands =
            dataConfigSection.getStringList("commands.console").map { it.replace("{player}", player.name) }
        playerCommands.forEach { player.performCommand(it) }
        consoleCommands.forEach { Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), it) }

        clickedItem.itemMeta = clickedItemMeta
    }
}