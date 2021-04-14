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
        RanksManager.Rank rank = RanksManager.getRank(points.getPoints());
        RanksManager.Rank nextRank = RanksManager.getNextRank(points.getPoints());

        sender.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + points.getPoints() + ChatColor.RED + " points.");
        sender.sendMessage(ChatColor.DARK_RED + "You are rank " + rank.getChatColor() + "[" + rank.getTitle() + "]");
        
        if (nextRank == null) {
            sender.sendMessage(ChatColor.DARK_RED + "You are the final rank.");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You need " + ChatColor.RED + (nextRank.getPoints() - points.getPoints()) + ChatColor.DARK_RED + " more points for the next rank.");
        }
        
        return true;
    }
}
