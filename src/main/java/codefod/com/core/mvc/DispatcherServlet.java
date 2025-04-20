package codefod.com.core.mvc;

import codefod.com.core.dispatcher.HttpServlet;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;

public class DispatcherServlet extends HttpServlet {
    private final ControllerRegistry registry;

    public DispatcherServlet(ControllerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    public void doPut(HttpRequest request, HttpResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    public void doDelete(HttpRequest request, HttpResponse response) throws IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpRequest request, HttpResponse response) throws IOException {
        try {
            if (!registry.handleRequest(request, response)) {
                response.setStatusCode(404);
                response.write("<html><body><h1>404 Not Found</h1></body></html>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(500);
            response.write("<html><body><h1>500 Internal Server Error</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }
}
