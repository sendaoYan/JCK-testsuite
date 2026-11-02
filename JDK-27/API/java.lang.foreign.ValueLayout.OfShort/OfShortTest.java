/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Short conformance: sub-interface of ValueLayout for short.
 * @run main OfShortTest
 */
import java.lang.foreign.ValueLayout;

public class OfShortTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfShort.class.isInterface(), "is interface");
        ValueLayout.OfShort v = ValueLayout.JAVA_SHORT;
        check(v.byteSize() == 2, "byteSize");
        check(v.carrier() == short.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
