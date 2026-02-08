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
        channels.put(name.toLowerCase(), new ChannelData(name, owner, isPublic));
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

    // 招待トークン生成 (英数字8桁)
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
                data.pendingInvites.remove(token); // 1回きり
                joinChannel(player, data.name);
                return true;
            }
        }
        return false;
    }

    public void joinChannel(Player player, String name) {
        ChannelData data = channels.get(name.toLowerCase());
        if (data == null) return;
        if (!data.isPublic && !data.members.contains(player.getUniqueId()) && !data.owner.equals(player.getUniqueId())) return;

        leaveChannel(player);
        data.members.add(player.getUniqueId());
        playerInChannel.put(player.getUniqueId(), name.toLowerCase());
    }

    public void leaveChannel(Player player) {
        String current = playerInChannel.remove(player.getUniqueId());
        if (current != null) {
            ChannelData data = channels.get(current);
            if (data != null) data.members.remove(player.getUniqueId());
        }
    }

    // --- 保存・読み込み ---
    public void saveConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("channels", null); // クリア
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
    public Set<UUID> getMembers(String name) { return channels.get(name.toLowerCase()).members; }
    public Set<String> getChannelList() { return channels.keySet(); }
    public ChannelData getChannelData(String name) { return channels.get(name.toLowerCase()); }
}