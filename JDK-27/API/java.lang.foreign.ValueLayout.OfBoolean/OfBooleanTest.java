/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Boolean conformance: sub-interface of ValueLayout for boolean.
 * @run main OfBooleanTest
 */
import java.lang.foreign.ValueLayout;

public class OfBooleanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfBoolean.class.isInterface(), "is interface");
        ValueLayout.OfBoolean v = ValueLayout.JAVA_BOOLEAN;
        check(v.byteSize() == 1, "byteSize");
        check(v.carrier() == boolean.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
