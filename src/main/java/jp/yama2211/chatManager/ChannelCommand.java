package jp.yama2211.chatManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ChannelCommand implements CommandExecutor {
    private final ChannelManager manager;

    public ChannelCommand(ChannelManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行可能です。");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage("§c使用法: /channel create [名前] [public/private]");
                    return true;
                }
                String name = args[1];
                if (manager.getChannelData(name) != null) {
                    player.sendMessage("§cその名前のチャンネルは既に存在します。");
                    return true;
                }
                boolean isPublic = args.length >= 3 && args[2].equalsIgnoreCase("public");
                manager.createChannel(name, player.getUniqueId(), isPublic);
                player.sendMessage("§aチャンネル '" + name + "' (" + (isPublic ? "公開" : "非公開") + ") を作成しました。");
                player.sendMessage("§7※自動的にこのチャンネルに参加しました。");
            }

            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage("§c使用法: /channel join [名前/招待コード]");
                    return true;
                }
                String input = args[1];

                if (manager.joinWithToken(player, input)) {
                    player.sendMessage("§a招待コードが承認されました。");
                    return true;
                }

                ChannelData data = manager.getChannelData(input);
                if (data == null) {
                    player.sendMessage("§cチャンネルが見つからないか、招待コードが無効です。");
                    return true;
                }

                if (data.isPublic || data.members.contains(player.getUniqueId())) {
                    manager.joinChannel(player, input);
                    player.sendMessage("§aチャンネル '" + data.name + "' に参加しました。");
                } else {
                    player.sendMessage("§cこのチャンネルは非公開です。参加するには招待が必要です。");
                }
            }

            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage("§c使用法: /channel invite [プレイヤー名]");
                    return true;
                }
                String currentChannel = manager.getPlayerChannel(player);
                if (currentChannel == null) {
                    player.sendMessage("§cチャンネルに参加していません。");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cプレイヤーが見つかりません。");
                    return true;
                }

                String token = manager.createInvite(currentChannel, target.getUniqueId());
                String joinCommand = "/channel join " + token;

                target.sendMessage("§e§l--- チャンネル招待 ---");
                target.sendMessage("§f" + player.getName() + " さんがあなたを §b" + currentChannel + " §fに招待しました。");

                // コマンド文字列の表示
                target.sendMessage("§7招待コードを使用して参加するには以下のコマンドを入力してください:");
                target.sendMessage("§e" + joinCommand);

                // クリックボタンの表示
                Component joinButton = Component.text("[ここをクリックして参加]")
                        .color(NamedTextColor.AQUA)
                        .hoverEvent(HoverEvent.showText(Component.text("クリックしてチャンネルに参加")))
                        .clickEvent(ClickEvent.runCommand(joinCommand));

                target.sendMessage(joinButton);
                player.sendMessage("§a" + target.getName() + " に招待を送りました。");
            }

            case "leave" -> {
                String current = manager.getPlayerChannel(player);
                if (current == null) {
                    player.sendMessage("§cチャンネルに参加していません。");
                    return true;
                }
                manager.leaveChannel(player);
                player.sendMessage("§eチャンネル '" + current + "' から退出しました。");
            }

            case "delete" -> {
                if (args.length < 2) {
                    player.sendMessage("§c使用法: /channel delete [名前]");
                    return true;
                }
                if (manager.deleteChannel(args[1], player)) {
                    player.sendMessage("§a削除しました。");
                } else {
                    player.sendMessage("§c削除権限がないか、チャンネルが存在しません。");
                }
            }

            case "list" -> {
                Set<String> channels = manager.getChannelList();
                player.sendMessage("§b--- チャンネルリスト ---");
                for (String name : channels) {
                    ChannelData data = manager.getChannelData(name);
                    String type = data.isPublic ? "§7[Public]" : "§6[Private]";
                    player.sendMessage("§f- " + name + " " + type);
                }
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§b--- Channel Help ---");
        player.sendMessage("§f/channel create [名前] [public/private]");
        player.sendMessage("§f/channel join [名前/コード]");
        player.sendMessage("§f/channel invite [プレイヤー]");
        player.sendMessage("§f/channel leave");
        player.sendMessage("§f/channel delete [名前]");
        player.sendMessage("§f/channel list");
    }
}