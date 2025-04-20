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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Bộ điều phối yêu cầu (Request Dispatcher) chịu trách nhiệm định tuyến các yêu cầu đến
 * các servlet xử lý tương ứng và quản lý chuỗi bộ lọc (filter chain).
 * <p>
 * Lớp này hỗ trợ:
 * - Ánh xạ đường dẫn chính xác với servlet
 * - Ánh xạ mẫu đường dẫn (regex) với servlet
 * - Ánh xạ đường dẫn với tệp JSP
 * - Áp dụng các bộ lọc cho tất cả các yêu cầu
 */
public class RequestDispatcher {
    private static final Logger LOGGER = Logger.getLogger(RequestDispatcher.class.getName());

    // Các ánh xạ đường dẫn cụ thể với servlet
    private final Map<String, Servlet> servletMappings = new HashMap<>();

    // Các ánh xạ biểu thức chính quy với servlet (cho định tuyến linh hoạt)
    private final Map<Pattern, Servlet> regexMappings = new HashMap<>();

    // Các ánh xạ JSP (trong triển khai thực tế sẽ liên kết với JSP engine)
    private final Map<String, String> jspMappings = new HashMap<>();

    // Danh sách các bộ lọc áp dụng cho tất cả các yêu cầu
    private final List<ServletFilter> filters = new ArrayList<>();

    // Nội dung HTML cho các phản hồi lỗi phổ biến
    private static final String ERROR_500 = "<html><body><h1>500 Lỗi Máy Chủ Nội Bộ</h1><p>Đã xảy ra lỗi khi xử lý yêu cầu của bạn.</p></body></html>";
    private static final String ERROR_404 = "<html><body><h1>404 Không Tìm Thấy</h1><p>Tài nguyên bạn yêu cầu không tồn tại.</p></body></html>";
    private static final String ERROR_405 = "<html><body><h1>405 Phương Thức Không Được Phép</h1><p>Phương thức HTTP không được hỗ trợ cho tài nguyên này.</p></body></html>";

    /**
     * Đăng ký một servlet với đường dẫn cụ thể
     *
     * @param path    Đường dẫn chính xác để khớp với yêu cầu
     * @param servlet Servlet để xử lý yêu cầu
     */
    public void registerServlet(String path, Servlet servlet) {
        LOGGER.info("Đăng ký servlet cho đường dẫn: " + path);
        servletMappings.put(path, servlet);
    }

    /**
     * Đăng ký một servlet với mẫu biểu thức chính quy
     *
     * @param pathPattern Mẫu biểu thức chính quy để khớp với đường dẫn
     * @param servlet     Servlet để xử lý yêu cầu khớp
     */
    public void registerServletWithRegex(String pathPattern, Servlet servlet) {
        LOGGER.info("Đăng ký servlet với regex: " + pathPattern);
        regexMappings.put(Pattern.compile(pathPattern), servlet);
    }

    /**
     * Đăng ký một tệp JSP với đường dẫn
     *
     * @param path    Đường dẫn URL để truy cập JSP
     * @param jspFile Đường dẫn tệp JSP trên hệ thống
     */
    public void registerJsp(String path, String jspFile) {
        LOGGER.info("Đăng ký JSP: " + path + " -> " + jspFile);
        jspMappings.put(path, jspFile);
    }

    /**
     * Thêm một bộ lọc servlet vào chuỗi bộ lọc
     *
     * @param filter Bộ lọc cần thêm
     */
    public void addFilter(ServletFilter filter) {
        LOGGER.info("Thêm bộ lọc: " + filter.getClass().getName());
        filters.add(filter);
    }

    /**
     * Điều phối yêu cầu HTTP đến servlet thích hợp thông qua chuỗi bộ lọc
     *
     * @param request  Yêu cầu HTTP cần xử lý
     * @param response Phản hồi HTTP để gửi lại
     * @throws IOException nếu có lỗi I/O trong quá trình xử lý
     */
    public void dispatch(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();
        LOGGER.fine("Đang điều phối yêu cầu cho đường dẫn: " + path);

        // Tìm servlet phù hợp cho yêu cầu
        Servlet servlet = findMatchingServlet(path);

        // Kiểm tra xem có JSP phù hợp không
        if (servlet == null && jspMappings.containsKey(path)) {
            handleJspRequest(path, response);
            return;
        }

        // Tạo một chuỗi bộ lọc
        FilterChain filterChain = createFilterChain(servlet, request, response);

        // Thực thi chuỗi bộ lọc với servlet
        if (servlet != null) {
            filterChain.doFilter(request, response, servlet);
        } else {
            // Không tìm thấy servlet hoặc JSP phù hợp
            sendNotFoundResponse(response);
        }
    }

    /**
     * Tìm servlet phù hợp với đường dẫn yêu cầu
     *
     * @param path Đường dẫn URL để tìm kiếm
     * @return Servlet phù hợp hoặc null nếu không tìm thấy
     */
    private Servlet findMatchingServlet(String path) {
        // Đầu tiên kiểm tra các ánh xạ đường dẫn chính xác
        Servlet servlet = servletMappings.get(path);

        // Nếu không có kết quả trực tiếp, thử các mẫu regex
        if (servlet == null) {
            servlet = findServletByRegex(path);
        }

        return servlet;
    }

    /**
     * Tìm servlet dựa trên mẫu biểu thức chính quy
     *
     * @param path Đường dẫn URL để khớp với các mẫu
     * @return Servlet phù hợp hoặc null nếu không tìm thấy
     */
    private Servlet findServletByRegex(String path) {
        for (Map.Entry<Pattern, Servlet> entry : regexMappings.entrySet()) {
            if (entry.getKey().matcher(path).matches()) {
                LOGGER.fine("Tìm thấy khớp regex cho đường dẫn: " + path);
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Xử lý yêu cầu JSP
     *
     * @param path     Đường dẫn của yêu cầu JSP
     * @param response Phản hồi HTTP để gửi
     * @throws IOException nếu có lỗi khi ghi phản hồi
     */
    private void handleJspRequest(String path, HttpResponse response) throws IOException {
        // Trong triển khai thực tế, đây sẽ là nơi xử lý và render JSP
        String jspFile = jspMappings.get(path);
        LOGGER.fine("Xử lý JSP: " + jspFile);
        response.write("<html><body>JSP: " + jspFile + " sẽ được render tại đây</body></html>");
    }

    /**
     * Tạo chuỗi bộ lọc với servlet đích
     *
     * @param servlet Servlet đích cho chuỗi bộ lọc
     * @return FilterChain đã cấu hình
     */
    private FilterChain createFilterChain(Servlet servlet, HttpRequest request, HttpResponse response) {
        return new FilterChain(filters, targetServlet -> {
            try {
                processServletRequest(targetServlet, request, response);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi xử lý yêu cầu servlet", e);
                try {
                    sendErrorResponse(response, 500, ERROR_500);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Xử lý yêu cầu bằng servlet dựa trên phương thức HTTP
     *
     * @param servlet  Servlet để xử lý yêu cầu
     * @param request  Yêu cầu HTTP
     * @param response Phản hồi HTTP
     * @throws Exception nếu có lỗi trong quá trình xử lý
     */
    private void processServletRequest(Servlet servlet, HttpRequest request, HttpResponse response) throws Exception {
        // Xử lý dựa trên phương thức HTTP
        String method = request.getMethod();
        LOGGER.fine("Đang xử lý yêu cầu " + method + " cho servlet: " + servlet.getClass().getName());

        switch (method) {
            case "GET" -> servlet.doGet(request, response);
            case "POST" -> servlet.doPost(request, response);
            case "PUT" -> servlet.doPut(request, response);
            case "DELETE" -> servlet.doDelete(request, response);
            case null, default -> sendMethodNotAllowedResponse(response);
        }
    }

    /**
     * Gửi phản hồi lỗi 404 Not Found
     *
     * @param response Phản hồi HTTP để gửi
     * @throws IOException nếu có lỗi khi ghi phản hồi
     */
    private void sendNotFoundResponse(HttpResponse response) throws IOException {
        sendErrorResponse(response, 404, ERROR_404);
    }

    /**
     * Gửi phản hồi lỗi 405 Method Not Allowed
     *
     * @param response Phản hồi HTTP để gửi
     * @throws IOException nếu có lỗi khi ghi phản hồi
     */
    private void sendMethodNotAllowedResponse(HttpResponse response) throws IOException {
        sendErrorResponse(response, 405, ERROR_405);
    }

    /**
     * Gửi phản hồi lỗi với mã trạng thái và nội dung cụ thể
     *
     * @param response   Phản hồi HTTP để gửi
     * @param statusCode Mã trạng thái HTTP
     * @param content    Nội dung phản hồi
     * @throws IOException nếu có lỗi khi ghi phản hồi
     */
    private void sendErrorResponse(HttpResponse response, int statusCode, String content) throws IOException {
        LOGGER.fine("Gửi phản hồi lỗi: " + statusCode);
        response.setStatusCode(statusCode);
        response.write(content);
    }
}