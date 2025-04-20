package codefod.com.core.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private String protocol;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private String body;
    private final Map<String, Object> attributes = new HashMap<>();

    public HttpRequest(String rawRequest) {
        parseRequest(rawRequest);
    }

    private void parseRequest(String rawRequest) {
        String[] lines = rawRequest.split("\r\n");
        if (lines.length > 0) {
            // Parse request line
            String[] requestLine = lines[0].split(" ");
            if (requestLine.length >= 3) {
                method = requestLine[0];
                parsePathAndParameters(requestLine[1]);
                protocol = requestLine[2];
            }

            // Parse headers
            int i = 1;
            while (i < lines.length && !lines[i].isEmpty()) {
                String[] headerParts = lines[i].split(": ", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
                i++;
            }

            // Parse body if present
            if (i < lines.length - 1) {
                StringBuilder bodyBuilder = new StringBuilder();
                for (int j = i + 1; j < lines.length; j++) {
                    bodyBuilder.append(lines[j]);
                    if (j < lines.length - 1) {
                        bodyBuilder.append("\r\n");
                    }
                }
                body = bodyBuilder.toString();

                // If it's a form submission, parse parameters from body
                if ("POST".equals(method) &&
                        headers.getOrDefault("Content-Type", "").equals("application/x-www-form-urlencoded")) {
                    parseParameters(body);
                }
            }
        }
    }

    private void parsePathAndParameters(String fullPath) {
        String[] parts = fullPath.split("\\?", 2);
        path = parts[0];

        if (parts.length > 1) {
            parseParameters(parts[1]);
        }
    }

    private void parseParameters(String paramString) {
        String[] pairs = paramString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1]);
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setAttribute(String variableName, String variableValue) {
        attributes.put(variableName, variableValue);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
