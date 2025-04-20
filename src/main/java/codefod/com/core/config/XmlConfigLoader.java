package codefod.com.core.config;

import codefod.com.core.dispatcher.RequestDispatcher;
import codefod.com.core.dispatcher.Servlet;
import codefod.com.core.filter.ServletFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlConfigLoader {
    private final RequestDispatcher dispatcher;

    public XmlConfigLoader(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void loadConfig(String configFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(configFile));
        document.getDocumentElement().normalize();

        // Parse web-app root element
        Element rootElement = document.getDocumentElement();
        if (!"web-app".equals(rootElement.getNodeName())) {
            throw new IllegalArgumentException("Root element must be 'web-app'");
        }

        // Parse servlets
        NodeList servletNodes = rootElement.getElementsByTagName("servlet");
        Map<String, Servlet> servlets = new HashMap<>();

        for (int i = 0; i < servletNodes.getLength(); i++) {
            Node servletNode = servletNodes.item(i);
            if (servletNode.getNodeType() == Node.ELEMENT_NODE) {
                Element servletElement = (Element) servletNode;

                String servletName = getTextContent(servletElement, "servlet-name");
                String servletClass = getTextContent(servletElement, "servlet-class");

                // Instantiate servlet
                Class<?> cls = Class.forName(servletClass);
                Servlet servlet = (Servlet) cls.newInstance();
                servlets.put(servletName, servlet);

                // Initialize servlet
                servlet.init();
            }
        }

        // Parse servlet mappings
        NodeList servletMappingNodes = rootElement.getElementsByTagName("servlet-mapping");
        for (int i = 0; i < servletMappingNodes.getLength(); i++) {
            Node mappingNode = servletMappingNodes.item(i);
            if (mappingNode.getNodeType() == Node.ELEMENT_NODE) {
                Element mappingElement = (Element) mappingNode;

                String servletName = getTextContent(mappingElement, "servlet-name");
                String urlPattern = getTextContent(mappingElement, "url-pattern");

                Servlet servlet = servlets.get(servletName);
                if (servlet != null) {
                    if (urlPattern.contains("*")) {
                        // Convert to regex pattern
                        String regex = urlPattern.replace("*", ".*");
                        dispatcher.registerServletWithRegex(regex, servlet);
                    } else {
                        dispatcher.registerServlet(urlPattern, servlet);
                    }
                }
            }
        }

        // Parse filters
        NodeList filterNodes = rootElement.getElementsByTagName("filter");
        Map<String, ServletFilter> filters = new HashMap<>();

        for (int i = 0; i < filterNodes.getLength(); i++) {
            Node filterNode = filterNodes.item(i);
            if (filterNode.getNodeType() == Node.ELEMENT_NODE) {
                Element filterElement = (Element) filterNode;

                String filterName = getTextContent(filterElement, "filter-name");
                String filterClass = getTextContent(filterElement, "filter-class");

                // Instantiate filter
                Class<?> cls = Class.forName(filterClass);
                ServletFilter filter = (ServletFilter) cls.newInstance();
                filters.put(filterName, filter);
            }
        }

        // Parse filter mappings (for order)
        List<String> filterOrder = new ArrayList<>();
        NodeList filterMappingNodes = rootElement.getElementsByTagName("filter-mapping");
        for (int i = 0; i < filterMappingNodes.getLength(); i++) {
            Node mappingNode = filterMappingNodes.item(i);
            if (mappingNode.getNodeType() == Node.ELEMENT_NODE) {
                Element mappingElement = (Element) mappingNode;

                String filterName = getTextContent(mappingElement, "filter-name");
                if (!filterOrder.contains(filterName)) {
                    filterOrder.add(filterName);
                }
            }
        }

        // Add filters in defined order
        for (String filterName : filterOrder) {
            ServletFilter filter = filters.get(filterName);
            if (filter != null) {
                dispatcher.addFilter(filter);
            }
        }

        // Parse welcome files
        NodeList welcomeFileListNodes = rootElement.getElementsByTagName("welcome-file-list");
        if (welcomeFileListNodes.getLength() > 0) {
            Element welcomeFileListElement = (Element) welcomeFileListNodes.item(0);
            NodeList welcomeFileNodes = welcomeFileListElement.getElementsByTagName("welcome-file");

            for (int i = 0; i < welcomeFileNodes.getLength(); i++) {
                Node welcomeFileNode = welcomeFileNodes.item(i);
                if (welcomeFileNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element welcomeFileElement = (Element) welcomeFileNode;
                    String welcomeFile = welcomeFileElement.getTextContent().trim();

                    // Register welcome file in dispatcher
                    // In a real implementation, we would handle this differently
                    if (welcomeFile.endsWith(".jsp")) {
                        dispatcher.registerJsp("/" + welcomeFile, welcomeFile);
                    }
                }
            }
        }
    }

    private String getTextContent(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
}
