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

    // ---- Part 3: deep contract (full carrier/order/unaligned tables) ----
    static void testDeepContract() throws Exception {
        // complete size/alignment/carrier table for the JAVA_* family
        Object[][] table = {
                {"JAVA_BYTE", ValueLayout.JAVA_BYTE, 1, byte.class},
                {"JAVA_BOOLEAN", ValueLayout.JAVA_BOOLEAN, 1, boolean.class},
                {"JAVA_CHAR", ValueLayout.JAVA_CHAR, 2, char.class},
                {"JAVA_SHORT", ValueLayout.JAVA_SHORT, 2, short.class},
                {"JAVA_INT", ValueLayout.JAVA_INT, 4, int.class},
                {"JAVA_FLOAT", ValueLayout.JAVA_FLOAT, 4, float.class},
                {"JAVA_LONG", ValueLayout.JAVA_LONG, 8, long.class},
                {"JAVA_DOUBLE", ValueLayout.JAVA_DOUBLE, 8, double.class},
        };
        for (Object[] row : table) {
            ValueLayout vl = (ValueLayout) row[1];
            String tag = (String) row[0];
            check(vl.byteSize() == ((Integer) row[2]).longValue(), tag + " byteSize " + row[2]);
            check(vl.byteAlignment() == vl.byteSize(), tag + " alignment == size (compact)");
            check(vl.carrier() == row[3], tag + " carrier " + row[3]);
            check(vl.order() == ByteOrder.nativeOrder(), tag + " order is nativeOrder");
            check(vl.name().isEmpty(), tag + " has no name by default");
        }
        // the complete UNALIGNED family: same size, alignment 1
        ValueLayout[] unaligned = {ValueLayout.JAVA_CHAR_UNALIGNED, ValueLayout.JAVA_SHORT_UNALIGNED,
                ValueLayout.JAVA_INT_UNALIGNED, ValueLayout.JAVA_FLOAT_UNALIGNED,
                ValueLayout.JAVA_LONG_UNALIGNED, ValueLayout.JAVA_DOUBLE_UNALIGNED,
                ValueLayout.ADDRESS_UNALIGNED};
        for (ValueLayout u : unaligned)
            check(u.byteAlignment() == 1 && u.byteSize() >= 2,
                  "unaligned variant alignment 1: " + u);
        check(ValueLayout.JAVA_INT_UNALIGNED.byteSize() == 4
                      && ValueLayout.JAVA_INT_UNALIGNED.carrier() == int.class,
              "unaligned keeps size and carrier");
        // alignment surgery
        check(ValueLayout.JAVA_INT.withByteAlignment(1).byteAlignment() == 1,
              "JAVA_INT.withByteAlignment(1) is legal");
        check(ValueLayout.JAVA_INT.withByteAlignment(1).byteSize() == 4,
              "withByteAlignment does not change size");
        try {
            ValueLayout.JAVA_INT.withByteAlignment(3);
            check(false, "non-power-of-two alignment rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "JAVA_INT.withByteAlignment(3) -> IAE");
        }
        // withName/withoutName round trip keeps value equality
        check(ValueLayout.JAVA_INT.withName("x").withoutName().equals(ValueLayout.JAVA_INT),
              "withName/withoutName value round trip");
        check(!ValueLayout.JAVA_INT.withName("x").equals(ValueLayout.JAVA_INT),
              "naming changes layout identity");
        // varHandle carrier and coordinates
        VarHandle vh = ValueLayout.JAVA_LONG.varHandle();
        check(vh.varType() == long.class, "JAVA_LONG varHandle varType");
        check(java.util.Arrays.equals(vh.coordinateTypes().toArray(),
                      new Class<?>[]{MemorySegment.class, long.class}),
              "varHandle coordinates are (MemorySegment, long)");
        // UNALIGNED layouts actually permit off-alignment access (measured)
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(8, 8);
            seg.set(ValueLayout.JAVA_INT_UNALIGNED, 1, 7);
            check(seg.get(ValueLayout.JAVA_INT_UNALIGNED, 1) == 7,
                  "JAVA_INT_UNALIGNED@1 round-trips");
            try {
                seg.get(ValueLayout.JAVA_INT, 1);
                check(false, "aligned layout at odd offset must fail");
            } catch (IllegalArgumentException expected) {
                check(true, "JAVA_INT@1 -> IAE while UNALIGNED variant succeeds");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
