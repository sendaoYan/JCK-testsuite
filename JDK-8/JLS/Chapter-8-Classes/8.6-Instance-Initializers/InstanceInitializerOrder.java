/*
 * @test
 * @summary JLS 8.6 (Java SE 8): an instance initializer runs after the superclass
 *          constructor returns and before the body of the class's own constructor
 *          executes.
 * @run main InstanceInitializerOrder
 */

public class InstanceInitializerOrder {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Super {
        int order;
        Super() { order = 1; }
    }

    static class Sub extends Super {
        { order = 2; }   // instance initializer
        Sub() {
            check(order == 2, "instance initializer ran after super()");
            order = 3;
        }
    }

    public static void main(String[] args) {
        Sub s = new Sub();
        check(s.order == 3, "constructor body ran after instance initializer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
