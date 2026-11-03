/*
 * @test
 * @summary java.lang.foreign.AddressLayout conformance: white-box structure + black-box behavior.
 * @run main AddressLayoutTest
 */
import java.lang.foreign.*;
import java.lang.reflect.*;
import java.nio.ByteOrder;
import java.util.Optional;

public class AddressLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.AddressLayout");
        check(c.isInterface(), "interface");
        check(ValueLayout.class.isAssignableFrom(c), "extends ValueLayout");
        check(c.getMethod("withTargetLayout", MemoryLayout.class).getReturnType() == AddressLayout.class,
              "withTargetLayout returns AddressLayout");
        check(c.getMethod("withoutTargetLayout").getReturnType() == AddressLayout.class,
              "withoutTargetLayout returns AddressLayout");
        check(c.getMethod("targetLayout").getReturnType() == Optional.class, "targetLayout returns Optional");
        check(c.getMethod("withName", String.class).getReturnType() == AddressLayout.class, "withName covariant");
        check(c.getMethod("withOrder", ByteOrder.class).getReturnType() == AddressLayout.class, "withOrder covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == AddressLayout.class,
              "withByteAlignment covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AddressLayout a = ValueLayout.ADDRESS;
        check(a.byteSize() == 4 || a.byteSize() == 8, "address byteSize is pointer width (4 or 8)");
        check(a.byteAlignment() == a.byteSize(), "address alignment equals size");
        check(a.order() == ByteOrder.nativeOrder(), "default order is native order");
        check(a.carrier() == MemorySegment.class, "address carrier is MemorySegment");
        check(a.targetLayout().isEmpty(), "plain ADDRESS has no target layout");

        AddressLayout t = a.withTargetLayout(ValueLayout.JAVA_INT);
        check(t.targetLayout().isPresent() && t.targetLayout().get() == ValueLayout.JAVA_INT,
              "withTargetLayout sets target layout");
        check(t.withoutTargetLayout().targetLayout().isEmpty(), "withoutTargetLayout clears target");

        AddressLayout be = a.withOrder(ByteOrder.BIG_ENDIAN);
        check(be.order() == ByteOrder.BIG_ENDIAN, "withOrder applies order");
        check(a.withOrder(ByteOrder.nativeOrder()).order() == ByteOrder.nativeOrder(), "withOrder roundtrip");

        AddressLayout named = a.withName("ptr");
        check("ptr".equals(named.name().orElse(null)), "withName sets name");
        check(named.withoutName().name().isEmpty(), "withoutName clears name");

        AddressLayout aligned = a.withByteAlignment(a.byteAlignment());
        check(aligned.byteAlignment() == a.byteAlignment(), "withByteAlignment preserves natural alignment");
        check(a.equals(a.withTargetLayout(ValueLayout.JAVA_INT).withoutTargetLayout()),
              "withTargetLayout/withoutTargetLayout roundtrip equals original");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
