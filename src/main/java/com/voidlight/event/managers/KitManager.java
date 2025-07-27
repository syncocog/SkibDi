package com.voidlight.event.managers;

import com.voidlight.event.VoidlightEventPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages kit distribution and restoration
 * Handles the "Beast" kit with unbreakable diamond equipment
 */
public class KitManager {
    
    private final VoidlightEventPlugin plugin;
    private final Map<UUID, ItemStack[]> savedInventories;
    
    public KitManager(VoidlightEventPlugin plugin) {
        this.plugin = plugin;
        this.savedInventories = new HashMap<>();
    }
    
    /**
     * Give the "Beast" kit to a player
     */
    public void giveBeastKit(Player player) {
        // Save current inventory
        saveInventory(player);
        
        // Clear inventory
        player.getInventory().clear();
        
        // Create Beast kit items
        ItemStack helmet = createKitItem(Material.DIAMOND_HELMET);
        ItemStack chestplate = createKitItem(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = createKitItem(Material.DIAMOND_LEGGINGS);
        ItemStack boots = createKitItem(Material.DIAMOND_BOOTS);
        ItemStack sword = createKitSword();
        
        // Add Protection III to armor
        addArmorEnchantments(helmet);
        addArmorEnchantments(chestplate);
        addArmorEnchantments(leggings);
        addArmorEnchantments(boots);
        
        // Set armor
        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        
        // Set sword in first slot
        player.getInventory().setItem(0, sword);
        
        // Update inventory
        player.updateInventory();
    }
    
    /**
     * Remove kit and restore original inventory
     */
    public void removeKit(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Clear current inventory
        player.getInventory().clear();
        
        // Restore saved inventory if exists
        if (savedInventories.containsKey(playerId)) {
            ItemStack[] savedInventory = savedInventories.get(playerId);
            player.getInventory().setContents(savedInventory);
            savedInventories.remove(playerId);
        }
        
        // Clear armor slots
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        
        // Update inventory
        player.updateInventory();
    }
    
    /**
     * Save a player's current inventory
     */
    private void saveInventory(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        savedInventories.put(player.getUniqueId(), inventory.clone());
    }
    
    /**
     * Create a kit item with unbreakable property
     */
    private ItemStack createKitItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Create the Beast kit sword
     */
    private ItemStack createKitSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.setDisplayName("§6Beast Sword");
            sword.setItemMeta(meta);
        }
        
        return sword;
    }
    
    /**
     * Add Protection III enchantment to armor
     */
    private void addArmorEnchantments(ItemStack armor) {
        ItemMeta meta = armor.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION, 3, true);
            armor.setItemMeta(meta);
        }
    }
    
    /**
     * Check if a player has the kit
     */
    public boolean hasKit(Player player) {
        return savedInventories.containsKey(player.getUniqueId());
    }
    
    /**
     * Clear all saved inventories (plugin shutdown)
     */
    public void clearAllSavedInventories() {
        savedInventories.clear();
    }
    
    /**
     * Restore inventory for a specific player (emergency)
     */
    public void restoreInventory(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && savedInventories.containsKey(playerId)) {
            removeKit(player);
        }
    }
}