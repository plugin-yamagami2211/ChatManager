package jp.yama2211.chatManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GlobalCommand implements CommandExecutor {

    private static final TagResolver SAFE_TAGS = TagResolver.resolver(StandardTags.color(), StandardTags.decorations());
    private static final MiniMessage MM = MiniMessage.builder().tags(SAFE_TAGS).build();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder().character('&').hexColors().build();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行可能です。");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§c使用法: /global [メッセージ]");
            return true;
        }

        // 1. メッセージの結合
        String rawMessage = String.join(" ", args);

        // 2. カラー処理 (ChatListenerと同様のロジック)
        // &a などを Component にし、さらに MiniMessage 文字列にしてから制限付きでパース
        Component legacyComp = LEGACY.deserialize(rawMessage);
        String mmString = MiniMessage.miniMessage().serialize(legacyComp);
        Component finalMsg = MM.deserialize(mmString);

        // 3. 全体チャット形式の作成 [Global] プレイヤー名: メッセージ
        Component globalPrefix = Component.text("[Global] ").color(NamedTextColor.GOLD);
        Component format = globalPrefix
                .append(player.displayName())
                .append(Component.text(": ").color(NamedTextColor.WHITE))
                .append(finalMsg);

        // 4. 全プレイヤー（およびコンソール）に送信
        Bukkit.broadcast(format);

        return true;
    }
}