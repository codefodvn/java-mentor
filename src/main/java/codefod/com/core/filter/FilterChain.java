package codefod.com.core.filter;

import codefod.com.core.dispatcher.Servlet;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class FilterChain {
    private final List<ServletFilter> filters;
    private int currentFilter = 0;
    private final Consumer<Servlet> servletProcessor;
    private Servlet servlet;

    public FilterChain(List<ServletFilter> filters, Consumer<Servlet> servletProcessor) {
        this.filters = filters;
        this.servletProcessor = servletProcessor;
    }

    public void doFilter(HttpRequest request, HttpResponse response, Servlet servlet) throws IOException {
        this.servlet = servlet;

        if (currentFilter < filters.size()) {
            ServletFilter filter = filters.get(currentFilter++);
            filter.doFilter(request, response, this);
        } else {
            // End of filter chain, process the servlet
            servletProcessor.accept(servlet);
        }
    }

    public Servlet getServlet() {
        return servlet;
    }
}