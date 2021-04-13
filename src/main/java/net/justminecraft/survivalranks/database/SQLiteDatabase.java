package net.justminecraft.survivalranks.database;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase extends Database {

    private File dbFile;

    public SQLiteDatabase(Plugin plugin, File dbFile) {
        super(plugin);
        this.dbFile = dbFile;
    }


    @Override
    public Connection openConnection() throws SQLException {
        if (!dbFile.exists()) {
            try {
                if (!dbFile.getParentFile().isDirectory() && !dbFile.getParentFile().mkdirs()) {
                    throw new IOException("Could not create database file's parent directory");
                }
                if (!dbFile.createNewFile()) {
                    throw new IOException("Could not create database file");
                }
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }
}
