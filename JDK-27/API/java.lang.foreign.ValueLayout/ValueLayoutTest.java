/*
 * @test
 * @summary java.lang.foreign.ValueLayout conformance: white-box structure + black-box behavior.
 * @run main ValueLayoutTest
 */
import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public class ValueLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.ValueLayout");
        check(c.isInterface(), "interface");
        check(MemoryLayout.class.isAssignableFrom(c), "extends MemoryLayout");
        check(c.getField("ADDRESS").getType() == AddressLayout.class, "ADDRESS is AddressLayout");
        check(c.getField("JAVA_INT").getType() == ValueLayout.OfInt.class, "JAVA_INT is OfInt");
        check(c.getField("JAVA_LONG").getType() == ValueLayout.OfLong.class, "JAVA_LONG is OfLong");
        check(c.getField("JAVA_DOUBLE").getType() == ValueLayout.OfDouble.class, "JAVA_DOUBLE is OfDouble");
        check(c.getField("JAVA_BOOLEAN").getType() == ValueLayout.OfBoolean.class, "JAVA_BOOLEAN is OfBoolean");
        check(c.getMethod("order").getReturnType() == ByteOrder.class, "order returns ByteOrder");
        check(c.getMethod("withOrder", ByteOrder.class).getReturnType() == ValueLayout.class, "withOrder returns ValueLayout");
        check(c.getMethod("carrier").getReturnType() == Class.class, "carrier returns Class");
        check(c.getMethod("varHandle").getReturnType() == VarHandle.class, "varHandle returns VarHandle");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(ValueLayout.JAVA_BYTE.byteSize() == 1 && ValueLayout.JAVA_BYTE.byteAlignment() == 1, "JAVA_BYTE 1/1");
        check(ValueLayout.JAVA_BOOLEAN.byteSize() == 1 && ValueLayout.JAVA_BOOLEAN.byteAlignment() == 1, "JAVA_BOOLEAN 1/1");
        check(ValueLayout.JAVA_CHAR.byteSize() == 2 && ValueLayout.JAVA_CHAR.byteAlignment() == 2, "JAVA_CHAR 2/2");
        check(ValueLayout.JAVA_SHORT.byteSize() == 2 && ValueLayout.JAVA_SHORT.byteAlignment() == 2, "JAVA_SHORT 2/2");
        check(ValueLayout.JAVA_INT.byteSize() == 4 && ValueLayout.JAVA_INT.byteAlignment() == 4, "JAVA_INT 4/4");
        check(ValueLayout.JAVA_FLOAT.byteSize() == 4 && ValueLayout.JAVA_FLOAT.byteAlignment() == 4, "JAVA_FLOAT 4/4");
        check(ValueLayout.JAVA_LONG.byteSize() == 8 && ValueLayout.JAVA_LONG.byteAlignment() == 8, "JAVA_LONG 8/8");
        check(ValueLayout.JAVA_DOUBLE.byteSize() == 8 && ValueLayout.JAVA_DOUBLE.byteAlignment() == 8, "JAVA_DOUBLE 8/8");

        check(ValueLayout.JAVA_INT.carrier() == int.class, "JAVA_INT carrier int");
        check(ValueLayout.JAVA_LONG.carrier() == long.class, "JAVA_LONG carrier long");
        check(ValueLayout.JAVA_BYTE.carrier() == byte.class, "JAVA_BYTE carrier byte");
        check(ValueLayout.JAVA_BOOLEAN.carrier() == boolean.class, "JAVA_BOOLEAN carrier boolean");

        check(ValueLayout.JAVA_INT.order() == ByteOrder.nativeOrder(), "default order native");
        check(ValueLayout.JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN).order() == ByteOrder.BIG_ENDIAN, "withOrder");

        ValueLayout named = ValueLayout.JAVA_INT.withName("i");
        check("i".equals(named.name().orElse(null)), "withName");
        check(named.withoutName().name().isEmpty(), "withoutName");
        check(named.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");

        check(ValueLayout.JAVA_INT_UNALIGNED.byteAlignment() == 1, "unaligned variant alignment 1");
        check(ValueLayout.JAVA_INT_UNALIGNED.byteSize() == 4, "unaligned variant same size");

        VarHandle vh = ValueLayout.JAVA_INT.varHandle();
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(8, 8);
            vh.set(seg, 0L, 777);
            check((int) vh.get(seg, 0L) == 777, "varHandle set/get");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
