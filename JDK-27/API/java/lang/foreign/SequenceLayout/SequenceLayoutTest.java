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

    // ---- Part 3: deep contract (varlen removal, reshape/flatten matrix) ----
    static void testDeepContract() {
        SequenceLayout seq = MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_INT);

        // JDK-27 removed variable-length sequences: all negative counts are IAE
        try { MemoryLayout.sequenceLayout(-1, ValueLayout.JAVA_INT); check(false, "seq(-1) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "sequenceLayout(-1) -> IAE (varlen removed)"); }
        try { seq.withElementCount(-1); check(false, "wEC(-1) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "withElementCount(-1) -> IAE"); }
        try { seq.withElementCount(-2); check(false, "wEC(-2) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "withElementCount(-2) -> IAE"); }
        // zero-count sequence is legal
        check(MemoryLayout.sequenceLayout(0, ValueLayout.JAVA_INT).byteSize() == 0,
              "seq(0) has size 0");

        // reshape requires the product of dimensions to equal elementCount
        check(seq.reshape(1, 2, 2).elementCount() == 1, "reshape(1,2,2) outer count 1");
        check(((SequenceLayout) seq.reshape(1, 2, 2).elementLayout()).elementCount() == 2,
              "reshape(1,2,2) nests [2:[2:i4]]");
        check(seq.reshape(1, 2, 2).byteSize() == 16, "reshape preserves total size");
        check(seq.reshape(4).elementLayout() == ValueLayout.JAVA_INT,
              "reshape(4) keeps the leaf element");
        for (long bad : new long[]{5, 3, 2, 0}) {
            try {
                seq.reshape(bad);
                check(false, "reshape(" + bad + ") must not divide 4");
            } catch (IllegalArgumentException expected) {
                check(true, "reshape(" + bad + ") -> IAE (product must match)");
            }
        }
        // flatten undoes reshape exactly
        SequenceLayout nested = seq.reshape(2, 2);
        SequenceLayout flat = nested.flatten();
        check(flat.elementCount() == 4 && flat.elementLayout() == ValueLayout.JAVA_INT,
              "flatten restores a single dimension");
        check(flat.equals(seq), "flatten result equals the original sequence");

        // alignment inherits from the element layout; withByteAlignment can raise it
        check(seq.byteAlignment() == 4, "sequence alignment equals element alignment");
        check(MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_LONG).byteAlignment() == 8,
              "long-element sequence alignment 8");
        // byteOffset path arithmetic across the sequence
        check(seq.byteOffset(MemoryLayout.PathElement.sequenceElement(3)) == 12,
              "sequenceElement(3) offset 12");
        // unbound sequenceElement() selects the element layout itself
        check(seq.select(MemoryLayout.PathElement.sequenceElement()) == ValueLayout.JAVA_INT,
              "select(sequenceElement()) returns the element layout");
        // withName covariant chain keeps it a SequenceLayout
        SequenceLayout named = (SequenceLayout) seq.withName("v").withoutName();
        check(named.elementCount() == 4, "name chain preserves count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
