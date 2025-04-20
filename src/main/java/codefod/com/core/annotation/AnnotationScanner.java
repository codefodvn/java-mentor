package codefod.com.core.annotation;

import codefod.com.core.dispatcher.RequestDispatcher;
import codefod.com.core.dispatcher.Servlet;
import codefod.com.core.filter.ServletFilter;
import codefod.com.core.mvc.ControllerRegistry;
import codefod.com.core.mvc.annotation.Controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class AnnotationScanner {
    private final RequestDispatcher dispatcher;
    private final ControllerRegistry controllerRegistry;

    public AnnotationScanner(RequestDispatcher dispatcher, ControllerRegistry controllerRegistry) {
        this.dispatcher = dispatcher;
        this.controllerRegistry = controllerRegistry;
    }

    public void scanPackage(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        for (File directory : dirs) {
            findClasses(directory, packageName);
        }
    }

    private void findClasses(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                Class<?> cls = Class.forName(className);

                // Process WebServlet annotation
                if (cls.isAnnotationPresent(WebServlet.class)) {
                    WebServlet annotation = cls.getAnnotation(WebServlet.class);
                    try {
                        Servlet servlet = (Servlet) cls.getDeclaredConstructor().newInstance();

                        String[] patterns = annotation.urlPatterns().length > 0 ?
                                annotation.urlPatterns() : annotation.value();

                        for (String pattern : patterns) {
                            if (pattern.contains("*")) {
                                // Convert to regex pattern
                                String regex = pattern.replace("*", ".*");
                                dispatcher.registerServletWithRegex(regex, servlet);
                            } else {
                                dispatcher.registerServlet(pattern, servlet);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Process WebFilter annotation
                if (cls.isAnnotationPresent(WebFilter.class)) {
                    try {
                        ServletFilter filter = (ServletFilter) cls.getDeclaredConstructor().newInstance();
                        dispatcher.addFilter(filter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (cls.isAnnotationPresent(Controller.class)) {
                    Object controller = null;
                    try {
                        controller = cls.getDeclaredConstructor().newInstance();
                        controllerRegistry.registerController(controller);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
