package com.github.tvbox.osc.api;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.blankj.utilcode.util.ToastUtils;
import com.github.catvod.crawler.JarLoader;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.bean.LiveChannelGroup;
import com.github.tvbox.osc.bean.LiveChannelItem;
import com.github.tvbox.osc.bean.LiveSourceBean;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.util.AES;
import com.github.tvbox.osc.util.AdBlocker;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.KVStorage;
import com.github.tvbox.osc.util.MD5;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.orhanobut.hawk.Hawk;
import com.undcover.freedom.pyramid.PythonLoader;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class ApiConfig {
    private static ApiConfig instance;
    private LinkedHashMap<String, SourceBean> sourceBeanList;
    private SourceBean mHomeSource;
    private ParseBean mDefaultParse;
    private List<LiveChannelGroup> liveChannelGroupList;
    private List<ParseBean> parseBeanList;
    private List<String> vipParseFlags;
    private List<IJKCode> ijkCodes;
    private String spider = null;
    public String wallpaper = "";
    private SourceBean emptyHome = new SourceBean();

    private JarLoader jarLoader = new JarLoader();

    public static String userAgent = "okhttp/3.15";

    public static String requestAccept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";

    private ApiConfig() {
        sourceBeanList = new LinkedHashMap<>();
        liveChannelGroupList = new ArrayList<>();
        parseBeanList = new ArrayList<>();
    }

    public static ApiConfig get() {
        if (instance == null) {
            synchronized (ApiConfig.class) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

    public static void release() {
        instance = null;
    }

    public static String FindResult(String json, String configKey) {
        String content = json;
        try {
            if (AES.isJson(content)) return content;
            if(content.contains("**")){
                String[] data = json.split("\\*\\*");
                content = new String(Base64.decode(data[1], Base64.DEFAULT));
            }
            if (content.startsWith("2423")) {
                String data = content.substring(content.indexOf("2324") + 4, content.length() - 26);
                content = new String(AES.toBytes(content)).toLowerCase();
                String key = AES.rightPadding(content.substring(content.indexOf("$#") + 2, content.indexOf("#$")), "0", 16);
                String iv = AES.rightPadding(content.substring(content.length() - 13), "0", 16);
                json = AES.CBC(data, key, iv);
            }else if (configKey !=null && !AES.isJson(content)) {
                json = AES.ECB(content, configKey);
            }
            else{
                json = content;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public void loadConfig(boolean useCache, LoadConfigCallback callback, Activity activity) {
        String apiUrl = Hawk.get(HawkConfig.API_URL, "");
        if (apiUrl.isEmpty()) {
            callback.error("-1");
            return;
        }
        File cache = new File(App.getInstance().getFilesDir().getAbsolutePath() + "/" + MD5.encode(apiUrl));
        if (useCache && cache.exists()) {
            try {
                parseJson(apiUrl, cache);
                callback.success();
                return;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        String TempKey = null, configUrl = "", pk = ";pk;";
        if (apiUrl.contains(pk)) {
            String[] a = apiUrl.split(pk);
            TempKey = a[1];
            if (apiUrl.startsWith("clan")){
                configUrl = clanToAddress(a[0]);
            }else if (apiUrl.startsWith("http")){
                configUrl = a[0];
            }else {
                configUrl = "http://" + a[0];
            }
        } else if (apiUrl.startsWith("clan")) {
            configUrl = clanToAddress(apiUrl);
        } else if (!apiUrl.startsWith("http")) {
            configUrl = "http://" + configUrl;
        } else {
            configUrl = apiUrl;
        }
        String configKey = TempKey;
        OkGo.<String>get(configUrl)
                .headers("User-Agent", userAgent)
                .headers("Accept", requestAccept)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String json = response.body();
                            json = FindResult(json, configKey);
                            parseJson(apiUrl, json);
                            try {
                                File cacheDir = cache.getParentFile();
                                if (!cacheDir.exists())
                                    cacheDir.mkdirs();
                                if (cache.exists())
                                    cache.delete();
                                FileOutputStream fos = new FileOutputStream(cache);
                                fos.write(json.getBytes("UTF-8"));
                                fos.flush();
                                fos.close();
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                            callback.success();
                        } catch (Throwable th) {
                            th.printStackTrace();
                            callback.error("解析配置失败");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        if (cache.exists()) {
                            try {
                                parseJson(apiUrl, cache);
                                callback.success();
                                return;
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        }
                        callback.error("拉取配置失败\n" + (response.getException() != null ? response.getException().getMessage() : ""));
                    }

                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        String result = "";
                        if (response.body() == null) {
                            result = "";
                        } else {
                            result = response.body().string();
                        }
                        if (apiUrl.startsWith("clan")) {
                            result = clanContentFix(clanToAddress(apiUrl), result);
                        }
                        //假相對路徑
                        result = fixContentPath(apiUrl,result);
                        return result;
                    }
                });
    }


    public void loadJar(boolean useCache, String spider, LoadConfigCallback callback) {
        String[] urls = spider.split(";md5;");
        String jarUrl = urls[0];
        String md5 = urls.length > 1 ? urls[1].trim() : "";
        File cache = new File(App.getInstance().getFilesDir().getAbsolutePath() + "/csp.jar");
        if (!md5.isEmpty() || useCache) {
            if (cache.exists() && useCache && MD5.getFileMd5(cache).equalsIgnoreCase(md5)) {
                if (jarLoader.load(cache.getAbsolutePath())) {
                    callback.success();
                } else {
                    callback.error("jar 缓存加载失败");
                }
                return;
            }
        }
        GetRequest<File> request = OkGo.<File>get(jarUrl).tag("downLoadJar");
        request.headers("User-Agent", userAgent);
        request.execute(new AbsCallback<File>() {

            @Override
            public File convertResponse(okhttp3.Response response) throws Throwable {
                File cacheDir = cache.getParentFile();
                if (!cacheDir.exists())
                    cacheDir.mkdirs();
                if (cache.exists())
                    cache.delete();
                FileOutputStream fos = new FileOutputStream(cache);
                fos.write(response.body().bytes());
                        fos.flush();
                        fos.close();
                        return cache;
                    }

                    @Override
                    public void onSuccess(Response<File> response) {
                        if (response.body().exists()) {
                            if (jarLoader.load(response.body().getAbsolutePath())) {
                                callback.success();
                            } else {
                                callback.error("jar内部解析失败");
                            }
                        } else {
                            callback.error("jar不存在");
                        }
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        callback.error(response.getException().getMessage());
                    }
                });
    }

    private void parseJson(String apiUrl, File f) throws Throwable {
        BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String s = "";
        while ((s = bReader.readLine()) != null) {
            sb.append(s + "\n");
        }
        bReader.close();
        parseJson(apiUrl, sb.toString());
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void parseJson(String apiUrl, String jsonStr) {
        executorService.execute(() -> PythonLoader.getInstance().setConfig(jsonStr));
        JsonObject infoJson = new Gson().fromJson(jsonStr, JsonObject.class);
        // spider
        spider = DefaultConfig.safeJsonString(infoJson, "spider", "");
        // wallpaper
        wallpaper = DefaultConfig.safeJsonString(infoJson, "wallpaper", "");
        // 远端站点源
        SourceBean firstSite = null;
        for (JsonElement opt : infoJson.get("sites").getAsJsonArray()) {
            JsonObject obj = (JsonObject) opt;
            SourceBean sb = new SourceBean();
            String siteKey = obj.get("key").getAsString().trim();
            sb.setKey(siteKey);
            sb.setName(obj.get("name").getAsString().trim());
            sb.setType(obj.get("type").getAsInt());
            sb.setApi(obj.get("api").getAsString().trim());
            sb.setSearchable(DefaultConfig.safeJsonInt(obj, "searchable", 1));
            sb.setQuickSearch(DefaultConfig.safeJsonInt(obj, "quickSearch", 1));
            sb.setFilterable(DefaultConfig.safeJsonInt(obj, "filterable", 1));
            sb.setPlayerUrl(DefaultConfig.safeJsonString(obj, "playUrl", ""));
            if(obj.has("ext") && (obj.get("ext").isJsonArray() || obj.get("ext").isJsonObject())){
                sb.setExt(obj.get("ext").toString());
            }else {
                sb.setExt(DefaultConfig.safeJsonString(obj, "ext", ""));
            }
            sb.setJar(DefaultConfig.safeJsonString(obj, "jar", ""));
            sb.setPlayerType(DefaultConfig.safeJsonInt(obj, "playerType", -1));
            sb.setCategories(DefaultConfig.safeJsonStringList(obj, "categories"));
            sb.setClickSelector(DefaultConfig.safeJsonString(obj, "click", ""));
            if (firstSite == null)
                firstSite = sb;
            sourceBeanList.put(siteKey, sb);
        }
        if (sourceBeanList != null && sourceBeanList.size() > 0) {
            String home = Hawk.get(HawkConfig.HOME_API, "");
            SourceBean sh = getSource(home);
            if (sh == null)
                setSourceBean(firstSite);
            else
                setSourceBean(sh);
        }
        // 需要使用vip解析的flag
        vipParseFlags = DefaultConfig.safeJsonStringList(infoJson, "flags");
        // 解析地址
        parseBeanList.clear();
        for (JsonElement opt : infoJson.get("parses").getAsJsonArray()) {
            JsonObject obj = (JsonObject) opt;
            ParseBean pb = new ParseBean();
            pb.setName(obj.get("name").getAsString().trim());
            pb.setUrl(obj.get("url").getAsString().trim());
            String ext = obj.has("ext") ? obj.get("ext").getAsJsonObject().toString() : "";
            pb.setExt(ext);
            pb.setType(DefaultConfig.safeJsonInt(obj, "type", 0));
            parseBeanList.add(pb);
        }
        // 获取默认解析
        if (parseBeanList != null && parseBeanList.size() > 0) {
            String defaultParse = Hawk.get(HawkConfig.DEFAULT_PARSE, "");
            if (!TextUtils.isEmpty(defaultParse))
                for (ParseBean pb : parseBeanList) {
                    if (pb.getName().equals(defaultParse))
                        setDefaultParse(pb);
                }
            if (mDefaultParse == null)
                setDefaultParse(parseBeanList.get(0));
        }
        loadLiveSourceUrl(apiUrl, infoJson);
        // 广告地址
        for (JsonElement host : infoJson.getAsJsonArray("ads")) {
            AdBlocker.addAdHost(host.getAsString());
        }
        // IJK解码配置
        boolean foundOldSelect = false;
        String ijkCodec = Hawk.get(HawkConfig.IJK_CODEC, "");
        ijkCodes = new ArrayList<>();
        for (JsonElement opt : infoJson.get("ijk").getAsJsonArray()) {
            JsonObject obj = (JsonObject) opt;
            String name = obj.get("group").getAsString();
            LinkedHashMap<String, String> baseOpt = new LinkedHashMap<>();
            for (JsonElement cfg : obj.get("options").getAsJsonArray()) {
                JsonObject cObj = (JsonObject) cfg;
                String key = cObj.get("category").getAsString() + "|" + cObj.get("name").getAsString();
                String val = cObj.get("value").getAsString();
                baseOpt.put(key, val);
            }
            IJKCode codec = new IJKCode();
            codec.setName(name);
            codec.setOption(baseOpt);
            if (name.equals(ijkCodec) || TextUtils.isEmpty(ijkCodec)) {
                codec.selected(true);
                ijkCodec = name;
                foundOldSelect = true;
            } else {
                codec.selected(false);
            }
            ijkCodes.add(codec);
        }
        if (!foundOldSelect && ijkCodes.size() > 0) {
            ijkCodes.get(0).selected(true);
        }
    }

    public void loadLiveSourceUrl(String apiUrl, JsonObject infoJson) {
        // 直播源
        liveChannelGroupList.clear();           //修复从后台切换重复加载频道列表
        try {
            boolean isWebUrl;//是否是网络直播地址
            LiveSourceBean liveSourceBean = KVStorage.getBean(HawkConfig.LIVE_SOURCE_URL_CURRENT, LiveSourceBean.class);
            String liveSource;
            if (liveSourceBean != null) {
                if (liveSourceBean.isOfficial()) {//如果是官方源
                    if (!TextUtils.equals(liveSourceBean.getExtraKey(), apiUrl)) {//如果不是同一个线路下的直播源,以接口下发的为准
                        liveSource = infoJson.get("lives").getAsJsonArray().toString();
                        isWebUrl = true;
                    } else {//如果是同一个线路下的直播源
                        liveSource = "proxy://do=live&type=txt&ext=" + liveSourceBean.getSourceUrl();
                        isWebUrl = false;
                    }
                } else {
                    liveSource = "proxy://do=live&type=txt&ext=" + liveSourceBean.getSourceUrl();
                    isWebUrl = false;
                }
            } else {
                liveSource = infoJson.get("lives").getAsJsonArray().toString();
                isWebUrl = true;
            }
            int index = liveSource.indexOf("proxy://");
            if (index != -1) {
                String realUrl;
                if (isWebUrl) {
                    int endIndex = liveSource.lastIndexOf("\"");
                    realUrl = DefaultConfig.checkReplaceProxy(liveSource.substring(index, endIndex));
                } else {
                    realUrl = DefaultConfig.checkReplaceProxy(liveSource);
                }
                //clan
                String extUrl = Uri.parse(realUrl).getQueryParameter("ext");
                if (extUrl != null && !extUrl.isEmpty()) {
                    String extUrlFix = "";
                    if (extUrl.startsWith("http") || extUrl.startsWith("https")) {
                        extUrlFix = extUrl;
                    } else {
                        extUrlFix = new String(Base64.decode(extUrl, Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8");
                    }

                    if (extUrlFix.startsWith("clan://")) {
                        extUrlFix = clanContentFix(clanToAddress(apiUrl), extUrlFix);
                        extUrlFix = Base64.encodeToString(extUrlFix.getBytes("UTF-8"), Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP);
                        realUrl = realUrl.replace(extUrl, extUrlFix);
                    }
                    //修复直播明文加载错误
                    if (extUrlFix.startsWith("http") || extUrlFix.startsWith("https")) {
                        extUrlFix = Base64.encodeToString(extUrlFix.getBytes("UTF-8"), Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP);
                        realUrl = realUrl.replace(extUrl, extUrlFix);
                    }
                    if (isWebUrl) {
                        liveSourceBean = new LiveSourceBean();
                        liveSourceBean.setSourceName("自带直播");
                        liveSourceBean.setSourceUrl(extUrlFix);
                        liveSourceBean.setOfficial(true);
                        liveSourceBean.setExtraKey(apiUrl);
                        ArrayList<LiveSourceBean> list = (ArrayList<LiveSourceBean>) KVStorage.getList(HawkConfig.LIVE_SOURCE_URL_HISTORY, LiveSourceBean.class);
                        Iterator<LiveSourceBean> iterator = list.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().isOfficial()) {
                                iterator.remove();
                                break;
                            }
                        }
                        list.add(0,liveSourceBean);
                        KVStorage.putList(HawkConfig.LIVE_SOURCE_URL_HISTORY, list);
                        KVStorage.putBean(HawkConfig.LIVE_SOURCE_URL_CURRENT, liveSourceBean);
                    }
                }
                LiveChannelGroup liveChannelGroup = new LiveChannelGroup();
                liveChannelGroup.setGroupName(realUrl);
                liveChannelGroupList.add(liveChannelGroup);
            } else {
                loadLives(infoJson.get("lives").getAsJsonArray());
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void loadLives(JsonArray livesArray) {
        liveChannelGroupList.clear();
        int groupIndex = 0;
        int channelIndex = 0;
        int channelNum = 0;
        for (JsonElement groupElement : livesArray) {
            LiveChannelGroup liveChannelGroup = new LiveChannelGroup();
            liveChannelGroup.setLiveChannels(new ArrayList<LiveChannelItem>());
            liveChannelGroup.setGroupIndex(groupIndex++);
            String groupName = ((JsonObject) groupElement).get("group").getAsString().trim();
            String[] splitGroupName = groupName.split("_", 2);
            liveChannelGroup.setGroupName(splitGroupName[0]);
            if (splitGroupName.length > 1)
                liveChannelGroup.setGroupPassword(splitGroupName[1]);
            else
                liveChannelGroup.setGroupPassword("");
            channelIndex = 0;
            for (JsonElement channelElement : ((JsonObject) groupElement).get("channels").getAsJsonArray()) {
                JsonObject obj = (JsonObject) channelElement;
                LiveChannelItem liveChannelItem = new LiveChannelItem();
                liveChannelItem.setChannelName(obj.get("name").getAsString().trim());
                liveChannelItem.setChannelIndex(channelIndex++);
                liveChannelItem.setChannelNum(++channelNum);
                ArrayList<String> urls = DefaultConfig.safeJsonStringList(obj, "urls");
                ArrayList<String> sourceNames = new ArrayList<>();
                ArrayList<String> sourceUrls = new ArrayList<>();
                int sourceIndex = 1;
                for (String url : urls) {
                    String[] splitText = url.split("\\$", 2);
                    sourceUrls.add(splitText[0]);
                    if (splitText.length > 1)
                        sourceNames.add(splitText[1]);
                    else
                        sourceNames.add("源" + Integer.toString(sourceIndex));
                    sourceIndex++;
                }
                liveChannelItem.setChannelSourceNames(sourceNames);
                liveChannelItem.setChannelUrls(sourceUrls);
                liveChannelGroup.getLiveChannels().add(liveChannelItem);
            }
            liveChannelGroupList.add(liveChannelGroup);
        }
    }

    public String getSpider() {
        return spider;
    }

    public Spider getCSP(SourceBean sourceBean) {
        //pyramid-add-start
        if (sourceBean.getApi().startsWith("py_")) {
            try {
                return PythonLoader.getInstance().getSpider(sourceBean.getKey(), sourceBean.getExt());
            } catch (Exception e) {
                e.printStackTrace();
                return new SpiderNull();
            }
        }
        return jarLoader.getSpider(sourceBean.getKey(), sourceBean.getApi(), sourceBean.getExt(), sourceBean.getJar());
    }

    public Object[] proxyLocal(Map param) {
        //pyramid-add-start
        try {
            String doStr = param.get("do").toString();
            if(param.containsKey("api")){
                if(doStr.equals("ck"))
                    return PythonLoader.getInstance().proxyLocal("","",param);
                SourceBean sourceBean = ApiConfig.get().getSource(doStr);
                return PythonLoader.getInstance().proxyLocal(sourceBean.getKey(),sourceBean.getExt(),param);
            }else{
                if(doStr.equals("live")) return PythonLoader.getInstance().proxyLocal("","",param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jarLoader.proxyInvoke(param);
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) {
        return jarLoader.jsonExt(key, jxs, url);
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) {
        return jarLoader.jsonExtMix(flag, key, name, jxs, url);
    }

    public interface LoadConfigCallback {
        void success();

        void retry();

        void error(String msg);
    }

    public interface FastParseCallback {
        void success(boolean parse, String url, Map<String, String> header);

        void fail(int code, String msg);
    }

    public SourceBean getSource(String key) {
        if (!sourceBeanList.containsKey(key))
            return null;
        return sourceBeanList.get(key);
    }

    public void setSourceBean(SourceBean sourceBean) {
        this.mHomeSource = sourceBean;
        Hawk.put(HawkConfig.HOME_API, sourceBean.getKey());
        if (sourceBean.getKey().startsWith("py_") && !App.getInstance().getPyLoadSuccess()) {
            PythonLoader.getInstance().setApplication(App.getInstance());
            App.getInstance().setPyLoadSuccess(true);
            ToastUtils.showShort("第一次加载py会有点慢，等等~");
        }
    }

    public void setDefaultParse(ParseBean parseBean) {
        if (this.mDefaultParse != null)
            this.mDefaultParse.setDefault(false);
        this.mDefaultParse = parseBean;
        Hawk.put(HawkConfig.DEFAULT_PARSE, parseBean.getName());
        parseBean.setDefault(true);
    }

    public ParseBean getDefaultParse() {
        return mDefaultParse;
    }

    public List<SourceBean> getSourceBeanList() {
        return new ArrayList<>(sourceBeanList.values());
    }

    public List<ParseBean> getParseBeanList() {
        return parseBeanList;
    }

    public List<String> getVipParseFlags() {
        return vipParseFlags;
    }

    public SourceBean getHomeSourceBean() {
        return mHomeSource == null ? emptyHome : mHomeSource;
    }

    public List<LiveChannelGroup> getChannelGroupList() {
        return liveChannelGroupList;
    }

    public List<IJKCode> getIjkCodes() {
        return ijkCodes;
    }

    public IJKCode getCurrentIJKCode() {
        String codeName = Hawk.get(HawkConfig.IJK_CODEC, "");
        return getIJKCodec(codeName);
    }

    public IJKCode getIJKCodec(String name) {
        for (IJKCode code : ijkCodes) {
            if (code.getName().equals(name))
                return code;
        }
        return ijkCodes.get(0);
    }

    public static String clanToAddress(String lanLink) {
        if (lanLink.startsWith("clan://localhost/")) {
            return lanLink.replace("clan://localhost/", ControlManager.get().getAddress(true) + "file/");
        } else {
            String link = lanLink.substring(7);
            int end = link.indexOf('/');
            return "http://" + link.substring(0, end) + "/file/" + link.substring(end + 1);
        }
    }

    String clanContentFix(String lanLink, String content) {
        String fix = lanLink.substring(0, lanLink.indexOf("/file/") + 6);
        return content.replace("clan://", fix);
    }

    String fixContentPath(String url, String content) {
        if (content.contains("\"./")) {
            if(!url.startsWith("http") && !url.startsWith("clan://")){
                url = "http://" + url;
            }
            if(url.startsWith("clan://"))url=clanToAddress(url);
            content = content.replace("./", url.substring(0,url.lastIndexOf("/") + 1));
        }
        return content;
    }


}
