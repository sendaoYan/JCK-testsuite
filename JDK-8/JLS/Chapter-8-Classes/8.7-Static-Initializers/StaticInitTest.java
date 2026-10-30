/*
 * @test
 * @summary JLS 8.7 (Java SE 8): static initializers run once when the class is
 *          initialized, in textual order.
 * @run main StaticInitTest
 */

public class StaticInitTest {
    static int x;
    static int order = 0;
    static int log;

    static {
        x = 1;
        order = 1;
    }
    static {
        log = order;   // runs after the first initializer
    }

    public static void main(String[] args) {
        if (x != 1) throw new AssertionError("static init");
        if (order != 1 || log != 1) throw new AssertionError("static init order");
        System.out.println("PASS");
    }
}
