/*
 * @test
 * @summary java.lang.foreign.AddressLayout conformance: interface and ADDRESS layout operations.
 * @run main AddressLayoutTest
 */
import java.lang.foreign.AddressLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

public class AddressLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(AddressLayout.class.isInterface(), "is interface");
        AddressLayout a = ValueLayout.ADDRESS;
        check(a != null, "ADDRESS constant");
        check(a.withName("p").name().get().equals("p"), "withName");
        check(a.withoutName().name().isEmpty(), "withoutName");
        check(a.targetLayout().isEmpty(), "default targetLayout empty");
        check(a.withTargetLayout(ValueLayout.JAVA_INT).targetLayout().get() == ValueLayout.JAVA_INT, "withTargetLayout");
        check(a.withOrder(ByteOrder.LITTLE_ENDIAN).order() == ByteOrder.LITTLE_ENDIAN, "withOrder");
        check(a.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
