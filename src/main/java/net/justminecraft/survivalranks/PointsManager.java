package net.justminecraft.survivalranks;

import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;

import java.util.HashMap;

public class PointsManager implements Listener, Runnable {
    
    private static final int POINTS_INTERVAL = 5;
    
    private static final int MOVEMENT_POINTS = 7;
    private static final int BREAK_BLOCK_POINTS = 20;
    private static final int PLACE_BLOCK_POINTS = 30;
    private static final int KILL_MOB_POINTS = 100;
    
    private static final int ACHIEVEMENT_POINTS = 2500;

    private final SurvivalRanks survivalRanks;
    
    private HashMap<Player, Integer> pointsThisInterval = new HashMap<>();
    private HashMap<Player, Location> lastLocations = new HashMap<>();

    public PointsManager(SurvivalRanks survivalRanks) {
        this.survivalRanks = survivalRanks;
        survivalRanks.getServer().getPluginManager().registerEvents(this, survivalRanks);
        survivalRanks.getServer().getScheduler().runTaskTimer(survivalRanks, this, POINTS_INTERVAL * 20, POINTS_INTERVAL * 20);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAchievement(PlayerAchievementAwardedEvent event) {
        int parents = 1;
        Achievement parent = event.getAchievement();
        
        while (parent.getParent() != null) {
            parent = parent.getParent();
            parents++;
        }
        
        int points = ACHIEVEMENT_POINTS * parents;
        survivalRanks.getPoints(event.getPlayer()).incrementPoints(points);
        
        Bukkit.getScheduler().runTask(survivalRanks, () -> event.getPlayer().sendMessage(ChatColor.GREEN + " + " + points + " points"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        setPointsThisInterval(event.getPlayer(), BREAK_BLOCK_POINTS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        setPointsThisInterval(event.getPlayer(), PLACE_BLOCK_POINTS);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity killer = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
            
            if (killer instanceof Player) {
                setPointsThisInterval((Player) killer, KILL_MOB_POINTS);
            }
        }
    }

    private void setPointsThisInterval(Player player, int points) {
        if (pointsThisInterval.getOrDefault(player, 0) < points) {
            pointsThisInterval.put(player, points);
        }
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = player.getLocation();
            Location lastLocation = lastLocations.get(player);
            
            if (lastLocation == null
                    || lastLocation.getYaw() != location.getYaw()
                    || lastLocation.getPitch() != location.getPitch()
                    || Math.abs(lastLocation.getX() - location.getX()) >= 5
                    || Math.abs(lastLocation.getZ() - location.getZ()) >= 5) {
                lastLocations.put(player, location);

                survivalRanks.getPoints(player).incrementPoints(pointsThisInterval.getOrDefault(player, MOVEMENT_POINTS));
            }
        }

        pointsThisInterval.clear();
    }
}
