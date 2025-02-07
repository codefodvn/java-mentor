package codefod.com.not_di;

public class Main {

    static class Engine {

        void start() {
            System.out.println("Engine started...");
        }
    }

    static class Car {

        private Engine engine = new Engine(); // Tạo đối tượng Engine bên trong Car

        void drive() {
            engine.start();
            System.out.println("Car is moving...");
        }
    }

    public static void main(String[] args) {
        Car car = new Car();
        car.drive();
    }
}
