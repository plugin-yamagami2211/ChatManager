package jp.yama2211.chatManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.*;

public class ChannelManager {
    private final Main plugin;
    private final Map<String, ChannelData> channels = new HashMap<>();
    private final Map<UUID, String> playerInChannel = new HashMap<>();

    public ChannelManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void createChannel(String name, UUID owner, boolean isPublic) {
        ChannelData data = new ChannelData(name, owner, isPublic);
        // 作成者を最初からメンバーに追加
        data.members.add(owner);
        channels.put(name.toLowerCase(), data);

        // 作成者をそのチャンネルに参加状態にする
        Player player = org.bukkit.Bukkit.getPlayer(owner);
        if (player != null) {
            playerInChannel.put(owner, name.toLowerCase());
        }

        saveConfig();
    }

    public boolean deleteChannel(String name, Player actor) {
        ChannelData data = channels.get(name.toLowerCase());
        if (data == null || !data.owner.equals(actor.getUniqueId())) return false;

        channels.remove(name.toLowerCase());
        playerInChannel.entrySet().removeIf(entry -> entry.getValue().equalsIgnoreCase(name));
        saveConfig();
        return true;
    }

    public String createInvite(String channelName, UUID targetUuid) {
        ChannelData data = channels.get(channelName.toLowerCase());
        if (data == null) return null;

        String token = UUID.randomUUID().toString().substring(0, 8);
        data.pendingInvites.put(token, targetUuid);
        return token;
    }

    public boolean joinWithToken(Player player, String token) {
        for (ChannelData data : channels.values()) {
            if (data.pendingInvites.containsKey(token) && data.pendingInvites.get(token).equals(player.getUniqueId())) {
                data.pendingInvites.remove(token);
                // メンバーリストに追加してから参加処理
                data.members.add(player.getUniqueId());
                joinChannel(player, data.name);
                saveConfig(); // メンバー増員を保存
                return true;
            }
        }
        return false;
    }

    public void joinChannel(Player player, String name) {
        ChannelData data = channels.get(name.toLowerCase());
        if (data == null) return;

        // 公開か、あるいはメンバーリストに含まれている場合のみ参加可能
        if (!data.isPublic && !data.members.contains(player.getUniqueId())) return;

        leaveChannel(player);
        playerInChannel.put(player.getUniqueId(), name.toLowerCase());
    }

    public void leaveChannel(Player player) {
        playerInChannel.remove(player.getUniqueId());
    }

    public void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("channels", null);
        for (ChannelData data : channels.values()) {
            String path = "channels." + data.name;
            config.set(path + ".owner", data.owner.toString());
            config.set(path + ".public", data.isPublic);
            List<String> memberList = data.members.stream().map(UUID::toString).toList();
            config.set(path + ".members", memberList);
        }
        plugin.saveConfig();
    }

    private void loadConfig() {
        if (!plugin.getConfig().contains("channels")) return;
        plugin.getConfig().getConfigurationSection("channels").getKeys(false).forEach(name -> {
            String path = "channels." + name;
            UUID owner = UUID.fromString(plugin.getConfig().getString(path + ".owner"));
            boolean isPublic = plugin.getConfig().getBoolean(path + ".public");
            ChannelData data = new ChannelData(name, owner, isPublic);
            plugin.getConfig().getStringList(path + ".members").forEach(s -> data.members.add(UUID.fromString(s)));
            channels.put(name.toLowerCase(), data);
        });
    }

    public String getPlayerChannel(Player player) { return playerInChannel.get(player.getUniqueId()); }
    public Set<UUID> getMembers(String name) {
        ChannelData data = channels.get(name.toLowerCase());
        return data != null ? data.members : Collections.emptySet();
    }
    public Set<String> getChannelList() { return channels.keySet(); }
    public ChannelData getChannelData(String name) { return channels.get(name.toLowerCase()); }
}