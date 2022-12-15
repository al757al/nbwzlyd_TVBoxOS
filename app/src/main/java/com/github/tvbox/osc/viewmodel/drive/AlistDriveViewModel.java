package com.github.tvbox.osc.viewmodel.drive;

import com.github.UA;
import com.github.tvbox.osc.bean.DriveFolderFile;
import com.github.tvbox.osc.cache.StorageDrive;
import com.github.tvbox.osc.ui.dialog.util.AlistWebParse;
import com.github.tvbox.osc.util.StorageDriveType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AlistDriveViewModel extends AbstractDriveViewModel {

    public static final String API1_GET = "api/fs/get";
    public static final String API_LIST = "api/fs/list";

    private AlistWebParse alistWebParse = new AlistWebParse(this);

    private void setRequestHeader(PostRequest request, String origin) {
        request.headers("User-Agent", UA.random());
        if (origin != null && !origin.isEmpty()) {
            if (origin.endsWith("/"))
                origin = origin.substring(0, origin.length() - 1);
            request.headers("origin", origin);
        }
        request.headers("accept", "application/json, text/plain, */*");
        request.headers("content-type", "application/json;charset=UTF-8");
    }

    @Override
    public String loadData(LoadDataCallback callback) {
        JsonObject config = currentDrive.getConfig();
        if (currentDriveNote == null) {
            currentDriveNote = new DriveFolderFile(null,
                    config.has("initPath") ? config.get("initPath").getAsString() : "", false, null, null);
        }
        String targetPath = currentDriveNote.getAccessingPathStr() + currentDriveNote.name;
        if (currentDriveNote.getChildren() == null) {
            try {
                String webLink = config.get("url").getAsString();
                JSONObject requestBody = new JSONObject();
                requestBody.put("path", targetPath.isEmpty() ? "/" : targetPath);
                requestBody.put("password", currentDrive.getConfig().get("password").getAsString());
                requestBody.put("page_num", 1);
                requestBody.put("page_size", 30);
                PostRequest request = OkGo.post(webLink + "api/public/path").tag("drive");
                request.cacheTime(3 * 24 * 60 * 60 * 1000).cacheKey(request.getUrl() + requestBody.get("path"))
                        .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST);
                request.upJson(requestBody);
                setRequestHeader(request, webLink);
                request.execute(new AbsCallback<String>() {

                                    @Override
                                    public String convertResponse(okhttp3.Response response) throws Throwable {
                                        return response.body().string();
                                    }

                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        parseFileListData(response, callback);
                                    }

                                    @Override
                                    public void onCacheSuccess(Response<String> response) {
                                        super.onCacheSuccess(response);
                                        parseFileListData(response, callback);
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        super.onError(response);
                                        callback.fail(response.getException().getMessage());
                                    }
                                }
                );
            } catch (Exception ex) {
                callback.fail(ex.getMessage());
                ex.printStackTrace();
            }
            return targetPath;
        } else {
            sortData(currentDriveNote.getChildren());
            if (callback != null)
                callback.callback(currentDriveNote.getChildren(), true);
        }
        return targetPath;
    }

    private void parseFileListData(Response<String> response, LoadDataCallback callback) {
        String respBody = response.body();
        try {
            JsonObject respData = JsonParser.parseString(respBody).getAsJsonObject();
            List<DriveFolderFile> items = new ArrayList<>();
            if (respData.get("code").getAsInt() == 200) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                for (JsonElement file : respData.get("data").getAsJsonObject().get("files").getAsJsonArray()) {
                    JsonObject fileObj = file.getAsJsonObject();
                    String fileName = fileObj.get("name").getAsString();
                    int extNameStartIndex = fileName.lastIndexOf(".");
                    boolean isFile = fileObj.get("type").getAsInt() != 1;
                    String fileUrl = null;
                    if (fileObj.has("url") && !fileObj.get("url").getAsString().isEmpty())
                        fileUrl = fileObj.get("url").getAsString();
                    try {
                        DriveFolderFile driveFile = new DriveFolderFile(currentDriveNote, fileName, isFile,
                                isFile && extNameStartIndex >= 0 && extNameStartIndex < fileName.length() ?
                                        fileName.substring(extNameStartIndex + 1) : null,
                                dateFormat.parse(fileObj.get("updated_at").getAsString()).getTime());
                        if (fileUrl != null)
                            driveFile.fileUrl = fileUrl;
                        items.add(driveFile);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            sortData(items);
            DriveFolderFile backItem = new DriveFolderFile(null, null, false, null, null);
            backItem.parentFolder = backItem;
            items.add(0, backItem);
            currentDriveNote.setChildren(items);
            if (callback != null)
                callback.callback(currentDriveNote.getChildren(), false);
        } catch (Exception ex) {
            //尝试另一种格式的解析
            alistWebParse.parseAlistList(currentDrive.getConfig().get("url").getAsString(), callback);
        }
    }

    @Override
    public Runnable search(String keyword, LoadDataCallback callback) {
        JsonObject config = currentDrive.getConfig();
        if (currentDriveNote == null) {
            currentDriveNote = new DriveFolderFile(null,
                    config.has("initPath") ? config.get("initPath").getAsString() : "", false, null, null);
        }
        String targetPath = currentDriveNote.getAccessingPathStr();
        return new Runnable() {
            @Override
            public void run() {
                String webLink = config.get("url").getAsString();
                PostRequest request = OkGo.post(webLink + "api/public/search").tag("drive");
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("path", targetPath.isEmpty() ? "/" : targetPath);
                    requestBody.put("keyword", keyword);
                    request.upJson(requestBody);
                    setRequestHeader(request, webLink);
                    request.execute(new AbsCallback<String>() {

                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            return response.body().string();
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            String respBody = response.body();
                            try {
                                JsonObject respData = JsonParser.parseString(respBody).getAsJsonObject();
                                List<DriveFolderFile> items = new ArrayList<>();
                                if (respData.get("code").getAsInt() == 200) {
                                    StorageDrive driveData = new StorageDrive();
                                    driveData.type = StorageDriveType.TYPE.ALISTWEB.ordinal();
                                    for (JsonElement file : respData.get("data").getAsJsonArray()) {
                                        JsonObject fileObj = file.getAsJsonObject();
                                        String fileName = fileObj.get("name").getAsString();
                                        int extNameStartIndex = fileName.lastIndexOf(".");
                                        boolean isFile = fileObj.get("type").getAsInt() != 1;
                                        DriveFolderFile driveFile = new DriveFolderFile(null, fileName, isFile,
                                                isFile && extNameStartIndex >= 0 && extNameStartIndex < fileName.length() ?
                                                        fileName.substring(extNameStartIndex + 1) : null,
                                                null);
                                        driveFile.setDriveData(driveData);
                                        driveFile.setAccessingPath(fileObj.get("path").getAsString().split("\\/"));
                                        JsonObject config = currentDrive.getConfig();
                                        config.addProperty("initPath", fileObj.get("path").getAsString() + "/" + fileName);
                                        driveFile.setConfig(config);
                                        items.add(driveFile);
                                    }
                                }
                                if (callback != null)
                                    callback.callback(items, false);
                            } catch (Exception ex) {
                                if (callback != null)
                                    callback.fail("无法访问，请注意地址格式");
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public void loadFile(DriveFolderFile targetFile, LoadFileCallback callback) {
        if (targetFile.fileUrl != null && !targetFile.fileUrl.isEmpty()) {
            if (callback != null)
                callback.callback(targetFile.fileUrl);
        } else {
            JsonObject config = currentDrive.getConfig();
            String targetPath = targetFile.getAccessingPathStr() + targetFile.name;
            String webLink = config.get("url").getAsString();
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("path", targetPath);
                requestBody.put("password", currentDrive.getConfig().get("password").getAsString());
                requestBody.put("page_num", 1);
                requestBody.put("page_size", 30);
                PostRequest request = OkGo.post(webLink + "api/public/path").tag("drive");
                request.cacheMode(CacheMode.IF_NONE_CACHE_REQUEST).cacheKey(request.getUrl() + requestBody.get("path"))
                        .cacheTime(10 * 60 * 1000);
                request.upJson(requestBody);
                setRequestHeader(request, webLink);
                request.execute(new AbsCallback<String>() {

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body().string();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String respBody = response.body();
                        try {
                            JsonObject respData = JsonParser.parseString(respBody).getAsJsonObject();
                            if (respData.get("code").getAsInt() == 200) {
                                JsonArray files = respData.get("data").getAsJsonObject().get("files").getAsJsonArray();
                                if (files.size() > 0 && callback != null) {
                                    callback.callback(files.get(0).getAsJsonObject().get("url").getAsString());
                                }
                            }
                        } catch (Exception e) {
                            alistWebParse.loadFile(targetFile, callback);
                        }

                    }

                    @Override
                    public void onCacheSuccess(Response<String> response) {
                        super.onCacheSuccess(response);
                        String respBody = response.body();
                        try {
                            JsonObject respData = JsonParser.parseString(respBody).getAsJsonObject();
                            if (respData.get("code").getAsInt() == 200) {
                                JsonArray files = respData.get("data").getAsJsonObject().get("files").getAsJsonArray();
                                if (files.size() > 0 && callback != null) {
                                    callback.callback(files.get(0).getAsJsonObject().get("url").getAsString());
                                }
                            }
                        } catch (Exception e) {
                            alistWebParse.loadFile(targetFile, callback);
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                if (callback != null)
                    callback.fail("不能获取该视频地址");
            }
        }

    }

    public interface LoadFileCallback {
        void callback(String fileUrl);

        void fail(String msg);
    }
}