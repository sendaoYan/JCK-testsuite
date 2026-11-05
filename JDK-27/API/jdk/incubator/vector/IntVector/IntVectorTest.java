/*
 * @test
 * @summary jdk.incubator.vector.IntVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main IntVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class IntVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.IntVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(IntVector.SPECIES_64 != null && IntVector.SPECIES_128 != null
              && IntVector.SPECIES_256 != null && IntVector.SPECIES_512 != null, "species constants");
        check(IntVector.SPECIES_64.elementType() == int.class, "element type int");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "lanewise"), "lanewise");
        check(hasMethod(c, "fromArray"), "fromArray");
        check(hasMethod(c, "intoArray"), "intoArray");
    }

    static void testBlackBox() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        IntVector a = IntVector.broadcast(sp, 7);
        IntVector b = IntVector.broadcast(sp, 3);
        check(a.lane(0) == 7, "broadcast lane");
        IntVector sum = a.add(b);
        check(sum.lane(0) == 10 && sum.lane(n - 1) == 10, "add lanes");
        IntVector diff = a.sub(b);
        check(diff.lane(0) == 4, "sub lane");
        IntVector neg = a.neg();
        check(neg.lane(0) == -7, "neg lane");
        int[] arr = new int[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 10 && arr[n - 1] == 10, "intoArray round-trip");
        IntVector back = IntVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        int reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == 7 * n, "reduceLanes ADD");
        VectorMask<Integer> eq = a.eq(b);
        check(!eq.allTrue(), "eq mask not all true");
    }


    static void testKernelVsScalar() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_128;
        int len = 13;
        int[] a = new int[len], b = new int[len], c = new int[len], out = new int[len];
        for (int i = 0; i < len; i++) { a[i] = i + 1; b[i] = 10 - i; c[i] = i * i; }
        int bound = sp.loopBound(len);
        check(bound == 12, "loopBound(13) == 12");
        for (int i = 0; i < bound; i += sp.length()) {
            IntVector.fromArray(sp, a, i)
                    .mul(IntVector.fromArray(sp, b, i))
                    .add(IntVector.fromArray(sp, c, i))
                    .intoArray(out, i);
        }
        VectorMask<Integer> tail = sp.maskAll(true).indexInRange(bound, len);
        check(tail.toString().equals("Mask[T...]"), "tail mask covers 1 of 4 lanes");
        IntVector.fromArray(sp, a, bound, tail)
                .mul(IntVector.fromArray(sp, b, bound, tail))
                .add(IntVector.fromArray(sp, c, bound, tail))
                .intoArray(out, bound, tail);
        for (int i = 0; i < len; i++) check(out[i] == a[i] * b[i] + c[i], "vector kernel equals scalar lane " + i);
    }

    static void testLanewiseMatrix() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_128;
        check(IntVector.broadcast(sp, 1).lanewise(VectorOperators.ROL, 4).lane(0) == 16, "ROL");
        check(IntVector.broadcast(sp, 16).lanewise(VectorOperators.ROR, 4).lane(0) == 1, "ROR");
        check(IntVector.broadcast(sp, 1).lanewise(VectorOperators.LSHL, 31).lane(0) == Integer.MIN_VALUE, "LSHL 31");
        check(IntVector.broadcast(sp, -16).lanewise(VectorOperators.ASHR, 2).lane(0) == -4, "ASHR sign ext");
        check(IntVector.broadcast(sp, -16).lanewise(VectorOperators.LSHR, 2).lane(0) == 1073741820, "LSHR zero fill");
        check(IntVector.broadcast(sp, 0x0F0F0F0F).lanewise(VectorOperators.BIT_COUNT).lane(0) == 16, "BIT_COUNT");
        check(IntVector.broadcast(sp, 8).lanewise(VectorOperators.TRAILING_ZEROS_COUNT).lane(0) == 3, "TRAILING_ZEROS_COUNT");
        check(IntVector.broadcast(sp, 1).lanewise(VectorOperators.LEADING_ZEROS_COUNT).lane(0) == 31, "LEADING_ZEROS_COUNT");
        check(IntVector.broadcast(sp, 0).lanewise(VectorOperators.LEADING_ZEROS_COUNT).lane(0) == 32, "LEADING_ZEROS_COUNT zero");
        IntVector zomo = IntVector.fromArray(sp, new int[] {0, 3, -1, 0}, 0).lanewise(VectorOperators.ZOMO);
        check(zomo.lane(0) == 0 && zomo.lane(1) == -1 && zomo.lane(2) == -1 && zomo.lane(3) == 0, "ZOMO zero-or-minus-one");
        check(IntVector.broadcast(sp, 0b1011).lanewise(VectorOperators.COMPRESS_BITS,
                IntVector.broadcast(sp, 0b0101)).lane(0) == 0b01, "COMPRESS_BITS");
        check(IntVector.broadcast(sp, 0b0011).lanewise(VectorOperators.EXPAND_BITS,
                IntVector.broadcast(sp, 0b0101)).lane(0) == 0b0101, "EXPAND_BITS");
        check(IntVector.broadcast(sp, 0xF0).lanewise(VectorOperators.AND_NOT,
                IntVector.broadcast(sp, 0xFF)).lane(0) == 0, "AND_NOT");
    }

    static void testDeepen() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_128;
        IntVector v = IntVector.fromArray(sp, new int[] {1, 2, 3, 4}, 0);
        check(v.reduceLanes(VectorOperators.FIRST_NONZERO) == 1, "FIRST_NONZERO first");
        check(IntVector.fromArray(sp, new int[] {0, 0, 5, 0}, 0).reduceLanes(VectorOperators.FIRST_NONZERO) == 5, "FIRST_NONZERO 5");
        check(IntVector.zero(sp).reduceLanes(VectorOperators.FIRST_NONZERO) == 0, "FIRST_NONZERO all zero");
        VectorMask<Integer> m = VectorMask.fromValues(sp, false, true, false, true);
        check(v.compress(m).lane(0) == 2 && v.compress(m).lane(1) == 4, "compress lanes");
        check(IntVector.broadcast(sp, 7).expand(m).lane(0) == 0 && IntVector.broadcast(sp, 7).expand(m).lane(1) == 7, "expand lanes");
        IntVector sel = v.selectFrom(IntVector.fromArray(sp, new int[] {2, 0, 3, 1}, 0));
        check(sel.lane(0) == 0 && sel.lane(1) == 3 && sel.lane(2) == 1 && sel.lane(3) == 2, "selectFrom gather b[this[i]]");
        check(v.slice(1).lane(0) == 2 && v.slice(1).lane(3) == 0, "slice(1) zero fill");
        check(v.slice(1).unslice(1, IntVector.zero(sp), 0,
                VectorMask.fromValues(sp, false, false, true, true)).lane(3) == 4, "unslice masked");
        IntVector bits = IntVector.broadcast(sp, 0x04030201);
        ByteVector bytes = (ByteVector) bits.reinterpretShape(ByteVector.SPECIES_128, 0);
        check(bytes.lane(0) == 0x01 && bytes.lane(1) == 0x02 && bytes.lane(3) == 0x04, "reinterpretShape little-endian");
        check(((IntVector) bytes.reinterpretShape(sp, 0)).equals(bits), "reinterpretShape round-trip");
        check(((DoubleVector) IntVector.broadcast(sp, 7).convert(VectorOperators.I2D, 0)).lane(0) == 7.0, "I2D");
        check(((LongVector) IntVector.broadcast(sp, 7).convert(VectorOperators.I2L, 0)).lane(0) == 7L, "I2L");
        check(((FloatVector) IntVector.broadcast(sp, 7).convert(VectorOperators.I2F, 0)).lane(0) == 7.0f, "I2F");
        check(((IntVector) FloatVector.broadcast(FloatVector.SPECIES_128, 2.75f).convert(VectorOperators.F2I, 0)).lane(0) == 2, "F2I truncates");
        check(((IntVector) FloatVector.broadcast(FloatVector.SPECIES_128, 1.0f).convert(VectorOperators.REINTERPRET_F2I, 0)).lane(0) == 0x3F800000, "REINTERPRET_F2I bits");
        IntVector u = IntVector.fromArray(sp, new int[] {-1, 2, 3, 4}, 0);
        IntVector two = IntVector.broadcast(sp, 2);
        check(u.compare(VectorOperators.ULT, two).toString().equals("Mask[....]"), "ULT -1 unsigned not < 2");
        check(u.compare(VectorOperators.LT, two).laneIsSet(0), "LT signed -1 < 2 true");
        check(u.compare(VectorOperators.UGT, two).laneIsSet(0), "UGT -1 unsigned > 2");
        check(u.compare(VectorOperators.UGE, two).laneIsSet(0) && u.compare(VectorOperators.UGE, two).laneIsSet(1), "UGE");
        check(u.compare(VectorOperators.ULE, two).laneIsSet(1), "ULE lane1");
        check(v.reduceLanesToLong(VectorOperators.ADD) == 10L, "reduceLanesToLong");
        check(v.reduceLanes(VectorOperators.AND) == 0, "AND reduce");
        check(IntVector.broadcast(sp, 0xF0).reduceLanes(VectorOperators.AND) == 0xF0, "AND broadcast");
        check(v.reduceLanes(VectorOperators.XOR) == 4, "XOR reduce");
        check(v.reduceLanes(VectorOperators.MIN) == 1 && v.reduceLanes(VectorOperators.MAX) == 4, "MIN/MAX reduce");
        check(v.reduceLanes(VectorOperators.ADD, m) == 6, "masked reduce ADD");
        check(v.blend(IntVector.broadcast(sp, 10), m).lane(0) == 1
                && v.blend(IntVector.broadcast(sp, 10), m).lane(1) == 10, "blend");
        check(v.lanewise(VectorOperators.ADD, IntVector.broadcast(sp, 10), m).lane(1) == 12
                && v.lanewise(VectorOperators.ADD, IntVector.broadcast(sp, 10), m).lane(0) == 1, "masked lanewise");
        check(v.lanewise(VectorOperators.ADD, 10, m).lane(1) == 12, "masked lanewise scalar");
        check(v.toString().equals("[1, 2, 3, 4]"), "toString exact");
        check(v.hashCode() == IntVector.fromArray(sp, new int[] {1, 2, 3, 4}, 0).hashCode(), "hashCode consistent");
    }

    public static void main(String[] args) throws Exception {
        testKernelVsScalar();
        testLanewiseMatrix();
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
