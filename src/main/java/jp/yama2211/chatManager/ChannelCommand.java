package jp.yama2211.chatManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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
        // コンソールからの実行を制限
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行可能です。");
            return true;
        }

        // 引数がない場合は使い方を表示
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
                // 重複チェック
                if (manager.getChannelData(name) != null) {
                    player.sendMessage("§cその名前のチャンネルは既に存在します。");
                    return true;
                }
                // デフォルトはprivate
                boolean isPublic = args.length >= 3 && args[2].equalsIgnoreCase("public");
                manager.createChannel(name, player.getUniqueId(), isPublic);
                player.sendMessage("§aチャンネル '" + name + "' (" + (isPublic ? "公開" : "非公開") + ") を作成しました。");
            }

            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage("§c使用法: /channel join [名前/招待コード]");
                    return true;
                }
                String input = args[1];

                // 1. まず招待トークンとして処理を試みる
                if (manager.joinWithToken(player, input)) {
                    player.sendMessage("§a招待が承認されました。チャンネルに参加しました。");
                    return true;
                }

                // 2. トークンでない場合はチャンネル名として処理
                ChannelData data = manager.getChannelData(input);
                if (data == null) {
                    player.sendMessage("§cチャンネルが見つからないか、招待コードが無効です。");
                    return true;
                }

                if (data.isPublic) {
                    manager.joinChannel(player, input);
                    player.sendMessage("§a公開チャンネル '" + data.name + "' に参加しました。");
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
                    player.sendMessage("§c招待を送るには、まずチャンネルに参加してください。");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cプレイヤー '" + args[1] + "' はオンラインではありません。");
                    return true;
                }

                // 招待トークン生成
                String token = manager.createInvite(currentChannel, target.getUniqueId());
                if (token == null) {
                    player.sendMessage("§cエラーが発生しました。");
                    return true;
                }

                // 招待相手へのメッセージ
                target.sendMessage("§e§l[!] チャンネル招待");
                target.sendMessage("§f" + player.getName() + " さんがあなたをチャンネル §b" + currentChannel + " §fに招待しました。");

                Component joinButton = Component.text(" >>> [ここをクリックして参加] <<< ")
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand("/channel join " + token));

                target.sendMessage(joinButton);
                player.sendMessage("§a" + target.getName() + " に招待を送りました。");
            }

            case "leave" -> {
                String current = manager.getPlayerChannel(player);
                if (current == null) {
                    player.sendMessage("§c現在はどのチャンネルにも参加していません。");
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
                String name = args[1];
                if (manager.deleteChannel(name, player)) {
                    player.sendMessage("§aチャンネル '" + name + "' を削除しました。");
                } else {
                    player.sendMessage("§c削除できません。作成者本人であるか確認してください。");
                }
            }

            case "list" -> {
                Set<String> channels = manager.getChannelList();
                if (channels.isEmpty()) {
                    player.sendMessage("§7現在、作成されているチャンネルはありません。");
                } else {
                    player.sendMessage("§b--- チャンネルリスト ---");
                    for (String name : channels) {
                        ChannelData data = manager.getChannelData(name);
                        String type = data.isPublic ? "§7[Public]" : "§6[Private]";
                        player.sendMessage("§f- " + name + " " + type);
                    }
                }
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§b--- Channel Help ---");
        player.sendMessage("§f/channel create [名前] [public/private] : チャンネル作成");
        player.sendMessage("§f/channel join [名前/コード] : 参加");
        player.sendMessage("§f/channel invite [プレイヤー] : 招待");
        player.sendMessage("§f/channel leave : 退出");
        player.sendMessage("§f/channel delete [名前] : 削除（作成者のみ）");
        player.sendMessage("§f/channel list : 一覧表示");
    }
}