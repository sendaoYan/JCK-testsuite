/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Float conformance: sub-interface of ValueLayout for float.
 * @run main OfFloatTest
 */
import java.lang.foreign.ValueLayout;

public class OfFloatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfFloat.class.isInterface(), "is interface");
        ValueLayout.OfFloat v = ValueLayout.JAVA_FLOAT;
        check(v.byteSize() == 4, "byteSize");
        check(v.carrier() == float.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
