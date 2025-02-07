package codefod.com.di;

public class Main {

    static class Engine {

        void start() {
            System.out.println("Engine started...");
        }
    }

    static class Car {

        private Engine engine;

        // Inject dependency qua constructor
        public Car(Engine engine) {
            this.engine = engine;
        }

        void drive() {
            engine.start();
            System.out.println("Car is moving...");
        }
    }

    // Sử dụng
    public static void main(String[] args) {
        Engine engine = new Engine(); // Tạo đối tượng Engine riêng
        Car car = new Car(engine); // Inject Engine vào Car
        car.drive();
    }
}
