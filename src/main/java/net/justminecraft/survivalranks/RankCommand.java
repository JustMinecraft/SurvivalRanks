package net.justminecraft.survivalranks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankCommand implements CommandExecutor {

    private final SurvivalRanks survivalRanks;

    public RankCommand(SurvivalRanks survivalRanks) {
        this.survivalRanks = survivalRanks;
        survivalRanks.getCommand("rank").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return false;
        }
        
        Points points = survivalRanks.getPoints((Player) sender);
        
        sender.sendMessage(ChatColor.DARK_RED + "You have " + ChatColor.RED + points.getPoints() + ChatColor.DARK_RED + " points.");
        
        return true;
    }
}
