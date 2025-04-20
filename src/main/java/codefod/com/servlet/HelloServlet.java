package codefod.com.servlet;

import codefod.com.core.annotation.WebServlet;
import codefod.com.core.dispatcher.HttpServlet;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    @Override
    public void doGet(HttpRequest request, HttpResponse response) throws IOException {
        String name = request.getParameter("name");
        if (name == null) {
            name = "World";
        }

        response.write("<html><body>");
        response.write("<h1>Hello, " + name + "!</h1>");
        response.write("<p>This is a simple servlet example</p>");
        response.write("</body></html>");
    }
}
