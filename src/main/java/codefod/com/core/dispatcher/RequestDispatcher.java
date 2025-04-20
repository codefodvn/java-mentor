package codefod.com.core.dispatcher;

import codefod.com.core.filter.FilterChain;
import codefod.com.core.filter.ServletFilter;
import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// The request dispatcher is responsible for routing incoming requests to the appropriate servlet
public class RequestDispatcher {
    private final Map<String, Servlet> servletMappings = new HashMap<>();
    private final Map<Pattern, Servlet> regexMappings = new HashMap<>();
    private final Map<String, String> jspMappings = new HashMap<>();
    private final List<ServletFilter> filters = new ArrayList<>();

    public void registerServlet(String path, Servlet servlet) {
        servletMappings.put(path, servlet);
    }

    public void registerServletWithRegex(String pathPattern, Servlet servlet) {
        regexMappings.put(Pattern.compile(pathPattern), servlet);
    }

    public void registerJsp(String path, String jspFile) {
        jspMappings.put(path, jspFile);
    }

    public void addFilter(ServletFilter filter) {
        filters.add(filter);
    }

    public void dispatch(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();

        // Create a filter chain
        FilterChain filterChain = new FilterChain(filters, servlet -> {
            try {
                processRequest(servlet, request, response);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatusCode(500);
                try {
                    response.write("<html><body><h1>500 Internal Server Error</h1></body></html>");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Find the matching servlet
        Servlet servlet = servletMappings.get(path);

        // If no direct match, try regex mappings
        if (servlet == null) {
            for (Map.Entry<Pattern, Servlet> entry : regexMappings.entrySet()) {
                if (entry.getKey().matcher(path).matches()) {
                    servlet = entry.getValue();
                    break;
                }
            }
        }

        // If still no match, check for JSP
        if (servlet == null && jspMappings.containsKey(path)) {
            // This would be handled by a JSP engine in a real implementation
            String jspFile = jspMappings.get(path);
            response.write("<html><body>JSP: " + jspFile + " would be rendered here</body></html>");
            return;
        }

        // Execute the filter chain with the servlet
        if (servlet != null) {
            filterChain.doFilter(request, response, servlet);
        } else {
            // No matching servlet or JSP
            response.setStatusCode(404);
            response.write("<html><body><h1>404 Not Found</h1><p>The requested resource was not found.</p></body></html>");
        }
    }

    private void processRequest(Servlet servlet, HttpRequest request, HttpResponse response) throws Exception {
        // Handle based on HTTP method
        String method = request.getMethod();
        switch (method) {
            case "GET" -> servlet.doGet(request, response);
            case "POST" -> servlet.doPost(request, response);
            case "PUT" -> servlet.doPut(request, response);
            case "DELETE" -> servlet.doDelete(request, response);
            case null, default -> {
                response.setStatusCode(405);
                response.write("<html><body><h1>405 Method Not Allowed</h1></body></html>");
            }
        }
    }
}
