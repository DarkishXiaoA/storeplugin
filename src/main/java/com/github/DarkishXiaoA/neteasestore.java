package com.github.DarkishXiaoA;

import com.github.DarkishXiaoA.Task.SendGood;
import com.github.DarkishXiaoA.entity.GoodEntity;
import com.github.DarkishXiaoA.event.PlayerListener;
import com.github.DarkishXiaoA.network.NeteaseNetWork;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class neteasestore extends JavaPlugin {
    public static String GameId;
    public static String SecretKey;
    public static String GetGoodsUrl;
    public static String SendGoodUrl;
    public static String EnterMsg;
    public static String GetMsg;
    public static Map<UUID, List<GoodEntity>> PlayerGoodInfo = new HashMap<>();
    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(),"config.yml");
        if (!file.exists()){
            saveDefaultConfig();
        }
        GameId = getConfig().getString("GameId");
        SecretKey = getConfig().getString("SecretKey");
        GetGoodsUrl = getConfig().getString("GetGoodsUrl");
        SendGoodUrl = getConfig().getString("SendGoodUrl");
        EnterMsg = getConfig().getString("EnterMsg");
        GetMsg =getConfig().getString("GetMsg");
        System.out.println("GameId:"+GameId);
        System.out.println("SecretKey:"+SecretKey);
        System.out.println("GetGoodsUrl:"+GetGoodsUrl);
        System.out.println("SendGoodUrl:"+SendGoodUrl);
        getServer().getMessenger().registerIncomingPluginChannel(this,"storemod", new NeteaseNetWork ());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "storemod");
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(),this);
        Bukkit.getPluginCommand("storeget").setExecutor(new SendGood());
        super.onEnable();
    }
}
