package io.tsukasau.paper_mysql;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class inventory_sync extends JavaPlugin implements Listener {
    StatusRecord statusRecord = null;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        FileConfiguration config = getConfig();

        Bukkit.getServer().getPluginManager().registerEvents(this, this);


        String host = "", database = "", username = "", password ="";
        int port = 0;
        if (config.contains("port")) port = config.getInt("port");
        if (config.contains("host")) host = config.getString("host");
        if (config.contains("database")) database = config.getString("database");
        if (config.contains("username")) username = config.getString("username");
        if (config.contains("password")) password = config.getString("password");

        statusRecord = new StatusRecord(host, port, database, username, password);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        statusRecord.loadPlayer(player);
        event.getPlayer().sendMessage(Component.text("load!, " + event.getPlayer().getName() + "!"));
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) throws SQLException {
        Player player = event.getPlayer();
//            statusRecord.deletePlayer(player);
        statusRecord.savePlayer(player, "UPDATE");
        event.getPlayer().sendMessage(Component.text("save!, " + event.getPlayer().getName() + "!"));
    }
}

