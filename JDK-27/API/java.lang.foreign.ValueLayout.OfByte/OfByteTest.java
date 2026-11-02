/*
 * @test
 * @summary java.lang.foreign.ValueLayout.Byte conformance: sub-interface of ValueLayout for byte.
 * @run main OfByteTest
 */
import java.lang.foreign.ValueLayout;

public class OfByteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.OfByte.class.isInterface(), "is interface");
        ValueLayout.OfByte v = ValueLayout.JAVA_BYTE;
        check(v.byteSize() == 1, "byteSize");
        check(v.carrier() == byte.class, "carrier");
        check(v.withName("x").name().get().equals("x"), "withName");
        check(v.withoutName().name().isEmpty(), "withoutName");
        check(v.varHandle() != null, "varHandle non-null");
        check(v.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
