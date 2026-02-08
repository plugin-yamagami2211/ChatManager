package jp.yama2211.chatManager;

import java.util.*;

public class ChannelData {
    public String name;
    public UUID owner;
    public boolean isPublic;
    public Set<UUID> members = new HashSet<>();
    public Map<String, UUID> pendingInvites = new HashMap<>(); // トークン -> 招待されたプレイヤー

    public ChannelData(String name, UUID owner, boolean isPublic) {
        this.name = name;
        this.owner = owner;
        this.isPublic = isPublic;
    }
}