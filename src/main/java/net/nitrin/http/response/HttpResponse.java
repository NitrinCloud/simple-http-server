package net.nitrin.http.response;

import net.nitrin.http.request.HttpRequest;

import java.util.Map;

public record HttpResponse(HttpRequest.Protocol protocol, StatusCode statusCode, Map<String, String> headers, String base) {

    public enum StatusCode {
        OK("OK", 200), NOT_FOUND("Not Found", 404);

        private final String base;
        private final int code;

        StatusCode(String base, int code) {
            this.base = base;
            this.code = code;
        }

        public String getBase() {
            return base;
        }

        public int getCode() {
            return code;
        }
    }
}
