package jp.yama2211.chatManager.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import jp.yama2211.chatManager.ChannelManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ChatListener implements Listener {
    private final ChannelManager manager;
    private static final TagResolver SAFE_TAGS = TagResolver.resolver(StandardTags.color(), StandardTags.decorations());
    private static final MiniMessage MM = MiniMessage.builder().tags(SAFE_TAGS).build();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder().character('&').hexColors().build();

    public ChatListener(ChannelManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String channelName = manager.getPlayerChannel(player);

        // 1. メッセージのカラー処理
        String raw = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        String mmString = MiniMessage.miniMessage().serialize(LEGACY.deserialize(raw));
        Component finalMsg = MM.deserialize(mmString);

        // 2. チャンネルプレフィックスの付与
        if (channelName != null) {
            event.renderer((source, sourceDisplayName, message, viewer) ->
                    Component.text("§7[" + channelName + "] ").append(sourceDisplayName).append(Component.text(": ")).append(finalMsg)
            );

            // 3. 受信者をチャンネルメンバーだけに制限
            event.viewers().removeIf(viewer -> {
                if (viewer instanceof Player p) {
                    return !manager.getMembers(channelName).contains(p.getUniqueId());
                }
                return false;
            });
        } else {
            // チャンネル未参加（全体）
            event.message(finalMsg);
        }
    }
}
