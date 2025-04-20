package codefod.com.filter;

import codefod.com.core.annotation.WebFilter;
import codefod.com.core.filter.FilterChain;
import codefod.com.core.filter.ServletFilter;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;

@WebFilter("/*")
public class RequestLoggingFilter implements ServletFilter {

    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws IOException {
        long startTime = System.currentTimeMillis();

        System.out.println("[REQUEST] " + request.getMethod() + " " + request.getPath());

        chain.doFilter(request, response, chain.getServlet());

        long endTime = System.currentTimeMillis();
        System.out.println("[RESPONSE] " + request.getMethod() + " " + request.getPath() +
                " - Processing time: " + (endTime - startTime) + "ms");
    }

}