/*
 * @test
 * @summary java.lang.foreign.ValueLayout conformance: interface and typed constants.
 * @run main ValueLayoutTest
 */
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

public class ValueLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ValueLayout.class.isInterface(), "is interface");
        check(ValueLayout.JAVA_INT.byteSize() == 4, "JAVA_INT size");
        check(ValueLayout.JAVA_LONG.byteSize() == 8, "JAVA_LONG size");
        check(ValueLayout.JAVA_BYTE.carrier() == byte.class, "JAVA_BYTE carrier");
        check(ValueLayout.JAVA_INT.carrier() == int.class, "JAVA_INT carrier");
        check(ValueLayout.JAVA_INT.order() == ByteOrder.nativeOrder(), "native order");
        check(ValueLayout.JAVA_INT.withOrder(ByteOrder.LITTLE_ENDIAN).order() == ByteOrder.LITTLE_ENDIAN, "withOrder");
        check(ValueLayout.JAVA_INT.withName("x").name().get().equals("x"), "withName");
        check(ValueLayout.JAVA_INT.varHandle() != null, "varHandle non-null");
        check(ValueLayout.ADDRESS != null, "ADDRESS constant");
        check(ValueLayout.JAVA_CHAR_UNALIGNED.byteAlignment() == 1, "unaligned alignment 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
