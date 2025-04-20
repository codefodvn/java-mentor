package codefod.com.core.jsp;

import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JspEngine {
    private final String viewsDirectory;
    private final Map<String, CompiledJsp> compiledJsps = new HashMap<>();

    public JspEngine(String viewsDirectory) {
        this.viewsDirectory = viewsDirectory;
    }

    public void processJsp(String jspPath, HttpRequest request, HttpResponse response, Map<String, Object> model) throws IOException {
        CompiledJsp jsp = compiledJsps.get(jspPath);

        // Check if JSP needs to be compiled
        File jspFile = new File(viewsDirectory, jspPath);
        if (jsp == null || jspFile.lastModified() > jsp.getCompileTime()) {
            jsp = compileJsp(jspFile);
            compiledJsps.put(jspPath, jsp);
        }

        // Execute the JSP
        String content = jsp.execute(request, response, model);
        response.write(content);
    }

    private CompiledJsp compileJsp(File jspFile) throws IOException {
        String jspContent = new String(Files.readAllBytes(jspFile.toPath()));

        // Process JSP directives, expressions, and scriptlets
        // This is a simplified implementation

        StringBuilder htmlOutput = new StringBuilder();
        StringBuilder javaCode = new StringBuilder();

        // Add header for the generated Java code
        javaCode.append("public String render(HttpRequest request, HttpResponse response, Map<String, Object> model) {\n");
        javaCode.append("    StringBuilder out = new StringBuilder();\n");

        // Process JSP content
        int currentPos = 0;

        // Process <%@ page ... %> directives
        Pattern pageDirectivePattern = Pattern.compile("<%@\\s+page\\s+([^%>]+)%>");
        Matcher pageDirectiveMatcher = pageDirectivePattern.matcher(jspContent);
        while (pageDirectiveMatcher.find()) {
            String directive = pageDirectiveMatcher.group(1).trim();
            // Parse directive attributes
            // For now, we'll just ignore them
        }

        // Process <%@ taglib ... %> directives
        Pattern taglibPattern = Pattern.compile("<%@\\s+taglib\\s+([^%>]+)%>");
        Matcher taglibMatcher = taglibPattern.matcher(jspContent);
        while (taglibMatcher.find()) {
            String taglib = taglibMatcher.group(1).trim();
            // Process taglib directive
            // For now, we'll just ignore them
        }

        // Process <%= expressions %> and <% scriptlets %>
        Pattern scriptPattern = Pattern.compile("<%=(.+?)%>|<%([^=].*?)%>|<%--(.+?)--%>");
        Matcher scriptMatcher = scriptPattern.matcher(jspContent);

        while (scriptMatcher.find()) {
            // Add HTML content before this script
            String htmlBefore = jspContent.substring(currentPos, scriptMatcher.start());
            htmlOutput.append(htmlBefore);
            javaCode.append("    out.append(\"").append(escapeJava(htmlBefore)).append("\");\n");

            if (scriptMatcher.group(1) != null) {
                // <%= expression %>
                String expression = scriptMatcher.group(1).trim();
                javaCode.append("    out.append(String.valueOf(").append(expression).append("));\n");
            } else if (scriptMatcher.group(2) != null) {
                // <% scriptlet %>
                String scriptlet = scriptMatcher.group(2);
                javaCode.append("    ").append(scriptlet).append("\n");
            }
            // Ignore JSP comments <%-- --%>

            currentPos = scriptMatcher.end();
        }

        // Add remaining HTML content
        String remainingHtml = jspContent.substring(currentPos);
        htmlOutput.append(remainingHtml);
        javaCode.append("    out.append(\"").append(escapeJava(remainingHtml)).append("\");\n");

        // Close the Java method
        javaCode.append("    return out.toString();\n");
        javaCode.append("}\n");

        return new CompiledJsp(javaCode.toString());
    }

    private String escapeJava(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Inner class representing a compiled JSP
    private static class CompiledJsp {
        private final String javaCode;
        private final long compileTime;

        public CompiledJsp(String javaCode) {
            this.javaCode = javaCode;
            this.compileTime = System.currentTimeMillis();
        }

        public String execute(HttpRequest request, HttpResponse response, Map<String, Object> model) {
            // In a real implementation, this would compile and execute the Java code
            // Here we'll just simulate the output

            // For demonstration, we'll just return a simple template with request info
            StringBuilder output = new StringBuilder();
            output.append("<html><body>");
            output.append("<h1>JSP Simulation</h1>");
            output.append("<p>Path: ").append(request.getPath()).append("</p>");

            if (model != null) {
                output.append("<h2>Model Data:</h2>");
                output.append("<ul>");
                for (Map.Entry<String, Object> entry : model.entrySet()) {
                    output.append("<li>").append(entry.getKey()).append(": ").append(entry.getValue()).append("</li>");
                }
                output.append("</ul>");
            }

            output.append("</body></html>");
            return output.toString();
        }

        public long getCompileTime() {
            return compileTime;
        }

        public String getJavaCode() {
            return javaCode;
        }
    }
}
