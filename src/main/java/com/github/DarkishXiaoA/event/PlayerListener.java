package com.github.DarkishXiaoA.event;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.DarkishXiaoA.entity.GoodEntity;
import com.github.DarkishXiaoA.neteasestore;
import com.github.DarkishXiaoA.util.Encypt;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    @EventHandler
    public void PlayerJoinGame(PlayerJoinEvent event){
        UUID uniqueId = event.getPlayer().getUniqueId();
        String Json = "{\"gameid\":"+neteasestore.GameId+",\"uuid\":\""+ uniqueId+"\"}";
        //System.out.println(Json);
        String KeyCode = "";
        try{
            KeyCode = Encypt.HMACSHA256("POST/get-mc-item-order-list"+Json,neteasestore.SecretKey);
            //System.out.println("Encrypt:"+KeyCode);
        }catch (Exception e){
            //System.out.println("加密错误");
        }
        Map<String,String> Header = new HashMap<>();
        Header.put("content-type","application/json; charset=utf-8");
        Header.put("netease-server-sign", KeyCode);
        Header.put("cache-control","no-cache");
        String body = HttpRequest.post(neteasestore.GetGoodsUrl).headers(Header).send(Json).body();
        try{
            body = URLDecoder.decode(body,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //System.out.println(body);
        try{
            JsonObject jsonObject= (JsonObject) new JsonParser().parse(body);
            JsonArray entities = jsonObject.getAsJsonArray("entities");
            ArrayList<GoodEntity> GoodList = new ArrayList<>();
            if (entities.size()==0){
                //System.out.println("这个玩家啥都没得");
                return;
            }
            for (int i = 0; i <= entities.size()-1; i++) {
                JsonObject tem = (JsonObject) entities.get(i);
                String orderid = tem.get("orderid").getAsString();
                String cmd = tem.get("cmd").getAsString();
                //System.out.println("单号："+orderid);
                //System.out.println("命令:"+cmd);
                GoodList.add(new GoodEntity(orderid,cmd));
            }
            event.getPlayer().sendMessage(neteasestore.EnterMsg);
            neteasestore.PlayerGoodInfo.put(uniqueId,GoodList);
        }catch (Exception e){
            //System.out.println("Json解析错误");
        }

    }
}
