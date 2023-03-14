package io.tsukasau.paper_mysql;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandClass implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("updateInventory")) {
            if (args.length == 0) {

                Player player = (Player) sender;

                inventory_sync.statusRecord.loadPlayer(player, "ENFORCE");
                sender.sendMessage("enforced to update inventory!, " + player.getName() + "!");

                return true;
            }
        }
        return false;
    }
}
