package codefod.com.dependency_inversion.not_apply;

public class Main {

    static class MySQLDatabase {

        public void connect() {
            System.out.println("Kết nối MySQL Database");
        }
    }

    static class DataService {

        private MySQLDatabase database = new MySQLDatabase();

        public void getData() {
            database.connect();
            System.out.println("Lấy dữ liệu từ MySQL");
        }
    }

    public static void main(String[] args) {
        DataService dataService = new DataService();
        dataService.getData();
    }
}
