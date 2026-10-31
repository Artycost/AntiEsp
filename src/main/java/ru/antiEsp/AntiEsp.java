package ru.antiEsp;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiEsp extends JavaPlugin {
    private VisibilityController visibilityController;

    @Override
    public void onEnable() {
        visibilityController = new VisibilityController(this);
        Bukkit.getPluginManager().registerEvents(visibilityController, this);
        PacketEvents.getAPI().getEventManager().registerListener(new EquipmentPacketListener());
        visibilityController.start();
    }

    @Override
    public void onDisable() {
        if (visibilityController != null) {
            visibilityController.restoreAll();
        }
    }
}
