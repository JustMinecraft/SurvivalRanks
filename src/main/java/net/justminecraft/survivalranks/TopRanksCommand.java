package net.justminecraft.survivalranks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TopRanksCommand implements CommandExecutor {

    private final SurvivalRanks survivalRanks;

    public TopRanksCommand(SurvivalRanks survivalRanks) {
        this.survivalRanks = survivalRanks;
        survivalRanks.getCommand("topranks").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        int page = 0;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page '" + args[0] + "'.");
                return false;
            }
        }

        if (page < 0 || page > (survivalRanks.getRankedList().size() - 1) / 10) {
            sender.sendMessage(ChatColor.RED + "Invalid page '" + page + "'.");
            return false;
        }

        sender.sendMessage(ChatColor.RED + " --- Top Recent Points (Page " + (page + 1) + ") --- ");
        sender.sendMessage(ChatColor.DARK_RED + "Next page: /" + label + " " + (page + 2));

        for (int i = page * 10; i < page * 10 + 10 && i < survivalRanks.getRankedList().size(); i++) {
            Points points = survivalRanks.getRankedList().get(i);
            String score = Integer.toString(points.getPoints());
            if (points.getPoints() >= 2000)
                score = points.getPoints() / 1000 + "k";
            if (points.getPoints() >= 2000000)
                score = points.getPoints() / 100000 / 10.0 + "M";
            if (points.getPoints() >= 20000000)
                score = points.getPoints() / 1000000 + "M";

            sender.sendMessage(ChatColor.RED + " " + (i + 1) + ". [" + RanksManager.getRank(points.getPoints()).getTitle() + "] " + points.getUsername() + " (" + score + ")");
        }

        return true;
    }
}
