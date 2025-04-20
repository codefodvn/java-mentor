package codefod.com.core.filter;

import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;

public interface ServletFilter {
    void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws IOException;
}

