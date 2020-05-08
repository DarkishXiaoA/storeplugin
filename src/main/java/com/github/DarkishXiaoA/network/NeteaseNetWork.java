package com.github.DarkishXiaoA.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

public class NeteaseNetWork implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        String recive=new String(bytes, StandardCharsets.UTF_8);
        System.out.println(recive);
    }
}
