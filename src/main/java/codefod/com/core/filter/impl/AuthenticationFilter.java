package codefod.com.core.filter.impl;

import codefod.com.core.filter.FilterChain;
import codefod.com.core.filter.ServletFilter;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;


public class AuthenticationFilter implements ServletFilter {
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Continue the filter chain
            chain.doFilter(request, response, chain.getServlet());
        } else {
            // Unauthorized
            response.setStatusCode(401);
            response.setHeader("WWW-Authenticate", "Bearer realm=\"api\"");
            response.write("<html><body><h1>401 Unauthorized</h1></body></html>");
        }
    }
}