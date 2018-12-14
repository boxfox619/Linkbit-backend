package com.boxfox.linkbit.service.network;

import com.boxfox.vertx.service.AsyncService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestService {

    public static void request(String url, Handler<AsyncResult<String>> resultHandler) {
        AsyncService.getInstance().doAsync("http-request", taskFuture -> {
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                taskFuture.complete(response.toString());
            } catch (IOException e) {
                taskFuture.fail(e);
            }
        }, resultHandler);
    }
}
