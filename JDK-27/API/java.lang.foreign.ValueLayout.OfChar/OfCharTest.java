/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Char conformance: sub-interface of ValueLayout for char.
 * @run main OfCharTest
 */
import java.lang.foreign.ValueLayout;

public class OfCharTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfChar.class.isInterface(), "is interface");
        ValueLayout.OfChar v = ValueLayout.JAVA_CHAR;
        check(v.byteSize() == 2, "byteSize");
        check(v.carrier() == char.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
