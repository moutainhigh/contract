package com.haier.hailian.contract.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.Date;

/**
 * Created by J.wind
 * At 2019-12-25 17:38
 * Email: jiangzd102@outlook.com
 */
public class IHaierUtil {


    /**
     * 获取token的接口
     *
     * @return
     */
    private static String getAccessToken(String secret) {
        OkHttpClient client = new OkHttpClient();
//        QQ8krXQuAuVzlRWsFoaMC5yV9chBDNsq
//        TUW0n1TAW8FYkALRHBS7OfYFQP9GezvB
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        Date date = new Date();
        long timestamp = date.getTime();
        RequestBody body = RequestBody.create(mediaType, "eid=102&secret="+secret+"&timestamp=" + timestamp + "&scope=resGroupSecret");
        Request request = new Request.Builder()
                .url("https://i.haier.net/gateway/oauth2/token/getAccessToken")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "i.haier.net")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            JsonParser parse = new JsonParser();  //创建json解析器
            Response response = client.newCall(request).execute();
            JsonObject json = (JsonObject) parse.parse(response.body().string());  //创建jsonObject对象
            JsonObject result = json.get("data").getAsJsonObject();
            return result.get("accessToken").getAsString();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取用于通讯录的token的接口
     *
     * @return
     */
    private static String getAccessTokenAll() {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        Date date = new Date();
        long timestamp = date.getTime();
        RequestBody body = RequestBody.create(mediaType, "{\"timestamp\":\"" + timestamp + "\"}");
        Request request = new Request.Builder()
                .url("http://apigw.haier.net/getopenid/gateway/oauth2/token/getAccessToken")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("api_gateway_auth_app_id", "8bbe7c30-2f11-48e6-9450-2a88a3c29eba")
                .addHeader("api_gateway_auth_app_password", "Dyc8lCbuv6j!5^t")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "apigw.haier.net")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            JsonParser parse = new JsonParser();  //创建json解析器
            Response response = client.newCall(request).execute();
            JsonObject json = (JsonObject) parse.parse(response.body().string());  //创建jsonObject对象
            JsonObject result = json.get("data").getAsJsonObject();
            return result.get("accessToken").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }


    /**
     * 返回创建任务的任务ID
     *
     * @param ihaierTask
     * @return
     */
    public static String getTaskId(String ihaierTask) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, ihaierTask);
        String accessToken = getAccessToken("QQ8krXQuAuVzlRWsFoaMC5yV9chBDNsq");
        if (accessToken == null) {
            return null;
        }
        Request request = new Request.Builder()
                .url("https://i.haier.net/gateway/cloudwork/worktask/create?accessToken=" + accessToken)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .addHeader("cache-control", "no-cache")
                .build();
        try {
            JsonParser parse = new JsonParser();  //创建json解析器
            Response response = client.newCall(request).execute();
            JsonObject json = (JsonObject) parse.parse(response.body().string());  //创建jsonObject对象
            JsonObject result = json.get("data").getAsJsonObject();
            return result.get("workTaskIds").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取用户的openId
     *
     * @param user
     * @return
     */
    public static String getUserOpenId(String[] user) {
        OkHttpClient client = new OkHttpClient();
        String userIds = new Gson().toJson(user);
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "data={\"array\":" + userIds + "}");
        Request request = new Request.Builder()
                .url("http://apigw.haier.net/getopenid/openimport/open/person/getInfoByJobNo")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("api_gateway_auth_app_id", "8bbe7c30-2f11-48e6-9450-2a88a3c29eba")
                .addHeader("api_gateway_auth_app_password", "Dyc8lCbuv6j!5^t")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "apigw.haier.net")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            String workTaskId = "";
            JsonParser parse = new JsonParser();  //创建json解析器
            Response response = client.newCall(request).execute();
            JsonObject json = (JsonObject) parse.parse(response.body().string());  //创建jsonObject对象
            JsonArray result = json.get("data").getAsJsonArray();
            for (int i = 0; i < result.size(); i++) {
                if (i == 0) {
                    workTaskId = result.get(i).getAsString();
                } else {
                    workTaskId = workTaskId + "," + result.get(i).getAsString();
                }
            }
            return workTaskId;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 创建群组
     *
     * @param user
     * @return
     */
    public static String getGroupId(String[] user) {
        OkHttpClient client = new OkHttpClient();
        String userIds = new Gson().toJson(user);
        String accessToken = getAccessToken("TUW0n1TAW8FYkALRHBS7OfYFQP9GezvB");
        if (accessToken == null) {
            return null;
        }
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"groupName\":\"协同预案交流群\",\"currentUid\":\"5e030c81ed50999dad27824d\",\"userIds\":" + userIds + ",\"param\":{\"groupClass\":\"链群\"}}");
        Request request = new Request.Builder()
                .url("https://i.haier.net/gateway/xtinterface/group/createGroup?accessToken=" + accessToken)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "920ddce3-c447-4690-a5c2-7c9301bc1fcc")
                .build();

        try {
            JsonParser parse = new JsonParser();  //创建json解析器
            Response response = client.newCall(request).execute();
            JsonObject json = (JsonObject) parse.parse(response.body().string());  //创建jsonObject对象
            JsonObject result = json.get("data").getAsJsonObject();
            String groupId = result.get("groupId").getAsString();
            createGG(groupId, accessToken);
            return result.get("groupId").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createGG(String groupId, String accessToken) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"currentUid\": \"5e030c81ed50999dad27824d\",\"groupId\": \"" + groupId + "\",\"title\": \"并联协同公告\",\"content\": \"请进行进一步的讨论！\"}");
        Request request = new Request.Builder()
                .url("https://i.haier.net/gateway/xtinterface/notice/create?accessToken=" + accessToken)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "i.haier.net")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            JsonParser parse = new JsonParser();  //创建json解析器
            Response response = client.newCall(request).execute();
            JsonObject json = (JsonObject) parse.parse(response.body().string());  //创建jsonObject对象
            JsonObject result = json.get("data").getAsJsonObject();
            return result.get("noticeId").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
