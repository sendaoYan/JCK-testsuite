/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Double conformance: sub-interface of ValueLayout for double.
 * @run main OfDoubleTest
 */
import java.lang.foreign.ValueLayout;

public class OfDoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfDouble.class.isInterface(), "is interface");
        ValueLayout.OfDouble v = ValueLayout.JAVA_DOUBLE;
        check(v.byteSize() == 8, "byteSize");
        check(v.carrier() == double.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
