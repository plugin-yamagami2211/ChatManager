package jp.yama2211.chatManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChannelCommand implements CommandExecutor {
    private final ChannelManager manager;

    public ChannelCommand(ChannelManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) return false;

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) return false;
                manager.createChannel(args[1]);
                player.sendMessage("§aチャンネル '" + args[1] + "' を作成しました。");
            }
            case "join" -> {
                if (args.length < 2) return false;
                if (!manager.getChannelList().contains(args[1].toLowerCase())) {
                    player.sendMessage("§cそのチャンネルは存在しません。");
                    return true;
                }
                manager.joinChannel(player, args[1]);
                player.sendMessage("§aチャンネル '" + args[1] + "' に参加しました。");
            }
            case "leave" -> {
                manager.leaveChannel(player);
                player.sendMessage("§eチャンネルから退出しました（全体チャットに戻ります）。");
            }
            case "delete" -> {
                if (args.length < 2) return false;
                manager.deleteChannel(args[1]);
                player.sendMessage("§cチャンネル '" + args[1] + "' を削除しました。");
            }
            case "list" -> {
                player.sendMessage("§bチャンネル一覧: §f" + String.join(", ", manager.getChannelList()));
            }
        }
        return true;
    }
}
