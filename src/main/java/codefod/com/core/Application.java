package codefod.com.core;

import codefod.com.core.annotation.AnnotationScanner;
import codefod.com.core.config.XmlConfigLoader;
import codefod.com.core.dispatcher.RequestDispatcher;
import codefod.com.core.jsp.JspEngine;
import codefod.com.core.mvc.ControllerRegistry;
import codefod.com.core.mvc.DispatcherServlet;
import codefod.com.core.server.NioHttpServer;
import codefod.com.core.websocket.WebSocketManager;
import codefod.com.socket.ChatWebSocketEndpoint;

import java.io.File;

public class Application {
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_THREAD_POOL_SIZE = 50;

    private final int port;
    private final String webappDir;
    private final int threadPoolSize;
    private NioHttpServer server;
    private final RequestDispatcher dispatcher;
    private final WebSocketManager webSocketManager;
    private final JspEngine jspEngine;
    private final ControllerRegistry controllerRegistry;

    public Application() {
        this(DEFAULT_PORT, "webapp", DEFAULT_THREAD_POOL_SIZE);
    }

    public Application(int port, String webappDir, int threadPoolSize) {
        this.port = port;
        this.webappDir = webappDir;
        this.threadPoolSize = threadPoolSize;
        this.dispatcher = new RequestDispatcher();
        this.webSocketManager = new WebSocketManager();
        this.jspEngine = new JspEngine(webappDir + "/WEB-INF/views");
        this.controllerRegistry = new ControllerRegistry(jspEngine);
    }

    public void start() throws Exception {
        // Create directory structure if it doesn't exist
        createDirectoryStructure();

        // Load XML configuration
        File webXml = new File(webappDir + "/WEB-INF/web.xml");
        if (webXml.exists()) {
            XmlConfigLoader configLoader = new XmlConfigLoader(dispatcher);
            configLoader.loadConfig(webXml.getPath());
        }

        // Scan for annotations
        AnnotationScanner scanner = new AnnotationScanner(dispatcher, controllerRegistry);
        scanner.scanPackage("codefod.com"); // Adjust package name as needed

        // Register dispatcher servlet for controllers
        DispatcherServlet dispatcherServlet = new DispatcherServlet(controllerRegistry);
        dispatcher.registerServletWithRegex("/app/.*", dispatcherServlet);

        webSocketManager.registerEndpoint("/ws/chat", new ChatWebSocketEndpoint());

        // Start the server
        server = new NioHttpServer(port, threadPoolSize, dispatcher, webSocketManager);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        System.out.println("Starting server on port " + port);
        server.start();
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirectoryStructure() {
        // Create base webapp directory
        File webapp = new File(webappDir);
        if (!webapp.exists()) {
            webapp.mkdirs();
        }

        // Create WEB-INF directory
        File webInf = new File(webappDir + "/WEB-INF");
        if (!webInf.exists()) {
            webInf.mkdirs();
        }

        // Create views directory for JSPs
        File views = new File(webappDir + "/WEB-INF/views");
        if (!views.exists()) {
            views.mkdirs();
        }

        // Create classes directory
        File classes = new File(webappDir + "/WEB-INF/classes");
        if (!classes.exists()) {
            classes.mkdirs();
        }

        // Create lib directory
        File lib = new File(webappDir + "/WEB-INF/lib");
        if (!lib.exists()) {
            lib.mkdirs();
        }
    }

    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    public JspEngine getJspEngine() {
        return jspEngine;
    }

    public ControllerRegistry getControllerRegistry() {
        return controllerRegistry;
    }

    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
            Application app = new Application(port, "webapp", DEFAULT_THREAD_POOL_SIZE);
            app.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
