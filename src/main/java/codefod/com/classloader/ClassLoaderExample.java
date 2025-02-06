package codefod.com.classloader;

import com.sun.net.httpserver.spi.HttpServerProvider;

public class ClassLoaderExample {

    public static void main(String[] args) {
        // Lấy class loader của lớp hiện tại
        ClassLoader classLoader = ClassLoaderExample.class.getClassLoader();

        // hiển thị ra jdk.internal.loader.ClassLoaders$AppClassLoader@5a07e868 (AppClassLoader đây là Application ClassLoader) vì đây là class do chúng ta viết
        System.out.println("ClassLoader of ClassLoaderExample: " + classLoader);

        // Lấy class loader của lớp String (nó sẽ sử dụng Bootstrap ClassLoader)
        ClassLoader stringClassLoader = String.class.getClassLoader();
        // Sẽ in ra null vì String được nạp bởi Bootstrap ClassLoader,
        // Bootstrap ClassLoader không phải là một Java object nên không thể truy cập trực tiếp từ Java code nên sẽ in ra null
        // Bootstrap ClassLoader được viết bằng ngôn ngữ lập trình C++ và được tích hợp sẵn trong JVM
        // Hiểu đơn giản là  vì Bootstrap ClassLoader là một phần của JVM được cài sẵn và không phải là một ClassLoader Java thuần túy
        // (không phải là đối tượng mà bạn có thể trực tiếp thao tác với nó),
        System.out.println("ClassLoader of String: " + stringClassLoader);

        // External libraries được nạp bởi Application ClassLoader
        // Ví dụ: PreparedStatement trong JDK nằm ở External libraries nên sẽ được nạp bởi Platform ClassLoader (thay thế extension class loader) evidence: https://stackoverflow.com/questions/46494112/classloaders-hierarchy-in-java-9
        // HttpServerProvider nằm ở JDK
        System.out.println(
                "class loader for PreparedStatement: " + HttpServerProvider.class.getClassLoader());

        // Dynamic tải một class bất kỳ bằng cách sử dụng ClassLoader
        try {
            Class<?> clazz = classLoader.loadClass("java.util.ArrayList");
            System.out.println("Class loaded: " + clazz.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
