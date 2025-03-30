package codefod.com.livelock;

public class LivelockExample {
    static class Spoon {
        private Diner owner;

        public Spoon(Diner d) {
            owner = d;
        }

        public Diner getOwner() {
            return owner;
        }

        public synchronized void setOwner(Diner d) {
            owner = d;
        }

        public synchronized void use() {
            System.out.println(owner.name + " is eating!");
        }
    }

    static class Diner {
        private String name;
        private boolean isHungry;

        public Diner(String name) {
            this.name = name;
            isHungry = true;
        }

        public String getName() {
            return name;
        }

        public boolean isHungry() {
            return isHungry;
        }

        public void eatWith(Spoon spoon, Diner partner) {
            while (isHungry) {
                if (spoon.getOwner() != this) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {}
                    continue;
                }

                if (partner.isHungry()) {
                    System.out.println(name + ": You eat first my friend " + partner.getName());
                    spoon.setOwner(partner);  // nhường spoon
                    continue;
                }

                spoon.use();
                isHungry = false;
                System.out.println(name + ": I am done eating.");
            }
        }
    }

    public static void main(String[] args) {
        final Diner alice = new Diner("Alice");
        final Diner bob = new Diner("Bob");

        final Spoon spoon = new Spoon(alice);

        new Thread(() -> alice.eatWith(spoon, bob)).start();
        new Thread(() -> bob.eatWith(spoon, alice)).start();
    }
}
