package ru.antiEsp;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

final class EquipmentPacketListener extends PacketListenerAbstract {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT) {
            return;
        }
        Player viewer = event.getPlayer();
        WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);
        Entity entity = SpigotConversionUtil.getEntityById(viewer.getWorld(), wrapper.getEntityId());
        if (!(entity instanceof Player target) || target.equals(viewer)) {
            return;
        }
        List<Equipment> equipment = wrapper.getEquipment();
        if (equipment == null || equipment.isEmpty()) {
            return;
        }
        List<Equipment> changed = new ArrayList<>(equipment.size());
        for (Equipment entry : equipment) {
            ItemStack item = SpigotConversionUtil.toBukkitItemStack(entry.getItem());
            if (isArmor(entry.getSlot()) && item != null && !item.getType().isAir()) {
                item = item.clone();
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text("@freakdev"));
                item.setItemMeta(meta);
            }
            changed.add(new Equipment(entry.getSlot(), SpigotConversionUtil.fromBukkitItemStack(item)));
        }
        wrapper.setEquipment(changed);
    }

    private boolean isArmor(EquipmentSlot slot) {
        return slot == EquipmentSlot.HELMET
                || slot == EquipmentSlot.CHEST_PLATE
                || slot == EquipmentSlot.LEGGINGS
                || slot == EquipmentSlot.BOOTS;
    }
}
