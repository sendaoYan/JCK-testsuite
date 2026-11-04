/*
 * @test
 * @summary java.lang.foreign.SequenceLayout conformance: white-box structure + black-box behavior.
 * @run main SequenceLayoutTest
 */
import java.lang.foreign.*;

public class SequenceLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.SequenceLayout");
        check(c.isInterface(), "interface");
        check(MemoryLayout.class.isAssignableFrom(c), "extends MemoryLayout");
        check(c.getMethod("elementLayout").getReturnType() == MemoryLayout.class, "elementLayout returns MemoryLayout");
        check(c.getMethod("elementCount").getReturnType() == long.class, "elementCount returns long");
        check(c.getMethod("withElementCount", long.class).getReturnType() == SequenceLayout.class,
              "withElementCount returns SequenceLayout");
        check(c.getMethod("reshape", long[].class).getReturnType() == SequenceLayout.class, "reshape returns SequenceLayout");
        check(c.getMethod("flatten").getReturnType() == SequenceLayout.class, "flatten returns SequenceLayout");
        check(c.getMethod("withName", String.class).getReturnType() == SequenceLayout.class, "withName covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == SequenceLayout.class,
              "withByteAlignment covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SequenceLayout seq = MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT);
        check(seq.elementCount() == 3, "elementCount");
        check(seq.elementLayout() == ValueLayout.JAVA_INT, "elementLayout");
        check(seq.byteSize() == 12, "byteSize");
        check(seq.byteOffset(MemoryLayout.PathElement.sequenceElement(2)) == 8, "sequenceElement offset");

        SequenceLayout five = seq.withElementCount(5);
        check(five.elementCount() == 5, "withElementCount changes count");
        check(five.byteSize() == 20, "withElementCount byteSize");
        check(seq.elementCount() == 3, "original unchanged");

        SequenceLayout nested = MemoryLayout.sequenceLayout(2, MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT));
        check(nested.elementCount() == 2, "nested outer count");
        check(nested.byteSize() == 24, "nested byteSize");

        SequenceLayout reshaped = nested.reshape(6);
        check(reshaped.elementCount() == 6, "reshape to single dimension");
        check(reshaped.elementLayout() == ValueLayout.JAVA_INT, "reshape element layout");
        check(reshaped.byteSize() == 24, "reshape preserves byteSize");

        SequenceLayout reshaped2 = nested.reshape(2, 3);
        check(reshaped2.elementCount() == 2, "reshape(2,3) outer count");
        check(reshaped2.elementLayout() instanceof SequenceLayout, "reshape(2,3) element is sequence");

        SequenceLayout flat = reshaped2.flatten();
        check(flat.elementCount() == 6, "flatten merges dimensions");
        check(flat.elementLayout() == ValueLayout.JAVA_INT, "flatten element layout");

        SequenceLayout named = seq.withName("vec");
        check("vec".equals(named.name().orElse(null)), "withName");
        check(named.withoutName().name().isEmpty(), "withoutName");
        check(named.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
