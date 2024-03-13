package net.nitrin.http.example;

import net.nitrin.http.HttpServer;
import net.nitrin.http.response.HttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class BaseExample {

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer();
        httpServer.bind(new InetSocketAddress(8888));

        System.out.println("Server listening...");
        httpServer.listen(httpRequest -> {
            System.out.println(httpRequest);
            if (httpRequest.path().equals("/")) {
                String html = "<html lang=\"en\"><head><title>Test</title></head><body><h1>Hello world!</h1><form method=\"post\"><input type=\"email\" id=\"email\" name=\"email\" /><button type=\"submit\">Submit</button></form></body></html>";
                return new HttpResponse(httpRequest.protocol(), HttpResponse.StatusCode.OK, Map.of("Content-Type", "text/html",
                        "Content-Length", String.valueOf(html.length())), html);
            }
            return null;
        });
    }
}
