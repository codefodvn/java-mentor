# How to Java work

## 1. Hướng dẫn Biên dịch và Chạy chương trình Java

### Yêu cầu:
- Cài đặt **JDK** (Java Development Kit). Đảm bảo rằng bạn đã cài đặt Java và thiết lập `JAVA_HOME` trong biến môi trường.
- Cài đặt **Text Editor** hoặc **IDE** (chẳng hạn như VS Code, IntelliJ IDEA, Eclipse).

### Các bước:

#### 1. Viết mã nguồn Java
Tạo một file mã nguồn Java với tên `Main.java` (hoặc bất kỳ tên nào bạn muốn).

Ví dụ:

```java
package codefod.com;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

#### 2. Biên dịch mã nguồn Java
Mở **Command Prompt** hoặc **Terminal** và di chuyển đến thư mục chứa file mã nguồn Java.

Biên dịch mã nguồn Java bằng lệnh sau:

```
javac Main.java
```

Nếu không có lỗi nào, lệnh trên sẽ tạo ra một file `Main.class`.

#### 3. Chạy chương trình Java
Sử dụng lệnh sau để chạy chương trình Java:

Lưu ý: phải cd ra ngoài folder java. Vì trong file Main.java có package `codefod.com.compiler` do đó java sẽ load file Main.class trong thư mục `codefod/com/compiler` tính từ thư mục hiện tại.

```
cd ../../../
```

```
java (terminal sẽ đứng ở đây)
   |
   codefod
         |
         com
            |
            compiler
                  |
                  Main.java
                  Main.class
```

Sau đó chạy lệnh:

```
java codefod.com.Main
```

Kết quả sẽ
```
Hello, World!
```



