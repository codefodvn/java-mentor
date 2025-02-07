package codefod.com.dependency_inversion.apply;

public class Main {

    static interface Database {

        void connect();
    }

    static class MySQLDatabase implements Database {

        public void connect() {
            System.out.println("Kết nối MySQL Database");
        }
    }

    static class PostgreSQLDatabase implements Database {

        public void connect() {
            System.out.println("Kết nối PostgreSQL Database");
        }
    }

    static class DataService {

        private Database database;

        public DataService(Database database) { // Inject abstraction
            this.database = database;
        }

        public void getData() {
            database.connect();
            System.out.println("Lấy dữ liệu từ database");
        }
    }

    public static void main(String[] args) {
        Database mySQL = new MySQLDatabase();
        DataService service = new DataService(mySQL);
        service.getData();

        Database postgreSQL = new PostgreSQLDatabase();
        DataService service2 = new DataService(postgreSQL);
        service2.getData();
    }
}
