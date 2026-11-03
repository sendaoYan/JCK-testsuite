/*
 * @test
 * @summary java.lang.foreign.ValueLayout.OfShort conformance: white-box structure + black-box behavior.
 * @run main OfShortTest
 */
import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public class OfShortTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.ValueLayout$OfShort");
        check(c.isInterface(), "interface");
        check(ValueLayout.class.isAssignableFrom(c), "extends ValueLayout");
        check(c.getMethod("withName", String.class).getReturnType() == ValueLayout.OfShort.class, "withName covariant");
        check(c.getMethod("withoutName").getReturnType() == ValueLayout.OfShort.class, "withoutName covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == ValueLayout.OfShort.class, "withByteAlignment covariant");
        check(c.getMethod("withOrder", ByteOrder.class).getReturnType() == ValueLayout.OfShort.class, "withOrder covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ValueLayout.OfShort l = ValueLayout.JAVA_SHORT;
        check(l.byteSize() == 2, "byteSize 2");
        check(l.byteAlignment() == 2, "byteAlignment 2");
        check(l.carrier() == short.class, "carrier short");
        check(l.order() == ByteOrder.nativeOrder(), "default order native");
        check(l.withOrder(ByteOrder.BIG_ENDIAN).order() == ByteOrder.BIG_ENDIAN, "withOrder big-endian");
        check("i".equals(l.withName("i").name().orElse(null)), "withName sets name");
        check(l.withoutName().name().isEmpty(), "withoutName clears name");
        check(l.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment 8");

        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(l.byteSize() * 2, l.byteAlignment());
            seg.set(l, 0, (short) 1234);
            check(seg.get(l, 0) == (short) 1234, "set/get roundtrip");
            seg.setAtIndex(l, 1, (short) 1234);
            check(seg.getAtIndex(l, 1) == (short) 1234, "setAtIndex/getAtIndex roundtrip");
            VarHandle vh = l.varHandle();
            vh.set(seg, 0L, (short) 1234);
            check(seg.get(l, 0) == (short) 1234, "varHandle set/get roundtrip");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
