package jp.yama2211.chatManager.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import jp.yama2211.chatManager.ChannelManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;

public class ChatListener implements Listener {
    private final ChannelManager manager;

    // 色と装飾のみを許可する設定
    private static final TagResolver SAFE_TAGS = TagResolver.resolver(
            StandardTags.color(),
            StandardTags.decorations()
    );

    private static final MiniMessage MM = MiniMessage.builder().tags(SAFE_TAGS).build();

    // '&' を色コードとして解釈するためのシリアライザー
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public ChatListener(ChannelManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String channelName = manager.getPlayerChannel(player);

        // 1. メッセージのカラー処理 (Legacy & -> MiniMessage変換)
        String raw = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        Component legacyComp = LEGACY.deserialize(raw);
        String mmString = MiniMessage.miniMessage().serialize(legacyComp);
        Component finalMsg = MM.deserialize(mmString);

        // 2. チャンネル処理
        if (channelName != null) {
            // 表示形式のレンダリング設定: [チャンネル名] プレイヤー名: メッセージ
            event.renderer((source, sourceDisplayName, message, viewer) ->
                    Component.text("§7[" + channelName + "] ")
                            .append(sourceDisplayName)
                            .append(Component.text(": "))
                            .append(finalMsg)
            );

            // 3. 受信者をチャンネルメンバーだけに制限
            // getMembers(channelName) で取得したメンバーリストに含まれないプレイヤーを viewer から削除
            Set<Set<java.util.UUID>> members = Set.of(manager.getMembers(channelName));
            event.viewers().removeIf(viewer -> {
                if (viewer instanceof Player p) {
                    return !manager.getMembers(channelName).contains(p.getUniqueId());
                }
                return false;
            });
        } else {
            // チャンネル未参加（全体チャット）の場合は、単に色を適用したメッセージをセット
            event.message(finalMsg);
        }
    }
}
