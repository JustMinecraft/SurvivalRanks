package net.justminecraft.survivalranks;

import java.util.UUID;

public class Points {
    
    private final UUID uuid;
    private String username;
    private int points;

    public Points(UUID uuid) {
        this.uuid = uuid;
        this.username = null;
        this.points = 0;
    }

    public Points(UUID uuid, String username, int points) {
        this.uuid = uuid;
        this.username = username;
        this.points = points;
    }
    
    private void insertIntoDatabase() {
        SurvivalRanks.getSQLDatabase().prepareStatementAsync(
                "INSERT OR IGNORE INTO points (uuid, username, points)\n" +
                "VALUES (?, ?, ?)", statement -> {
            statement.setString(1, uuid.toString());
            statement.setString(2, username);
            statement.setInt(3, points);
            statement.execute();
        });
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;

        SurvivalRanks.getSQLDatabase().prepareStatementAsync(
                "UPDATE points SET username = ? WHERE uuid = ?", statement -> {
            statement.setString(1, username);
            statement.setString(2, uuid.toString());

            if (statement.executeUpdate() == 0) {
                insertIntoDatabase();
            }
        });
    }

    public int getPoints() {
        return points;
    }

    public void incrementPoints(int points_diff) {
        this.points += points_diff;

        SurvivalRanks.getSQLDatabase().prepareStatementAsync(
                "UPDATE points SET points = points + ? WHERE uuid = ?", statement -> {
            statement.setInt(1, points_diff);
            statement.setString(2, uuid.toString());
            
            if (statement.executeUpdate() == 0) {
                insertIntoDatabase();
            }
        });
    }

    public void scalePoints(double points_scalar) {
        this.points *= points_scalar;


        SurvivalRanks.getSQLDatabase().prepareStatementAsync(
                "UPDATE points SET points = points * ? WHERE uuid = ?", statement -> {
            statement.setDouble(1, points_scalar);
            statement.setString(2, uuid.toString());

            if (statement.executeUpdate() == 0) {
                insertIntoDatabase();
            }
        });
    }
}
