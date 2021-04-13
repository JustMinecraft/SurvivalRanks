package net.justminecraft.survivalranks.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.function.Consumer;

public abstract class Database {

    private final Plugin plugin;

    public Database(Plugin plugin) {
        this.plugin = plugin;
    }

    private Connection connection = null;

    public abstract Connection openConnection() throws SQLException;

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = openConnection();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Deprecated
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkConnection();

        return connection.prepareStatement(sql);
    }

    public void prepareStatementAsync(String sql, SQLConsumer<PreparedStatement> preparedStatementConsumer) {
        SQLException exceptionInMainThread = new SQLException("Unhandled exception occurred async");
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                checkConnection();
                
                synchronized (this) {
                    preparedStatementConsumer.accept(connection.prepareStatement(sql));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                exceptionInMainThread.printStackTrace();
            }
        });
    }

    public void createTables() {
        try {
            checkConnection();

            try (Statement statement = connection.createStatement()) {
                statement.addBatch("CREATE TABLE IF NOT EXISTS points ("
                        + "uuid CHAR(36) PRIMARY KEY,"
                        + "username VARCHAR(16),"
                        + "points DEFAULT 0)");

                statement.executeBatch();
                statement.clearBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
