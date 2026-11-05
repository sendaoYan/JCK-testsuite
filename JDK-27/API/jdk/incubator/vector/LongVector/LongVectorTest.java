/*
 * @test
 * @summary jdk.incubator.vector.LongVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main LongVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class LongVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.LongVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(LongVector.SPECIES_64 != null && LongVector.SPECIES_128 != null, "species constants");
        check(LongVector.SPECIES_64.elementType() == long.class, "element type long");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Long> sp = LongVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        LongVector a = LongVector.broadcast(sp, 99L);
        LongVector b = LongVector.broadcast(sp, 1L);
        check(a.lane(0) == 99L, "broadcast lane");
        LongVector sum = a.add(b);
        check(sum.lane(0) == 100L && sum.lane(n - 1) == 100L, "add lanes");
        long[] arr = new long[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 100L && arr[n - 1] == 100L, "intoArray round-trip");
        LongVector back = LongVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        long reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == 99L * n, "reduceLanes ADD");
    }


    static void testDeepen() {
        VectorSpecies<Long> sp = LongVector.SPECIES_128;
        check(sp.length() == 2, "SPECIES_128 long length 2");
        check(LongVector.broadcast(sp, 1L).lanewise(VectorOperators.LSHL, 63).lane(0) == Long.MIN_VALUE, "LSHL 63");
        check(LongVector.broadcast(sp, -1L).lanewise(VectorOperators.BIT_COUNT).lane(0) == 64, "BIT_COUNT of -1L is 64");
        check(LongVector.broadcast(sp, 0x0102030405060708L).lanewise(VectorOperators.REVERSE_BYTES).lane(0)
                == 0x0807060504030201L, "REVERSE_BYTES");
        check(LongVector.broadcast(sp, 0x0102030405060708L).lanewise(VectorOperators.REVERSE).lane(0)
                == Long.reverse(0x0102030405060708L), "REVERSE matches Long.reverse");
        check(LongVector.broadcast(sp, 1L).lanewise(VectorOperators.LEADING_ZEROS_COUNT).lane(0) == 63, "LZ 63");
        check(LongVector.broadcast(sp, 0L).lanewise(VectorOperators.TRAILING_ZEROS_COUNT).lane(0) == 64, "TZ zero 64");
        DoubleVector d15 = DoubleVector.broadcast(DoubleVector.SPECIES_128, 1.5);
        check(d15.viewAsIntegralLanes().elementType() == long.class, "viewAsIntegralLanes long");
        check(((LongVector) d15.convert(VectorOperators.REINTERPRET_D2L, 0)).lane(0) == 0x3FF8000000000000L, "REINTERPRET_D2L bits of 1.5");
        check(((LongVector) d15.viewAsIntegralLanes()).viewAsFloatingLanes().equals(d15), "viewAs round-trip");
        check(LongVector.broadcast(sp, -1L).compare(VectorOperators.ULT,
                LongVector.broadcast(sp, 1L)).toString().equals("Mask[..]"), "ULT unsigned -1L not < 1");
        check(LongVector.broadcast(sp, -1L).compare(VectorOperators.UGT,
                LongVector.broadcast(sp, 1L)).toString().equals("Mask[TT]"), "UGT unsigned -1L > 1");
        VectorSpecies<Long> sp4 = LongVector.SPECIES_256;
        LongVector v = LongVector.fromArray(sp4, new long[] {3L, 1L, 4L, 1L}, 0);
        check(v.reduceLanesToLong(VectorOperators.ADD) == 9L, "reduceLanesToLong");
        check(v.reduceLanes(VectorOperators.FIRST_NONZERO) == 3L, "FIRST_NONZERO");
        check(v.reduceLanes(VectorOperators.MIN) == 1L && v.reduceLanes(VectorOperators.MAX) == 4L, "MIN/MAX reduce");
        VectorMask<Long> m = VectorMask.fromValues(sp4, true, false, true, false);
        check(v.reduceLanes(VectorOperators.ADD, m) == 7L, "masked reduce ADD");
        check(v.compress(m).lane(0) == 3L && v.compress(m).lane(1) == 4L, "compress lanes");
        check(LongVector.broadcast(sp4, 7L).expand(m).lane(0) == 7L
                && LongVector.broadcast(sp4, 7L).expand(m).lane(1) == 0, "expand lanes");
        check(v.blend(LongVector.broadcast(sp4, 99L), m).lane(0) == 99L
                && v.blend(LongVector.broadcast(sp4, 99L), m).lane(1) == 1L, "blend");
        check(v.toString().equals("[3, 1, 4, 1]"), "toString exact");
        check(v.withLane(0, 42L).lane(0) == 42L, "withLane");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
