package com.shuzijun.leetcode.plugin.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class CookieUtils {

    public static String toJSONString(List<BasicClientCookie> cookieList) {
        JSONArray jsonArray = new JSONArray();
        for (BasicClientCookie basicClientCookie : cookieList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", basicClientCookie.getName());
            jsonObject.put("value", basicClientCookie.getValue());
            jsonObject.put("domain", basicClientCookie.getDomain());
            jsonObject.put("path", basicClientCookie.getPath());
            jsonArray.add(jsonObject);
        }
        return jsonArray.toJSONString();
    }

    public static String httpCookieToJSONString(List<HttpCookie> cookieList) {
        JSONArray jsonArray = new JSONArray();
        for (HttpCookie basicClientCookie : cookieList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", basicClientCookie.getName());
            jsonObject.put("value", basicClientCookie.getValue());
            jsonObject.put("domain", basicClientCookie.getDomain());
            jsonObject.put("path", basicClientCookie.getPath());
            jsonArray.add(jsonObject);
        }
        return jsonArray.toJSONString();
    }

    public static List<BasicClientCookie> toCookie(String json) {

        List<BasicClientCookie> cookieList = new ArrayList<>();

        JSONArray jsonArray = JSONArray.parseArray(json);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            BasicClientCookie clientCookie = new BasicClientCookie(jsonObject.getString("name"), jsonObject.getString("value"));
            clientCookie.setDomain(jsonObject.getString("domain"));
            clientCookie.setPath(jsonObject.getString("path"));
            cookieList.add(clientCookie);
        }

        return cookieList;
    }

    public static List<HttpCookie> toHttpCookie(String json) {

        List<HttpCookie> cookieList = new ArrayList<>();

        JSONArray jsonArray = JSONArray.parseArray(json);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            HttpCookie clientCookie = new HttpCookie(jsonObject.getString("name"), jsonObject.getString("value"));
            clientCookie.setDomain(jsonObject.getString("domain"));
            clientCookie.setPath(jsonObject.getString("path"));
            clientCookie.setMaxAge(7 * 24 * 60);
            cookieList.add(clientCookie);
        }

        return cookieList;
    }

}
