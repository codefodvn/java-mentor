package codefod.com.core.dispatcher;

import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;

public interface Servlet {
    default void init() throws Exception {}

    default void destroy() {}

    default void doGet(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatusCode(405); // Method Not Allowed
        response.write("<html><body><h1>405 Method Not Allowed</h1></body></html>");
    }

    default void doPost(HttpRequest request, HttpResponse response) throws IOException {
        doGet(request, response);
    }

    default void doPut(HttpRequest request, HttpResponse response) throws IOException {
        doGet(request, response);
    }

    default void doDelete(HttpRequest request, HttpResponse response) throws IOException {
        doGet(request, response);
    }
}

