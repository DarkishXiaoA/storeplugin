package com.github.DarkishXiaoA.Task;

import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.HudMessageType;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.DarkishXiaoA.entity.GoodEntity;
import com.github.DarkishXiaoA.neteasestore;
import com.github.DarkishXiaoA.util.Encypt;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wangyifan.neteaselogger.NLAPI;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class SendGood implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            Player player = (Player) commandSender;
            UUID uniqueId = ((Player) commandSender).getUniqueId();
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
                ArrayList<GoodEntity> goodList = new ArrayList<>();
                if (entities.size()==0){
                    GermPacketAPI.sendHudMessage(player, HudMessageType.LEFT1, neteasestore.GetMsg);
                    //System.out.println("这个玩家啥都没得");
                    return true;
                }
                for (int i = 0; i <= entities.size()-1; i++) {
                    JsonObject tem = (JsonObject) entities.get(i);
                    String orderid = tem.get("orderid").getAsString();
                    String cmd = tem.get("cmd").getAsString();
                    //System.out.println("单号："+orderid);
                    //System.out.println("命令:"+cmd);
                    goodList.add(new GoodEntity(orderid,cmd));
                }
                GermPacketAPI.sendHudMessage(player, HudMessageType.LEFT1, neteasestore.EnterMsg);
                neteasestore.PlayerGoodInfo.put(uniqueId,goodList);
            }catch (Exception e){
                //System.out.println("Json解析错误");
            }

            if (!neteasestore.PlayerGoodInfo.containsKey(uniqueId)){
                GermPacketAPI.sendHudMessage(player, HudMessageType.LEFT1, neteasestore.GetMsg);
                return false;
            }
            StringBuilder orderlist = new StringBuilder();
            List<GoodEntity> goodEntities = neteasestore.PlayerGoodInfo.get(uniqueId);
            if (goodEntities.size()==0){
                return true;
            }
            for(GoodEntity tempGood:goodEntities){
                //发货执行指令
                String cmd = tempGood.getCmd();
                String orderId = tempGood.getOrderid();
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("{player}",player.getName()));
                orderlist.append(orderId).append(",");
                //System.out.println(goodEntities);
                try {
                    String[] arr4 = cmd.split("\\s+");
                    String points = arr4[arr4.length-1];
                    NLAPI.addDeliver(player.getName(), orderId, "星际币", orderId, points);
                    NLAPI.addCurrency(player.getName(), "星际币", points, "充值");
                    GermPacketAPI.sendHudMessage(player, HudMessageType.LEFT1, "§6§l[商城] §7星际币签收成功 §6+"+points);
                }catch (Exception ignore){
                }
//                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"neteaselogger deliver "+player.getName()+" "+orderId+" 星际币 "+orderId+" "+arr4[arr4.length-1]);
            }
            String json = "{\"gameid\": \""+neteasestore.GameId+"\",\"uuid\": \""+uniqueId+"\",\"orderid_list\": ["+orderlist.substring(0,orderlist.length()-1)+"]}";
            //System.out.println(json);
            try{
                KeyCode = Encypt.HMACSHA256("POST/ship-mc-item-order"+json,neteasestore.SecretKey);
                //System.out.println("Encrypt:"+KeyCode);
            }catch (Exception e){
                //System.out.println("加密错误");
            }
            Map<String,String> headers= new HashMap<>();
            headers.put("content-type","application/json; charset=utf-8");
            headers.put("netease-server-sign", KeyCode);
            headers.put("cache-control","no-cache");
            HttpRequest.post(neteasestore.SendGoodUrl).headers(headers).send(json).body();
            //System.out.println(body);
            neteasestore.PlayerGoodInfo.remove(uniqueId);
        }

        return true;
    }
}