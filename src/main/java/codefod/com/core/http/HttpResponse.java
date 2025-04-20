package codefod.com.core.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();

    public HttpResponse() {
        headers.put("Content-Type", "text/html");
        headers.put("Server", "Custom Java Server");
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        if (statusCode == 200) this.statusMessage = "OK";
        else if (statusCode == 201) this.statusMessage = "Created";
        else if (statusCode == 204) this.statusMessage = "No Content";
        else if (statusCode == 400) this.statusMessage = "Bad Request";
        else if (statusCode == 404) this.statusMessage = "Not Found";
        else if (statusCode == 500) this.statusMessage = "Internal Server Error";
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void write(String content) throws IOException {
        body.write(content.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();

        // Write status line
        responseBytes.write(("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n").getBytes());

        // Write headers
        headers.put("Content-Length", String.valueOf(body.size()));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            responseBytes.write((header.getKey() + ": " + header.getValue() + "\r\n").getBytes());
        }

        // Separate headers from body
        responseBytes.write("\r\n".getBytes());

        // Write body
        responseBytes.write(body.toByteArray());

        return responseBytes.toByteArray();
    }
}
