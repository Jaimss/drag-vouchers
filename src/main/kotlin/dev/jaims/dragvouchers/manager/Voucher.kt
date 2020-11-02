package dev.jaims.dragvouchers.manager

import dev.jaims.dragvouchers.DragVouchers
import dev.jaims.mcutils.bukkit.item.createItem
import dev.jaims.mcutils.bukkit.item.meta
import dev.jaims.mcutils.bukkit.item.name
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

lateinit var voucherNamespaceKey: NamespacedKey

/**
 * Get an item stack of a voucher from the item name
 * return the item stack or null
 *      will be null if the vouchers config doesnt exist or the voucher name doesn't exist
 */
fun buildVoucherItem(plugin: DragVouchers, name: String): ItemStack? {
    val voucherSection = plugin.config.getConfigurationSection("vouchers") ?: return null
    val itemSection = voucherSection.getConfigurationSection(name) ?: return null

    val material = Material.matchMaterial(itemSection.getString("item.material") ?: "BEDROCK") ?: Material.BEDROCK
    val itemName = itemSection.getString("item.name") ?: material.key.key
    val itemLore = itemSection.getStringList("item.lore")

    return createItem(material) {
        meta {
            this.name = itemName
            lore = itemLore
            this.persistentDataContainer.set(voucherNamespaceKey, PersistentDataType.STRING, name)
        }
    }
}