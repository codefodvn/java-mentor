package codefod.com.core.filter.impl;

import codefod.com.core.filter.FilterChain;
import codefod.com.core.filter.ServletFilter;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;

public class LoggingFilter implements ServletFilter {
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws IOException {
        System.out.println("[LOG] Before: " + request.getMethod() + " " + request.getPath());

        // Continue the filter chain
        chain.doFilter(request, response, chain.getServlet());

        System.out.println("[LOG] After: Processed request");
    }
}