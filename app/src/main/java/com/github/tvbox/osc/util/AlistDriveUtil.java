package com.github.tvbox.osc.util;

import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.StorageDrive;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : derek
 *     time   : 2022/11/22
 *     desc   : 将配置文件里的alist存入到数据库中
 *     version:
 * </pre>
 */
public class AlistDriveUtil {

    private static void saveAlist(String alistName, String alistPath) {
        if (!alistPath.endsWith("/"))
            alistPath += "/";
        JsonObject config = new JsonObject();
        config.addProperty("url", alistPath);
        config.addProperty("initPath", "");
        config.addProperty("password", "");
        RoomDataManger.insertDriveRecord(alistName, StorageDriveType.TYPE.ALISTWEB, config);
    }

    public static void saveAlist(JsonObject jsonObject) {

        if (!Hawk.get(HawkConfig.DRIVE_AUTO_SAVE, false)) {
            return;
        }
        List<StorageDrive> allDrives = RoomDataManger.getAllDrives();
        HashMap<String, StorageDrive> containsDrive = new HashMap<>();
        for (StorageDrive driveItem : allDrives) {
            JsonObject configJsonObj = JsonParser.parseString(driveItem.configJson).getAsJsonObject();
            containsDrive.put(configJsonObj.get("url").getAsString(), driveItem);
        }
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
            try {
                String newUrl = entry.getValue().getAsString();
                if (!newUrl.endsWith("/"))
                    newUrl += "/";
                StorageDrive driveItem = containsDrive.get(newUrl);
                if (driveItem != null) {
                    driveItem.name = entry.getKey();
                    RoomDataManger.updateDriveRecord(driveItem);
                } else {
                    saveAlist(entry.getKey(), newUrl);
                }
            } catch (Exception e) {
                JsonArray jsonArray = entry.getValue().getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    JsonObject jsonObject1 = jsonElement.getAsJsonObject();
                    String name = jsonObject1.get("name").getAsString();
                    String url = jsonObject1.get("server").getAsString();
                    StorageDrive driveItem = containsDrive.get(url);
                    if (driveItem != null) {
                        driveItem.name = name;
                        RoomDataManger.updateDriveRecord(driveItem);
                    } else {
                        saveAlist(name, url);
                    }
                }

            }

        }
    }
}
