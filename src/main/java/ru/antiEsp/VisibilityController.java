package ru.antiEsp;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class VisibilityController implements Listener {
    private static final double MAX_DISTANCE_SQUARED = 192.0 * 192.0;
    private static final int NORMAL_HIDE_DELAY = 4;
    private final AntiEsp plugin;
    private final Map<UUID, Set<UUID>> hidden = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> blockedTicks = new HashMap<>();

    VisibilityController(AntiEsp plugin) {
        this.plugin = plugin;
    }

    void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::update, 1L, 1L);
    }

    private void update() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.hasPermission("antiesp.bypass")) {
                restore(viewer);
                continue;
            }
            Set<UUID> viewerHidden = hidden.computeIfAbsent(viewer.getUniqueId(), ignored -> new HashSet<>());
            Map<UUID, Integer> viewerBlocked = blockedTicks.computeIfAbsent(viewer.getUniqueId(), ignored -> new HashMap<>());
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (viewer.equals(target)) {
                    continue;
                }
                boolean visible = canReveal(viewer, target) && hasDirectView(viewer, target);
                if (visible) {
                    viewerBlocked.remove(target.getUniqueId());
                    if (viewerHidden.remove(target.getUniqueId())) {
                        viewer.showPlayer(plugin, target);
                    }
                    continue;
                }
                int ticks = viewerBlocked.merge(target.getUniqueId(), 1, Integer::sum);
                int delay = target.isInvisible() ? 1 : NORMAL_HIDE_DELAY;
                if (ticks >= delay && viewerHidden.add(target.getUniqueId())) {
                    viewer.hidePlayer(plugin, target);
                }
            }
            viewerHidden.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
            viewerBlocked.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        }
    }

    private boolean canReveal(Player viewer, Player target) {
        if (!viewer.getWorld().equals(target.getWorld())) {
            return false;
        }
        return viewer.getLocation().distanceSquared(target.getLocation()) <= MAX_DISTANCE_SQUARED;
    }

    private boolean hasDirectView(Player viewer, Player target) {
        if (!viewer.getWorld().equals(target.getWorld())) {
            return false;
        }
        Location eye = viewer.getEyeLocation();
        Location base = target.getLocation();
        double height = Math.max(0.6, target.getHeight());
        double radius = Math.max(0.2, target.getWidth() * 0.48);
        return clear(eye, base.clone().add(0.0, 0.15, 0.0))
                || clear(eye, base.clone().add(0.0, height * 0.5, 0.0))
                || clear(eye, base.clone().add(0.0, height * 0.92, 0.0))
                || clear(eye, base.clone().add(radius, height * 0.55, 0.0))
                || clear(eye, base.clone().add(-radius, height * 0.55, 0.0))
                || clear(eye, base.clone().add(0.0, height * 0.55, radius))
                || clear(eye, base.clone().add(0.0, height * 0.55, -radius));
    }

    private boolean clear(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        if (distance < 0.01) {
            return true;
        }
        World world = from.getWorld();
        return world.rayTraceBlocks(from, direction.normalize(), distance, FluidCollisionMode.NEVER, true) == null;
    }

    private void restore(Player viewer) {
        Set<UUID> targets = hidden.remove(viewer.getUniqueId());
        blockedTicks.remove(viewer.getUniqueId());
        if (targets == null) {
            return;
        }
        for (UUID uuid : targets) {
            Player target = Bukkit.getPlayer(uuid);
            if (target != null) {
                viewer.showPlayer(plugin, target);
            }
        }
    }

    void restoreAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            restore(viewer);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        hidden.put(event.getPlayer().getUniqueId(), new HashSet<>());
        blockedTicks.put(event.getPlayer().getUniqueId(), new HashMap<>());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!positionChanged(event.getFrom(), event.getTo())) {
            return;
        }
        Player moved = event.getPlayer();
        revealVisible(moved);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(moved) && isHidden(viewer, moved)) {
                reveal(viewer, moved);
            }
        }
    }

    private boolean positionChanged(Location from, Location to) {
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()
                || !from.getWorld().equals(to.getWorld());
    }

    private void revealVisible(Player viewer) {
        Set<UUID> targets = hidden.get(viewer.getUniqueId());
        if (targets == null || targets.isEmpty()) {
            return;
        }
        for (UUID uuid : Set.copyOf(targets)) {
            Player target = Bukkit.getPlayer(uuid);
            if (target != null) {
                reveal(viewer, target);
            }
        }
    }

    private boolean isHidden(Player viewer, Player target) {
        Set<UUID> targets = hidden.get(viewer.getUniqueId());
        return targets != null && targets.contains(target.getUniqueId());
    }

    private void reveal(Player viewer, Player target) {
        if (!canReveal(viewer, target) || !hasDirectView(viewer, target)) {
            return;
        }
        Set<UUID> targets = hidden.get(viewer.getUniqueId());
        if (targets != null && targets.remove(target.getUniqueId())) {
            Map<UUID, Integer> counters = blockedTicks.get(viewer.getUniqueId());
            if (counters != null) {
                counters.remove(target.getUniqueId());
            }
            viewer.showPlayer(plugin, target);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restore(event.getPlayer());
        UUID uuid = event.getPlayer().getUniqueId();
        hidden.values().forEach(set -> set.remove(uuid));
        blockedTicks.values().forEach(map -> map.remove(uuid));
    }
}
