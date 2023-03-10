package io.tsukasau.paper_mysql;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class inventory_sync extends JavaPlugin implements Listener {
    static StatusRecord statusRecord = null;
    static JavaPlugin plugiin = null;

    public static Plugin getProvidingPlugin() {
        return plugiin;
    }

    @Override
    public void onEnable() {
        plugiin = this;

        saveDefaultConfig();
        FileConfiguration config = getConfig();

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("updateInventory")).setExecutor(new CommandClass());


        String host = "", database = "", username = "", password ="";
        int port = 0, timeout = 2000;
        if (config.contains("port")) port = config.getInt("port");
        if (config.contains("host")) host = config.getString("host");
        if (config.contains("database")) database = config.getString("database");
        if (config.contains("username")) username = config.getString("username");
        if (config.contains("password")) password = config.getString("password");
        if (config.contains("timeout")) timeout = config.getInt("timeout");

        statusRecord = new StatusRecord(host, port, database, username, password, timeout);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                statusRecord.loadPlayer(player, "");
            }
        });
//        statusRecord.loadPlayer(player, "");
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) throws SQLException {
        Player player = event.getPlayer();
//            statusRecord.deletePlayer(player);
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                try {
                    statusRecord.savePlayer(player, "UPDATE");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
//        statusRecord.savePlayer(player, "UPDATE");
    }

    JavaPlugin getPlugin() {
        return this;
    }
}

