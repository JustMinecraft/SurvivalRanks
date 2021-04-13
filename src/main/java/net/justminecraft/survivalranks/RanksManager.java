package net.justminecraft.survivalranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RanksManager implements Listener, Runnable {
    
    private static Map<String, Rank> ranksMap = new HashMap<>();
    private static List<Rank> ranks = new ArrayList<>();
    private final SurvivalRanks survivalRanks;
    
    private final Map<Player, Rank> lastRanks = new HashMap<>();

    public RanksManager(SurvivalRanks survivalRanks) {
        this.survivalRanks = survivalRanks;
        survivalRanks.getServer().getPluginManager().registerEvents(this, survivalRanks);
        load(survivalRanks.getConfig().getConfigurationSection("ranks"));
        survivalRanks.getServer().getScheduler().runTaskTimerAsynchronously(survivalRanks, this,
                (long) (60 * 20 + Math.random() * 100), (long) (60 * 20 + Math.random() * 100));
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Rank lastRank = lastRanks.get(player);
            Rank rank = getRank(survivalRanks.getPoints(player).getPoints());
            
            if (rank != lastRank) {
                if (lastRank != null) {
                    player.sendMessage(ChatColor.GREEN + "You have ranked up to " + rank.getChatColor() + "[" + rank.getTitle() + "]");
                    player.getWorld().playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                }
                
                lastRanks.put(player, rank);
            }
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastRanks.remove(e.getPlayer());
    }
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getFormat().contains("%1$s")) {
            Rank rank = getRank(survivalRanks.getPoints(e.getPlayer()).getPoints());
            e.setFormat(e.getFormat().replace("%1$s", rank.getChatColor() + "[" + rank.title + "] %1$s" + ChatColor.RESET));
        }
    }

    private void load(ConfigurationSection ranksSection) {
        for (String key : ranksSection.getKeys(false)) {
            ConfigurationSection section = ranksSection.getConfigurationSection(key);
            Rank rank = new Rank(key, section.getInt("points", 0));
            ranksMap.put(key, rank);
            ranks.add(rank);
        }

        Collections.sort(ranks);
    }
    
    public static Rank getRank(int points) {
        return ranks.get(binarySearch(points, ranks));
    }

    private static int binarySearch(int points, List<Rank> list) {
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Rank midVal = list.get(mid);
            int cmp = midVal.points - points;

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return low - 1;  // key not found
    }

    public static class Rank implements Comparable<Rank> {
        private String title;
        private int points;

        public Rank(String title, int points) {
            this.title = title;
            this.points = points;
        }

        public String getTitle() {
            return title;
        }

        public int getPoints() {
            return points;
        }
        
        public ChatColor getChatColor() {
            int color = new Random(title.hashCode()).nextInt(16);
            return ChatColor.values()[color];
        }

        @Override
        public int compareTo(@NotNull RanksManager.Rank o) {
            return points - o.points;
        }
        
        @Override
        public String toString() {
            return "Rank{" + title + ",points=" + points + "}";
        }
    }
}
