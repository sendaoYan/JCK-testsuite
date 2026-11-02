/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Long conformance: sub-interface of ValueLayout for long.
 * @run main OfLongTest
 */
import java.lang.foreign.ValueLayout;

public class OfLongTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfLong.class.isInterface(), "is interface");
        ValueLayout.OfLong v = ValueLayout.JAVA_LONG;
        check(v.byteSize() == 8, "byteSize");
        check(v.carrier() == long.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
