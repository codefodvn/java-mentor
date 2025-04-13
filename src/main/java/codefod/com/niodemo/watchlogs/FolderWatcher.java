package codefod.com.niodemo.watchlogs;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class FolderWatcher {

    public static void main(String[] args) {
        try {
            // 🔹 Khởi tạo WatchService
            WatchService watcher = FileSystems.getDefault().newWatchService();

            // 🔹 Đăng ký thư mục logs/ để theo dõi các sự kiện CREATE và MODIFY
            Path dir = Paths.get("logs");
            dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);

            System.out.println("👀 Đang giám sát thư mục: " + dir.toAbsolutePath());

            // 🔁 Vòng lặp để theo dõi sự kiện
            while (true) {
                WatchKey key;
                try {
                    // ⏳ Block cho đến khi có sự kiện
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    System.out.println("⛔ Bị gián đoạn");
                    return;
                }

                // 🔍 Duyệt qua các sự kiện xảy ra
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // 🔸 Bỏ qua nếu là sự kiện OVERFLOW
                    if (kind == OVERFLOW) continue;

                    // 🔸 Lấy tên file từ context
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    // 🔔 In ra thông tin sự kiện
                    System.out.printf("📢 %s: %s%n", kind.name(), filename);
                }

                // 🔁 Reset key để tiếp tục theo dõi
                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("⛔ WatchKey không còn hợp lệ");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi khởi tạo WatchService: " + e.getMessage());
        }
    }
}
