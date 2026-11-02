/*
 * @test
 * @summary java.lang.ScopedValue conformance: newInstance/where/Carrier/orElse/orElseThrow.
 * @run main ScopedValueTest
 */
import java.lang.ScopedValue;

public class ScopedValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        ScopedValue<String> sv = ScopedValue.newInstance();
        check(!sv.isBound(), "not bound outside");
        check("d".equals(sv.orElse("d")), "orElse");
        ScopedValue.Carrier car = ScopedValue.where(sv, "hello");
        check("hello".equals(car.get(sv)), "carrier get");
        check("hello".equals(car.call(() -> sv.get())), "carrier call");
        final int[] ran = {0};
        car.run(() -> { ran[0]++; check("hello".equals(sv.get()), "run sees value"); });
        check(ran[0] == 1, "run executed");
        ScopedValue.Carrier car2 = car.where(sv, "world");
        check("world".equals(car2.get(sv)), "where overrides");
        try { sv.orElseThrow(() -> new IllegalStateException("unbound")); throw new AssertionError("should throw"); }
        catch (IllegalStateException e) { passed++; check("unbound".equals(e.getMessage()), "orElseThrow"); }
        check(sv.hashCode() == sv.hashCode(), "hashCode stable");
        check(!sv.isBound(), "still unbound outside");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
