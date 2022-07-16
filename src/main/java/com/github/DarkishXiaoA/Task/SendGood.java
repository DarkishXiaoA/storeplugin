package com.github.DarkishXiaoA.Task;

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
            UUID uniqueId = player.getUniqueId();
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
                    return true;
                }
                for (int i = 0; i <= entities.size()-1; i++) {
                    JsonObject tem = (JsonObject) entities.get(i);
                    String orderid = tem.get("orderid").getAsString();
                    String cmd = tem.get("cmd").getAsString();
                    String type = tem.get("type").getAsString();
                    String extra = tem.get("extra").getAsString();
                    System.out.println("类型："+type);
                    System.out.println("额外："+extra);
                    //System.out.println("单号："+orderid);
                    //System.out.println("命令:"+cmd);
                    GoodList.add(new GoodEntity(orderid,cmd));
                }
                commandSender.sendMessage(neteasestore.EnterMsg);
                neteasestore.PlayerGoodInfo.put(uniqueId,GoodList);
            }catch (Exception e){
                //System.out.println("Json解析错误");
            }
            if (!neteasestore.PlayerGoodInfo.containsKey(uniqueId)){
                player.sendMessage(neteasestore.GetMsg);
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
                NLAPI.addDeliver(player.getName(), orderId, "星际币", orderId, cmd);
            }
            String json = "{\"gameid\": \""+neteasestore.GameId+"\",\"uuid\": \""+uniqueId+"\",\"orderid_list\": ["+orderlist.substring(0,orderlist.length()-1)+"]}";
            //System.out.println(json);
            try{
                KeyCode = Encypt.HMACSHA256("POST/ship-mc-item-order"+json,neteasestore.SecretKey);
                //System.out.println("Encrypt:"+KeyCode);
            }catch (Exception e){
                //System.out.println("加密错误");
            }
            Map<String,String> Headers= new HashMap<>();
            Headers.put("content-type","application/json; charset=utf-8");
            Headers.put("netease-server-sign", KeyCode);
            Headers.put("cache-control","no-cache");
            HttpRequest.post(neteasestore.SendGoodUrl).headers(Headers).send(json).body();
            //System.out.println(body);
            neteasestore.PlayerGoodInfo.remove(uniqueId);
        }

        return true;
    }
}