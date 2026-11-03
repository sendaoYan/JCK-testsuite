/*
 * @test
 * @summary java.lang.foreign.ValueLayout.OfDouble conformance: white-box structure + black-box behavior.
 * @run main OfDoubleTest
 */
import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public class OfDoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.ValueLayout$OfDouble");
        check(c.isInterface(), "interface");
        check(ValueLayout.class.isAssignableFrom(c), "extends ValueLayout");
        check(c.getMethod("withName", String.class).getReturnType() == ValueLayout.OfDouble.class, "withName covariant");
        check(c.getMethod("withoutName").getReturnType() == ValueLayout.OfDouble.class, "withoutName covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == ValueLayout.OfDouble.class, "withByteAlignment covariant");
        check(c.getMethod("withOrder", ByteOrder.class).getReturnType() == ValueLayout.OfDouble.class, "withOrder covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ValueLayout.OfDouble l = ValueLayout.JAVA_DOUBLE;
        check(l.byteSize() == 8, "byteSize 8");
        check(l.byteAlignment() == 8, "byteAlignment 8");
        check(l.carrier() == double.class, "carrier double");
        check(l.order() == ByteOrder.nativeOrder(), "default order native");
        check(l.withOrder(ByteOrder.BIG_ENDIAN).order() == ByteOrder.BIG_ENDIAN, "withOrder big-endian");
        check("i".equals(l.withName("i").name().orElse(null)), "withName sets name");
        check(l.withoutName().name().isEmpty(), "withoutName clears name");
        check(l.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment 8");

        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(l.byteSize() * 2, l.byteAlignment());
            seg.set(l, 0, 2.5);
            check(seg.get(l, 0) == 2.5, "set/get roundtrip");
            seg.setAtIndex(l, 1, 2.5);
            check(seg.getAtIndex(l, 1) == 2.5, "setAtIndex/getAtIndex roundtrip");
            VarHandle vh = l.varHandle();
            vh.set(seg, 0L, 2.5);
            check(seg.get(l, 0) == 2.5, "varHandle set/get roundtrip");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
