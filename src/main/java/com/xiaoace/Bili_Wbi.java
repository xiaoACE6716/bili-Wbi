package com.xiaoace;


import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class Bili_Wbi {

    private static final int[] mixinKeyEncTab ={
            46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
            33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
            61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
            36, 20, 34, 44, 52
    };

    public static void main(String[] args) {

        //https://api.bilibili.com/x/web-interface/nav
        String nav_URL = UrlBuilder.of("https://api.bilibili.com")
                .addPath("x")
                .addPath("web-interface")
                .addPath("nav")
                .build();

        String nav_result = HttpRequest.get(nav_URL).execute().body();

        JSONObject nav_json = JSONUtil.parseObj(nav_result,false);

        String img_url = nav_json.getJSONObject("data").getJSONObject("wbi_img").getStr("img_url");
        String sub_url = nav_json.getJSONObject("data").getJSONObject("wbi_img").getStr("sub_url");

        String img_Key = getKey(img_url);
        String sub_Key = getKey(sub_url);

        String mixinKey = img_Key + sub_Key;
        String mixinKeyReMap = mixinKeyReMap(mixinKey);

        long wts = System.currentTimeMillis()/1000L;

        //https://api.bilibili.com/x/player/wbi/playurl?bvid=BV1yv4y1H7wz&cid=1094702238&fnval=16
        UrlBuilder urlBuilder = UrlBuilder.of("https://api.bilibili.com")
                .addPath("x")
                .addPath("player")
                .addPath("wbi")
                .addPath("playurl")
                .addQuery("bvid","BV1yv4y1H7wz")
                .addQuery("cid","1094702238")
                .addQuery("fnval","16")
                .addQuery("wts",wts);


        String test_URL_1 = urlBuilder.build();

        String w_rid = DigestUtil.md5Hex(getAllQuery(test_URL_1) + mixinKeyReMap);
        urlBuilder.addQuery("w_rid",w_rid);
        String test_URL_2 = urlBuilder.build();
        String test_result = HttpRequest.get(test_URL_2)
                .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .execute().body();
        System.out.println(test_result);
    }

    private static String getKey(String path){

        //获取最后一个斜杠的索引
        int lastIndex = path.lastIndexOf("/");
        //获取文件名部分(含扩展名)
        //PS:其实这玩意并不是真的文件
        String fileNameWithExtension = path.substring(lastIndex + 1);
        //然后去掉后面的扩展名
        int dotIndex = fileNameWithExtension.lastIndexOf(".");
        String key = fileNameWithExtension.substring(0,dotIndex);

        return key;
    }

    private static String mixinKeyReMap(String mixinKey){

        StringBuilder remapBuilder  = new StringBuilder();
        for (int index : mixinKeyEncTab){
            remapBuilder.append(mixinKey.charAt(index));
        }
        return remapBuilder.substring(0,32);
    }

    private static String getAllQuery(String requestUrl){
        //获取最后一个?的索引
        int lastIndex = requestUrl.lastIndexOf("?");
        String allQuery = requestUrl.substring(lastIndex + 1);
        return allQuery;
    }

}
