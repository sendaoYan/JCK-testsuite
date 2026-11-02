/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Int conformance: sub-interface of ValueLayout for int.
 * @run main OfIntTest
 */
import java.lang.foreign.ValueLayout;

public class OfIntTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfInt.class.isInterface(), "is interface");
        ValueLayout.OfInt v = ValueLayout.JAVA_INT;
        check(v.byteSize() == 4, "byteSize");
        check(v.carrier() == int.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
