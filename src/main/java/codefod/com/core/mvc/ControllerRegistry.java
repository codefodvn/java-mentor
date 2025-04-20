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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Lớp đăng ký và quản lý các controller trong hệ thống MVC
 * Lớp này chịu trách nhiệm ánh xạ các URL đến các phương thức controller tương ứng
 */
public class ControllerRegistry {
    // Logger để ghi nhật ký
    private static final Logger LOGGER = Logger.getLogger(ControllerRegistry.class.getName());

    // Map lưu trữ ánh xạ giữa URL pattern và thông tin controller
    private final Map<String, ControllerMapping> mappings = new HashMap<>();

    // Engine xử lý JSP
    private final JspEngine jspEngine;

    // Tiền tố của ứng dụng web (mặc định là "/app")
    private final String appPrefix;

    /**
     * Khởi tạo ControllerRegistry với engine JSP
     *
     * @param jspEngine Engine xử lý JSP
     */
    public ControllerRegistry(JspEngine jspEngine) {
        this(jspEngine, "/app");
    }

    /**
     * Khởi tạo ControllerRegistry với engine JSP và tiền tố ứng dụng
     *
     * @param jspEngine Engine xử lý JSP
     * @param appPrefix Tiền tố của ứng dụng web
     */
    public ControllerRegistry(JspEngine jspEngine, String appPrefix) {
        this.jspEngine = jspEngine;
        this.appPrefix = appPrefix != null ? appPrefix : "";
        LOGGER.info("Khởi tạo ControllerRegistry với tiền tố ứng dụng: " + this.appPrefix);
    }

    /**
     * Đăng ký một controller vào hệ thống
     *
     * @param controller Đối tượng controller cần đăng ký
     * @throws IllegalArgumentException Nếu controller không hợp lệ
     */
    public void registerController(Object controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller không được là null");
        }

        Class<?> controllerClass = controller.getClass();

        // Kiểm tra xem lớp có được đánh dấu @Controller không
        if (!controllerClass.isAnnotationPresent(Controller.class)) {
            LOGGER.warning("Lớp " + controllerClass.getName() + " không có annotation @Controller");
            return;
        }

        // Xác định URL cơ sở từ annotation Controller và RequestMapping cấp lớp
        String baseUrl = extractBaseUrl(controllerClass);
        LOGGER.info("Đăng ký controller: " + controllerClass.getName() + " với baseUrl: " + baseUrl);

        // Xử lý từng phương thức trong controller
        for (Method method : controllerClass.getDeclaredMethods()) {
            registerControllerMethod(controller, method, baseUrl);
        }
    }

    /**
     * Trích xuất URL cơ sở từ annotation trên lớp controller
     *
     * @param controllerClass Lớp controller
     * @return URL cơ sở
     */
    private String extractBaseUrl(Class<?> controllerClass) {
        String baseUrl = "";

        // Xử lý annotation @Controller
        Controller controllerAnnotation = controllerClass.getAnnotation(Controller.class);
        if (controllerAnnotation != null && !controllerAnnotation.value().isEmpty()) {
            baseUrl = controllerAnnotation.value();
        }

        // Xử lý annotation @RequestMapping ở cấp lớp (ưu tiên cao hơn @Controller)
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
            String mappingValue = !requestMapping.value().isEmpty() ? requestMapping.value() : requestMapping.path();
            if (!mappingValue.isEmpty()) {
                baseUrl = mappingValue;
            }
        }

        // Đảm bảo URL bắt đầu bằng '/'
        if (!baseUrl.isEmpty() && !baseUrl.startsWith("/")) {
            baseUrl = "/" + baseUrl;
        }

        return baseUrl;
    }

    /**
     * Đăng ký một phương thức controller
     *
     * @param controller Đối tượng controller
     * @param method Phương thức cần đăng ký
     * @param baseUrl URL cơ sở của controller
     */
    private void registerControllerMethod(Object controller, Method method, String baseUrl) {
        String path = null;
        String[] httpMethods = null;

        // Kiểm tra các loại annotation mapping khác nhau
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

        // Nếu tìm thấy annotation mapping
        if (path != null) {
            // Tạo đường dẫn đầy đủ bằng cách kết hợp baseUrl và path
            String fullPath = combineUrlPaths(baseUrl, path);

            // Đăng ký mapping
            ControllerMapping mapping = new ControllerMapping(controller, method, httpMethods);
            mappings.put(fullPath, mapping);

            LOGGER.info("Đăng ký mapping: " + fullPath + " -> " + controller.getClass().getSimpleName() + "." + method.getName()
                    + " [" + String.join(", ", httpMethods) + "]");
        }
    }

    /**
     * Kết hợp hai phần của URL thành một đường dẫn hoàn chỉnh
     *
     * @param basePath Đường dẫn cơ sở
     * @param path Đường dẫn cần thêm vào
     * @return Đường dẫn hoàn chỉnh
     */
    private String combineUrlPaths(String basePath, String path) {
        if (path.isEmpty()) {
            return basePath;
        }

        if (path.startsWith("/") || basePath.endsWith("/")) {
            return basePath + path;
        } else {
            return basePath + "/" + path;
        }
    }

    /**
     * Xử lý yêu cầu HTTP bằng cách định tuyến đến controller phù hợp
     *
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @return true nếu yêu cầu được xử lý, false nếu không tìm thấy controller phù hợp
     * @throws Exception Nếu có lỗi xảy ra trong quá trình xử lý
     */
    public boolean handleRequest(HttpRequest request, HttpResponse response) throws Exception {
        String requestPath = request.getPath();

        // Loại bỏ tiền tố ứng dụng khỏi đường dẫn yêu cầu
        String normalizedPath = requestPath;
        if (!appPrefix.isEmpty() && normalizedPath.startsWith(appPrefix)) {
            normalizedPath = normalizedPath.substring(appPrefix.length());
        }

        // Đảm bảo đường dẫn bắt đầu bằng '/'
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }

        LOGGER.fine("Xử lý yêu cầu: " + requestPath + " -> " + normalizedPath);

        // Tìm mapping trùng khớp chính xác
        ControllerMapping mapping = mappings.get(normalizedPath);

        // Nếu không tìm thấy, thử tìm mapping với biến đường dẫn
        if (mapping == null) {
            for (Map.Entry<String, ControllerMapping> entry : mappings.entrySet()) {
                if (pathMatches(entry.getKey(), normalizedPath)) {
                    mapping = entry.getValue();
                    // Trích xuất biến đường dẫn và thêm vào request attributes
                    extractPathVariables(entry.getKey(), normalizedPath, request);
                    break;
                }
            }
        }

        // Nếu tìm thấy mapping
        if (mapping != null) {
            // Kiểm tra phương thức HTTP có được phép không
            boolean methodAllowed = isMethodAllowed(request.getMethod(), mapping.httpMethods());

            if (!methodAllowed) {
                LOGGER.warning("Phương thức không được phép: " + request.getMethod() + " cho đường dẫn: " + requestPath);
                response.setStatusCode(405); // Method Not Allowed
                response.setHeader("Allow", String.join(", ", mapping.httpMethods()));
                response.write("<html><body><h1>405 Method Not Allowed</h1></body></html>");
                return true;
            }

            try {
                // Thực thi phương thức controller
                Object result = mapping.execute(request, response);

                // Xử lý kết quả trả về
                handleControllerResult(result, request, response);

                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi thực thi controller: " + e.getMessage(), e);
                throw e;
            }
        }

        // Không tìm thấy controller phù hợp
        return false;
    }

    /**
     * Kiểm tra xem phương thức HTTP có được phép không
     *
     * @param requestMethod Phương thức của yêu cầu
     * @param allowedMethods Danh sách phương thức được phép
     * @return true nếu phương thức được phép, false nếu không
     */
    private boolean isMethodAllowed(String requestMethod, String[] allowedMethods) {
        for (String allowedMethod : allowedMethods) {
            if (allowedMethod.equalsIgnoreCase(requestMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Xử lý kết quả trả về từ controller
     *
     * @param result Kết quả từ controller
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @throws Exception Nếu có lỗi xảy ra trong quá trình xử lý
     */
    private void handleControllerResult(Object result, HttpRequest request, HttpResponse response) throws Exception {
        if (result == null) {
            // Không cần xử lý gì thêm, controller đã xử lý trực tiếp response
            return;
        }

        if (result instanceof String viewName) {
            // Xử lý tên view
            processView(viewName, request, response, null);
        } else if (result instanceof ModelAndView modelAndView) {
            // Xử lý ModelAndView
            String viewName = modelAndView.getViewName();
            processView(viewName, request, response, modelAndView.getModel());
        } else {
            // Kết quả không được hỗ trợ
            LOGGER.warning("Loại kết quả không được hỗ trợ: " + result.getClass().getName());
            response.setStatusCode(500);
            response.write("<html><body><h1>500 Internal Server Error</h1><p>Loại kết quả không được hỗ trợ</p></body></html>");
        }
    }

    /**
     * Xử lý view bằng JSP engine
     *
     * @param viewName Tên view
     * @param request Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @param model Model dữ liệu (có thể null)
     * @throws Exception Nếu có lỗi xảy ra trong quá trình xử lý
     */
    private void processView(String viewName, HttpRequest request, HttpResponse response, Map<String, Object> model) throws Exception {
        if (jspEngine == null) {
            LOGGER.warning("JSP Engine không được cấu hình");
            response.setStatusCode(500);
            response.write("<html><body><h1>500 Internal Server Error</h1><p>JSP Engine không được cấu hình</p></body></html>");
            return;
        }

        // Chuyển đổi tên view logic thành đường dẫn JSP
        String jspPath = viewName.endsWith(".jsp") ? viewName : viewName + ".jsp";

        LOGGER.fine("Xử lý JSP: " + jspPath);

        // Gọi JSP engine để xử lý view
        jspEngine.processJsp(jspPath, request, response, model);
    }

    /**
     * Kiểm tra xem đường dẫn có khớp với mẫu không, hỗ trợ biến đường dẫn
     *
     * @param pattern Mẫu URL (ví dụ: /users/{id})
     * @param path Đường dẫn thực tế (ví dụ: /users/123)
     * @return true nếu khớp, false nếu không
     */
    private boolean pathMatches(String pattern, String path) {
        // Khớp chính xác
        if (pattern.equals(path)) {
            return true;
        }

        // Kiểm tra biến đường dẫn
        if (pattern.contains("{") && pattern.contains("}")) {
            String[] patternParts = pattern.split("/");
            String[] pathParts = path.split("/");

            // Số phần phải giống nhau
            if (patternParts.length != pathParts.length) {
                return false;
            }

            // So sánh từng phần
            for (int i = 0; i < patternParts.length; i++) {
                String patternPart = patternParts[i];

                // Nếu là biến đường dẫn (ví dụ: {id})
                if (isPathVariable(patternPart)) {
                    // Luôn khớp
                    continue;
                }

                // Nếu không phải biến đường dẫn, phần phải giống nhau
                if (!patternPart.equals(pathParts[i])) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Kiểm tra xem một phần của đường dẫn có phải là biến không
     *
     * @param part Phần của đường dẫn
     * @return true nếu là biến, false nếu không
     */
    private boolean isPathVariable(String part) {
        return part.startsWith("{") && part.endsWith("}");
    }

    /**
     * Trích xuất biến đường dẫn và thêm vào thuộc tính của request
     *
     * @param pattern Mẫu URL (ví dụ: /users/{id}/posts/{postId})
     * @param path Đường dẫn thực tế (ví dụ: /users/123/posts/456)
     * @param request Yêu cầu HTTP để lưu biến
     */
    private void extractPathVariables(String pattern, String path, HttpRequest request) {
        String[] patternParts = pattern.split("/");
        String[] pathParts = path.split("/");

        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];

            if (isPathVariable(patternPart)) {
                // Trích xuất tên biến (loại bỏ { và })
                String variableName = patternPart.substring(1, patternPart.length() - 1);
                // Lấy giá trị từ đường dẫn thực tế
                String variableValue = pathParts[i];

                // Thêm vào thuộc tính của request
                request.setAttribute(variableName, variableValue);
                LOGGER.fine("Trích xuất biến đường dẫn: " + variableName + " = " + variableValue);
            }
        }
    }

    /**
     * Lớp nội bộ để lưu trữ thông tin về một mapping controller
     */
    private record ControllerMapping(Object controller, Method method, String[] httpMethods) {

        /**
         * Thực thi phương thức controller
         *
         * @param request Yêu cầu HTTP
         * @param response Phản hồi HTTP
         * @return Kết quả trả về từ phương thức controller
         * @throws Exception Nếu có lỗi xảy ra trong quá trình thực thi
         */
        public Object execute(HttpRequest request, HttpResponse response) throws Exception {
            int paramCount = method.getParameterCount();

            // Gọi phương thức với số lượng tham số phù hợp
            return switch (paramCount) {
                case 0 -> method.invoke(controller);
                case 1 -> method.invoke(controller, request);
                case 2 -> method.invoke(controller, request, response);
                default -> throw new IllegalArgumentException("Số lượng tham số không được hỗ trợ: " + paramCount);
            };
        }
    }

    /**
     * Lấy tất cả các mapping đã đăng ký (cho mục đích debug)
     *
     * @return Map chứa tất cả các mapping
     */
    public Map<String, ControllerMapping> getMappings() {
        return new HashMap<>(mappings);
    }
}