package codefod.com.core.mvc;

import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;
import codefod.com.core.jsp.JspEngine;
import codefod.com.core.mvc.annotation.Controller;
import codefod.com.core.mvc.annotation.DeleteMapping;
import codefod.com.core.mvc.annotation.GetMapping;
import codefod.com.core.mvc.annotation.PostMapping;
import codefod.com.core.mvc.annotation.PutMapping;
import codefod.com.core.mvc.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ControllerRegistry {
    private final Map<String, ControllerMapping> mappings = new HashMap<>();
    private final JspEngine jspEngine;

    public ControllerRegistry(JspEngine jspEngine) {
        this.jspEngine = jspEngine;
    }

    public void registerController(Object controller) {
        Class<?> controllerClass = controller.getClass();

        if (!controllerClass.isAnnotationPresent(Controller.class)) {
            return;
        }

        String baseUrl = getString(controllerClass);

        // Process methods
        for (Method method : controllerClass.getDeclaredMethods()) {
            String path = null;
            String[] httpMethods = null;

            // Check for different mapping annotations
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                path = !mapping.value().isEmpty() ? mapping.value() : mapping.path();
                httpMethods = mapping.method();
            } else if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping mapping = method.getAnnotation(GetMapping.class);
                path = !mapping.value().isEmpty() ? mapping.value() : mapping.path();
                httpMethods = new String[]{"GET"};
            } else if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping mapping = method.getAnnotation(PostMapping.class);
                path = !mapping.value().isEmpty() ? mapping.value() : mapping.path();
                httpMethods = new String[]{"POST"};
            } else if (method.isAnnotationPresent(PutMapping.class)) {
                PutMapping mapping = method.getAnnotation(PutMapping.class);
                path = !mapping.value().isEmpty() ? mapping.value() : mapping.path();
                httpMethods = new String[]{"PUT"};
            } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
                path = !mapping.value().isEmpty() ? mapping.value() : mapping.path();
                httpMethods = new String[]{"DELETE"};
            }

            if (path != null) {
                String fullPath = baseUrl + (path.startsWith("/") || baseUrl.endsWith("/") || path.isEmpty() ? path : "/" + path);
                ControllerMapping mapping = new ControllerMapping(controller, method, httpMethods);
                mappings.put(fullPath, mapping);
            }
        }
    }

    private static String getString(Class<?> controllerClass) {
        String baseUrl = "";
        Controller controllerAnnotation = controllerClass.getAnnotation(Controller.class);
        if (controllerAnnotation != null && !controllerAnnotation.value().isEmpty()) {
            baseUrl = controllerAnnotation.value();
        }

        // Check for class-level RequestMapping
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
            String mappingValue = !requestMapping.value().isEmpty() ? requestMapping.value() : requestMapping.path();
            if (!mappingValue.isEmpty()) {
                baseUrl = mappingValue;
            }
        }
        return baseUrl;
    }

    public boolean handleRequest(HttpRequest request, HttpResponse response) throws Exception {
        String path = request.getPath();
        ControllerMapping mapping = mappings.get(path.replaceFirst("/app", ""));

        if (mapping == null) {
            // Try to find a matching pattern with path variables
            for (Map.Entry<String, ControllerMapping> entry : mappings.entrySet()) {
                if (pathMatches("/app/" + entry.getKey(), path)) {
                    mapping = entry.getValue();
                    break;
                }
            }
        }

        if (mapping != null) {
            // Check if HTTP method is allowed
            boolean methodAllowed = false;
            for (String allowedMethod : mapping.httpMethods()) {
                if (allowedMethod.equalsIgnoreCase(request.getMethod())) {
                    methodAllowed = true;
                    break;
                }
            }

            if (!methodAllowed) {
                response.setStatusCode(405); // Method Not Allowed
                response.write("<html><body><h1>405 Method Not Allowed</h1></body></html>");
                return true;
            }

            // Execute controller method
            Object result = mapping.execute(request, response);

            // Handle the result
            if (result instanceof String) {
                // View name
                response.write("<html><body><h1>View: " + result + "</h1></body></html>");
            } else if (result instanceof ModelAndView modelAndView) {

                String viewName = modelAndView.getViewName();

                // Convert logical name "users/list" -> "users/list.jsp"
                String jspPath = viewName.endsWith(".jsp") ? viewName : viewName + ".jsp";

                // Call jsp engine
                jspEngine.processJsp(jspPath, request, response, modelAndView.getModel());
            }

            return true;
        }

        return false;
    }

    private boolean pathMatches(String pattern, String path) {
        // Simple path matching logic
        if (pattern.equals(path)) {
            return true;
        }

        // Check for path variables
        if (pattern.contains("{") && pattern.contains("}")) {
            String[] patternParts = pattern.split("/");
            String[] pathParts = path.split("/");

            if (patternParts.length != pathParts.length) {
                return false;
            }

            for (int i = 0; i < patternParts.length; i++) {
                if (patternParts[i].startsWith("{") && patternParts[i].endsWith("}")) {
                    // Path variable, always matches
                    continue;
                }

                if (!patternParts[i].equals(pathParts[i])) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    // Inner class for controller mappings
    private record ControllerMapping(Object controller, Method method, String[] httpMethods) {

        public Object execute(HttpRequest request, HttpResponse response) throws Exception {
            int paramCount = method.getParameterCount();

            return switch (paramCount) {
                case 0 -> method.invoke(controller);
                case 1 -> method.invoke(controller, request); // nếu bạn có method nhận 1 param
                case 2 -> method.invoke(controller, request, response);
                default -> throw new IllegalArgumentException("Unsupported parameter count: " + paramCount);
            };
        }
    }
}
