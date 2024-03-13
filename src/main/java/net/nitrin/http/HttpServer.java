package net.nitrin.http;

import net.nitrin.http.request.HttpRequest;
import net.nitrin.http.response.HttpResponse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HttpServer {

    private ServerSocket serverSocket;
    private ExecutorService workers;

    public void bind(SocketAddress address) throws IOException {
        this.workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(address);
    }

    public void listen(Function<HttpRequest, HttpResponse> handler) throws IOException {
        while (!isClosed()) {
            Socket socket = this.serverSocket.accept();

            workers.submit(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();

                    PrintWriter writer = new PrintWriter(socket.getOutputStream());

                    //Creates a BufferedReader that contains the server response
                    StringBuilder requestBuilder = new StringBuilder();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String nextLine;
                    //Prints each line of the response
                    while(!(nextLine = reader.readLine()).isEmpty()){
                        requestBuilder.append(nextLine).append("\n");
                    }

                    String[] lines = requestBuilder.toString().split("\n");

                    String baseRequest = lines[0];
                    String[] baseRequestParameter = baseRequest.split(" ");
                    String baseMethod = baseRequestParameter[0];
                    String basePath = baseRequestParameter[1];
                    String baseProtocol = baseRequestParameter[2];

                    HashMap<String, String> headers = new HashMap<>();
                    int headerIndex = 1;
                    while (headerIndex != lines.length) {
                        String header = lines[headerIndex];
                        headerIndex += 1;
                        if (header.isEmpty()) {
                            break;
                        }
                        int splitIndex = header.indexOf(":");
                        headers.put(header.substring(0, splitIndex), header.substring(splitIndex + 1).trim());
                    }

                    StringBuilder bodyBuilder = new StringBuilder();
                    String contentType = headers.get("Content-Type");
                    if (contentType != null) {
                        String baseLength = headers.get("Content-Length");
                        int length = Integer.parseInt(baseLength);
                        char[] chars = new char[length];
                        int charsRead = reader.read(chars);
                        bodyBuilder.append(new String(chars, 0, charsRead));
                    }

                    HttpRequest httpRequest = new HttpRequest(HttpRequest.Method.valueOf(baseMethod), basePath,
                            HttpRequest.Protocol.getByBase(baseProtocol), headers, (bodyBuilder.toString().isEmpty() ? null : bodyBuilder.toString()));
                    HttpResponse httpResponse = null;
                    if (handler != null) {
                        httpResponse = handler.apply(httpRequest);
                    }
                    if (httpResponse == null) {
                        httpResponse = new HttpResponse(httpRequest.protocol(), HttpResponse.StatusCode.NOT_FOUND, new HashMap<>(), null);
                    }

                    writer.println(httpResponse.protocol().getBase() + " " + httpResponse.statusCode().getCode() + " " + httpResponse.statusCode().getBase());
                    for (String name : httpResponse.headers().keySet()) {
                        writer.println(name + ": " + httpResponse.headers().get(name));
                    }

                    if (httpResponse.base() != null) {
                        writer.println("");
                        writer.println(httpResponse.base());
                    }

                    writer.flush();
                    //reader.close();
                    writer.close();
                } catch (IOException ignored) {
                }
            });
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    public boolean isClosed() {
        return this.serverSocket.isClosed();
    }
}
