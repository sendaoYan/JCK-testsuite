/*
 * @test
 * @summary JLS 15.9.5 (Java SE 8): an anonymous class is an expression that
 *          instantiates a subclass and can capture effectively final locals.
 * @run main AnonymousClassTest
 */

public class AnonymousClassTest {
    interface G { int get(); }

    public static void main(String[] args) {
        final int x = 10;
        G g = new G() {
            public int get() { return x; }
        };
        if (g.get() != 10) throw new AssertionError("anonymous class captures local");
        if (!(g instanceof G)) throw new AssertionError("anonymous class implements interface");
        System.out.println("PASS");
    }
}
