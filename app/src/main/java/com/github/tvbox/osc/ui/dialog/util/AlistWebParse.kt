package com.github.tvbox.osc.ui.dialog.util

import com.github.UA
import com.github.tvbox.osc.bean.DriveFolderFile
import com.github.tvbox.osc.viewmodel.drive.AbstractDriveViewModel
import com.github.tvbox.osc.viewmodel.drive.AlistDriveViewModel
import com.github.tvbox.osc.viewmodel.drive.AlistDriveViewModel.LoadFileCallback
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.PostRequest
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * <pre>
 *     author : derek
 *     time   : 2022/11/18
 *     desc   :
 *     version:
 * </pre>
 */
class AlistWebParse(val alistDriveViewModel: AlistDriveViewModel) {

    private fun setRequestHeader(request: PostRequest<*>, origin: String) {
        var origin: String? = origin
        request.headers("User-Agent", UA.random())
        if (origin != null && !origin.isEmpty()) {
            if (origin.endsWith("/")) origin = origin.substring(0, origin.length - 1)
            request.headers("origin", origin)
        }
        request.headers("accept", "application/json, text/plain, */*")
        request.headers("content-type", "application/json;charset=UTF-8")
    }


    fun parseAlistList(
        url: String,
        callback: AbstractDriveViewModel.LoadDataCallback,
    ) {
        val webLink = url + "api/fs/list"

        val targetPath: String =
            alistDriveViewModel.currentDriveNote.accessingPathStr + alistDriveViewModel.currentDriveNote.name
        val currentDrive = alistDriveViewModel.currentDrive
        val requestBody = JSONObject()
        requestBody.put("path", targetPath.ifEmpty { "/" })
        requestBody.put("password", currentDrive.config.get("password").asString)
        requestBody.put("page_num", 1)
        requestBody.put("page_size", 30)
        val request =
            OkGo.post<String>(webLink).tag("drive").cacheTime(10 * 60 * 1000);
        request.cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)
        request.cacheKey(request.url + requestBody.get("path"))
        request.upJson(requestBody)
        setRequestHeader(request, webLink)
        request.execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>?) {
                parseFileListData(response, callback)
            }

            override fun onCacheSuccess(response: Response<String>?) {
                super.onCacheSuccess(response)
                parseFileListData(response, callback)
            }

            override fun onError(response: Response<String>?) {
                super.onError(response)
                callback.fail("当前网盘内容不支持|或清除缓存后重试")
            }

        })


    }

    private fun parseFileListData(
        response: Response<String>?,
        callback: AbstractDriveViewModel.LoadDataCallback
    ) {
        val respData = JsonParser.parseString(response?.body()).asJsonObject
        if (respData.get("code").asInt == 200) {
            val items: MutableList<DriveFolderFile> = mutableListOf()
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            try {
                var jsonArray = respData.getAsJsonObject("data").get("content")
                if (jsonArray.isJsonNull) {
                    callback.fail("当前目录是空")
                } else {
                    jsonArray = jsonArray.asJsonArray
                    for (i in 0 until jsonArray.size()) {
                        val jsonObject = jsonArray.get(i).asJsonObject
                        val name = jsonObject.get("name").asString
                        val fileType = jsonObject.get("type").asInt.toString()//1的话是文件夹。2是视频
                        val isDir = jsonObject.get("is_dir").asBoolean
                        val updateTime = jsonObject.get("modified").asString

                        val extNameStartIndex: Int = name.lastIndexOf(".")
                        val driveFile = DriveFolderFile(
                            alistDriveViewModel.currentDriveNote,
                            name,
                            !isDir,
                            if (fileType == "2") name.substring(extNameStartIndex + 1) else fileType,
                            dateFormat.parse(updateTime).time
                        )
                        items.add(driveFile)
                    }
                    alistDriveViewModel.sortData(items)
                    val backItem = DriveFolderFile(null, null, false, null, null)
                    backItem.parentFolder = backItem
                    items.add(0, backItem)
                    alistDriveViewModel.currentDriveNote.children = items
                    callback.callback(
                        alistDriveViewModel.currentDriveNote.children,
                        false
                    )
                }
            } catch (e: Exception) {
                callback.fail("当前网盘内容不支持|或清除缓存后重试")
            }


        }
    }

    fun loadFile(targetFile: DriveFolderFile, callback: LoadFileCallback?) {
        if (targetFile.fileUrl != null && targetFile.fileUrl.isNotEmpty()) {
            callback?.callback(targetFile.fileUrl)
        } else {
            val config: JsonObject = alistDriveViewModel.currentDrive.config
            val targetPath = (targetFile.accessingPathStr + targetFile.name)
            val webLink = config["url"].asString
            val request: PostRequest<String> =
                OkGo.post<String>(webLink + "api/fs/get").tag("drive");

            try {
                val requestBody = JSONObject()
                requestBody.put(
                    "password",
                    alistDriveViewModel.currentDrive.config.get("password")
                        .asString
                )
                requestBody.put("page_num", 1)
                requestBody.put("page_size", 30)
                requestBody.put("path", targetPath)
                request.cacheTime(10 * 60 * 1000)
                    .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)
                    .cacheKey(request.url + requestBody.get("path"))
                request.upJson(requestBody)
                request.execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        try {
                            val respBody = response?.body()
                            val respData = JsonParser.parseString(respBody).asJsonObject
                            if (respData["code"].asInt == 200) {
                                val urlData = respData["data"].asJsonObject
                                callback?.callback(urlData["raw_url"].asString)
                            }
                        } catch (e: Exception) {
                            callback?.fail("不能获取该视频地址")
                        }

                    }

                    override fun onCacheSuccess(response: Response<String>?) {
                        super.onCacheSuccess(response)
                        try {
                            val respBody = response?.body()
                            val respData = JsonParser.parseString(respBody).asJsonObject
                            if (respData["code"].asInt == 200) {
                                val urlData = respData["data"].asJsonObject
                                callback?.callback(urlData["raw_url"].asString)
                            }
                        } catch (e: Exception) {
                            callback?.fail("不能获取该视频地址")
                        }
                    }

                })
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                callback?.fail("不能获取该视频地址")
            }
        }
    }
}