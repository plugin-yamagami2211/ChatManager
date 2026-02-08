package jp.yama2211.chatManager;

import jp.yama2211.chatManager.Listener.ChatListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ChannelManager manager = new ChannelManager(this);

        // チャンネルコマンドの登録
        getCommand("channel").setExecutor(new ChannelCommand(manager));

        // 全体チャットコマンドの登録
        GlobalCommand globalCommand = new GlobalCommand();
        getCommand("global").setExecutor(globalCommand);

        getServer().getPluginManager().registerEvents(new ChatListener(manager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
