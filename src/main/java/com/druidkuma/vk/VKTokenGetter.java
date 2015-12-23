package com.druidkuma.vk;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Iurii Miedviediev
 *
 * @author DruidKuma
 * @version 1.0.0
 * @since 12/23/15
 */
public final class VKTokenGetter {

    private VKTokenGetter() {}

    public static final String AUTH_URL = "http://oauth.vk.com/oauth/authorize?redirect_uri=http://oauth.vk.com/blank.html&response_type=token&client_id=%s&scope=%s&display=wap";
    private static final String CHECKED_PROPERTIES = "p";
    public static final String VK_URL = "https://m.vk.com";

    private static Map<String, String> login(String login, String password) throws IOException {
        Map<String, String> cookies;
        Connection.Response connection = Jsoup.connect(VK_URL).execute();
        String url = getFormActionFromPage(connection);
        Map<String, String> data = new HashMap<String, String>();
        data.put("email", login);
        data.put("pass", password);
        cookies = connection.cookies();
        connection = Jsoup.connect(url).cookies(cookies).data(data).execute();
        cookies = connection.cookies();
        if (!cookies.containsKey(CHECKED_PROPERTIES)) {
            throw new RuntimeException("Invalid login/password");
        }
        return cookies;
    }

    public static String getAccessToken(String appId, String scope, String login, String password) throws IOException {
        Map<String, String> loginCookies = login(login, password);
        Connection.Response response = Jsoup.connect(String.format(AUTH_URL, appId, scope)).cookies(loginCookies).execute();
        if (response.url().getRef() == null) {
            //grant access to the application
            String grantAccessAction = getFormActionFromPage(response);
            response = Jsoup.connect(grantAccessAction).cookies(loginCookies).execute();
        }
        return response.url().getRef().split("&")[0].split("=")[1];
    }

    private static String getFormActionFromPage(Connection.Response pageResponse) throws IOException {
        Document document = pageResponse.parse();
        Element form = document.getElementsByTag("form").get(0);
        return form.attr("action");
    }
}