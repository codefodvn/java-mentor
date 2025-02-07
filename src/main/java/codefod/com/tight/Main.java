package codefod.com.tight;

public class Main {

    static class Engine {

        void start() {
            System.out.println("Engine started...");
        }
    }

    static class Car {

        private Engine engine = new Engine(); // Car TIGHTLY COUPLED với Engine

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
