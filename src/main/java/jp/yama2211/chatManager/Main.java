package jp.yama2211.chatManager;

import jp.yama2211.chatManager.Listener.ChatListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ChannelManager manager = new ChannelManager();
        getCommand("channel").setExecutor(new ChannelCommand(manager));
        getServer().getPluginManager().registerEvents(new ChatListener(manager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
