package codefod.com.loose;

public class Main {

    static interface Engine {

        void start();
    }

    static class PetrolEngine implements Engine {

        public void start() {
            System.out.println("Petrol Engine started...");
        }
    }

    static class ElectricEngine implements Engine {

        public void start() {
            System.out.println("Electric Engine started...");
        }
    }

    static class Car {

        private Engine engine;

        // Inject dependency thông qua constructor
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
        Engine petrolEngine = new PetrolEngine();
        Car car1 = new Car(petrolEngine);
        car1.drive();

        Engine electricEngine = new ElectricEngine();
        Car car2 = new Car(electricEngine);
        car2.drive();
    }
}
