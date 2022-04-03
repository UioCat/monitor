package com.uio.monitor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
public class URLConnection {

    /**
     * post请求封装 参数为?a=1&b=2&c=3
     * @param path 接口地址
     * @param Info 参数
     * @return
     * @throws IOException
     */
    public static String getResponse(String path,String Info) throws IOException{

        //1, 得到URL对象
        URL url = new URL(path);

        //2, 打开连接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        //3, 设置提交类型
        conn.setRequestMethod("GET");

        //4, 设置允许写出数据,默认是不允许 false
        conn.setDoOutput(true);
        conn.setDoInput(true);//当前的连接可以从服务器读取内容, 默认是true

        //5, 获取向服务器写出数据的流
        OutputStream os = conn.getOutputStream();
        //参数是键值队  , 不以"?"开始
        os.write(Info.getBytes());
        //os.write("googleTokenKey=&username=admin&password=5df5c29ae86331e1b5b526ad90d767e4".getBytes());
        os.flush();
        //6, 获取响应的数据
        //得到服务器写回的响应数据
        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
        String str = br.readLine();

        return str;
    }

    public static void main(String[] args) {
        doPost("http://localhost:8080/sendWeChatMessage", "test for user-Agent");
    }

    /**
     * post请求，json格式
     * @param url url
     * @return
     */
    public static String doPost(String url, String bodyString) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(bodyString, mediaType);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if(response.code() == 200) {
                return Objects.requireNonNull(response.body()).string();
            }
        } catch (NullPointerException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}