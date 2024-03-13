package net.nitrin.http.request;

import java.util.Map;

public record HttpRequest(Method method, String path, Protocol protocol, Map<String, String> headers, String body) {

    public enum Method {
        GET, POST, PUT, DELETE
    }

    public enum Protocol {
        HTTP_1_0("HTTP/1.0"), HTTP_1_1("HTTP/1.1"), HTTP_2_0("HTTP/2.0");

        private final String base;

        Protocol(String base) {
            this.base = base;
        }

        public String getBase() {
            return base;
        }

        public static Protocol getByBase(String base) {
            for (Protocol protocol : values()) {
                if (protocol.base.equals(base)) {
                    return protocol;
                }
            }
            return null;
        }
    }
}
