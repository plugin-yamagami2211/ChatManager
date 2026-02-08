package jp.yama2211.chatManager;

import org.bukkit.entity.Player;
import java.util.*;

public class ChannelManager {
    // チャンネル名 -> 参加プレイヤーのUUIDセット
    private final Map<String, Set<UUID>> channels = new HashMap<>();
    // プレイヤーUUID -> 参加中のチャンネル名
    private final Map<UUID, String> playerInChannel = new HashMap<>();

    public void createChannel(String name) {
        channels.putIfAbsent(name.toLowerCase(), new HashSet<>());
    }

    public void deleteChannel(String name) {
        String lowerName = name.toLowerCase();
        Set<UUID> members = channels.get(lowerName);
        if (members != null) {
            for (UUID uuid : members) {
                playerInChannel.remove(uuid);
            }
            channels.remove(lowerName);
        }
    }

    public void joinChannel(Player player, String name) {
        String lowerName = name.toLowerCase();
        if (!channels.containsKey(lowerName)) return;

        leaveChannel(player); // 既に参加していれば抜ける
        channels.get(lowerName).add(player.getUniqueId());
        playerInChannel.put(player.getUniqueId(), lowerName);
    }

    public void leaveChannel(Player player) {
        String current = playerInChannel.remove(player.getUniqueId());
        if (current != null) {
            channels.get(current).remove(player.getUniqueId());
        }
    }

    public String getPlayerChannel(Player player) {
        return playerInChannel.get(player.getUniqueId());
    }

    public Set<UUID> getMembers(String name) {
        return channels.getOrDefault(name.toLowerCase(), Collections.emptySet());
    }

    public Set<String> getChannelList() {
        return channels.keySet();
    }
}
