/*
 * @test
 * @summary java.lang.ScopedValue.Carrier conformance: where/get/call/run bindings.
 * @run main CarrierTest
 */
import java.lang.ScopedValue;

public class CarrierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        ScopedValue<String> a = ScopedValue.newInstance();
        ScopedValue<Integer> b = ScopedValue.newInstance();
        ScopedValue.Carrier c = ScopedValue.where(a, "x").where(b, 1);
        check("x".equals(c.get(a)) && Integer.valueOf(1).equals(c.get(b)), "two bindings");
        check("x".equals(c.call(() -> a.get())), "call");
        c.run(() -> check("x".equals(a.get()) && b.get() == 1, "run sees bindings"));
        check(!a.isBound() && !b.isBound(), "unbound outside");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
