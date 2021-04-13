package net.justminecraft.survivalranks;

import net.justminecraft.survivalranks.database.Database;
import net.justminecraft.survivalranks.database.SQLiteDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SurvivalRanks extends JavaPlugin {
    
    private static Database database;

    private Map<UUID, Points> pointsMap = new HashMap<>();
    private Map<UUID, Integer> ranks = new HashMap<>();
    private List<Points> rankedList = new ArrayList<>();

    @Override
    public void onEnable() {
        database = new SQLiteDatabase(this, new File(getDataFolder(), "survival-ranks.db"));
        database.createTables();

        new PointsManager(this);
        
        new RankCommand(this);
        new TopRanksCommand(this);
        
        loadPoints();
        
        // Update everyone's rank every minute
        getServer().getScheduler().runTaskTimerAsynchronously(this,
                () -> Bukkit.getOnlinePlayers().forEach(this::updateRank),
                (long) (60 * 20 + Math.random() * 100), (long) (60 * 20 + Math.random() * 100));
    }

    private void loadPoints() {
        getSQLDatabase().prepareStatementAsync("SELECT * FROM points ORDER BY points DESC", preparedStatement -> {
            ResultSet results = preparedStatement.executeQuery();
            
            while (results.next()) {
                try {
                    UUID uuid = UUID.fromString(results.getString("uuid"));
                    String username = results.getString("username");
                    int points = results.getInt("points");
                    
                    Points pointsObj = new Points(uuid, username, points);
                    
                    Points oldPoints = pointsMap.put(uuid, pointsObj);
                    
                    if (oldPoints != null) {
                        rankedList.remove(oldPoints);
                    }
                    
                    rankedList.add(pointsObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static Database getSQLDatabase() {
        return database;
    }

    public Points getPoints(Player player) {
        Points points = pointsMap.computeIfAbsent(player.getUniqueId(), Points::new);
        
        if (!player.getName().equals(points.getUsername())) {
            points.setUsername(player.getName());
        }
        
        return points;
    }

    /**
     * Update the rank of a player by sorting the points
     * @param player The player to update the rank of
     */
    public void updateRank(Player player) {
        Points points = getPoints(player);
        int index = rankedList.indexOf(points);
        
        if (index == -1) {
            index = rankedList.size();
            rankedList.add(points);
        }
        
        // Sort upwards
        while (index > 0) {
            Points up = rankedList.get(index - 1);
            Points down = rankedList.get(index);
            
            if (up.getPoints() < down.getPoints()) {
                Collections.swap(rankedList, index, --index);
            } else {
                break;
            }
        }

        // Sort downwards
        while (index < rankedList.size() - 1) {
            Points up = rankedList.get(index);
            Points down = rankedList.get(index + 1);

            if (up.getPoints() < down.getPoints()) {
                Collections.swap(rankedList, index, ++index);
            } else {
                break;
            }
        }
        
        ranks.put(player.getUniqueId(), index);
    }

    /**
     * Get the ranking of a player. This value may be cached, update the rank
     * cache with {@code updateRank(player)}
     * @param player The player to get the ranking of
     * @return The rank of the player with 0 being the best
     */
    public int getRank(Player player) {
        return ranks.getOrDefault(player, rankedList.size());
    }

    public List<Points> getRankedList() {
        return rankedList;
    }
}