/*
 * @test
 * @summary jdk.incubator.vector.Vector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.Vector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "species"), "species");
        check(hasMethod(c, "elementType"), "elementType");
        check(hasMethod(c, "length"), "length");
        check(hasMethod(c, "shape"), "shape");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "reduceLanesToLong"), "reduceLanesToLong");
    }

    static void testBlackBox() {
        Vector<Integer> v = IntVector.broadcast(IntVector.SPECIES_256, 7);
        int n = v.length();
        check(n > 0, "length positive");
        check(v.elementType() == int.class, "elementType");
        check(v.elementSize() == 32, "elementSize");
        check(v.shape() != null, "shape non-null");
        check(v.species() == IntVector.SPECIES_256, "species");
        Vector<Integer> w = v.broadcast(1);
        check(w.length() == n, "broadcast length");
        Vector<Integer> sum = v.add(w);
        check(((IntVector) sum).lane(0) == 8, "add lane 0");
        check(((IntVector) sum).lane(n - 1) == 8, "add lane last");
        long red = v.reduceLanesToLong(VectorOperators.ADD);
        check(red == 7L * n, "reduceLanesToLong ADD");
        check(v.eq(v.broadcast(7)).allTrue(), "eq self allTrue");
    }


    static void testDeepen() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_128;
        IntVector v = IntVector.fromArray(sp, new int[] {1, 2, 3, 4}, 0);
        check(v.toString().equals("[1, 2, 3, 4]"), "toString exact");
        check(v.elementSize() * v.length() == v.species().vectorBitSize(), "elementSize*length == vectorBitSize");
        check(v.shape() == v.species().vectorShape(), "shape == species.vectorShape");
        check(v.viewAsIntegralLanes().elementType() == int.class, "integral species viewAs identity type");
        check(v.viewAsIntegralLanes().equals(v), "viewAsIntegralLanes identity for int");
        DoubleVector d = DoubleVector.fromArray(DoubleVector.SPECIES_128, new double[] {1.5, -2.25}, 0);
        jdk.incubator.vector.Vector<Long> bits = d.viewAsIntegralLanes();
        check(bits.elementType() == long.class && bits.length() == d.length(), "double views as long, same lanes");
        check(bits.viewAsFloatingLanes().equals(d), "viewAs round-trip restores values");
        VectorMask<Integer> m = VectorMask.fromValues(sp, false, true, false, true);
        check(v.compress(m).lane(0) == 2 && v.compress(m).lane(1) == 4, "compress lanes");
        check(IntVector.broadcast(sp, 7).expand(m).lane(1) == 7, "expand lanes");
        IntVector gathered = v.selectFrom(IntVector.fromArray(sp, new int[] {3, 0, 2, 1}, 0));
        check(gathered.lane(0) == 0 && gathered.lane(1) == 2
                && gathered.lane(2) == 1 && gathered.lane(3) == 3, "selectFrom this supplies indexes");
        check(v.slice(1).lane(0) == 2, "slice(1)");
        check(v.slice(1).unslice(1, IntVector.zero(sp), 0, m).lane(3) == 0, "unslice masked writes only set lanes");
        check(v.reduceLanesToLong(VectorOperators.ADD) == 10L, "reduceLanesToLong");
        check(v.reduceLanesToLong(VectorOperators.ADD, m) == 6L, "reduceLanesToLong masked");
        check(v.compare(VectorOperators.LT, IntVector.broadcast(sp, 3)).toString().equals("Mask[TT..]"), "compare LT");
        check(v.eq(IntVector.broadcast(sp, 2)).toString().equals("Mask[.T..]"), "eq mask position");
        check(v.hashCode() == IntVector.fromArray(sp, new int[] {1, 2, 3, 4}, 0).hashCode(), "hashCode value-based");
        check(!v.equals(IntVector.fromArray(sp, new int[] {4, 3, 2, 1}, 0)), "equals lane-order sensitive");
        check(v.withLane(2, 30).lane(2) == 30 && v.withLane(2, 30).lane(0) == 1, "withLane immutable");
        check(IntVector.zero(sp).test(VectorOperators.IS_DEFAULT).allTrue(), "IS_DEFAULT on zero");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
