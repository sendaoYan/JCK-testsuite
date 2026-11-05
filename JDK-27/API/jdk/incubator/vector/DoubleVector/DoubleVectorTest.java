/*
 * @test
 * @summary jdk.incubator.vector.DoubleVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main DoubleVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class DoubleVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.DoubleVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(DoubleVector.SPECIES_128 != null && DoubleVector.SPECIES_256 != null, "species constants");
        check(DoubleVector.SPECIES_128.elementType() == double.class, "element type double");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "sub"), "sub");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Double> sp = DoubleVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        DoubleVector a = DoubleVector.broadcast(sp, 6.0);
        DoubleVector b = DoubleVector.broadcast(sp, 2.0);
        check(a.lane(0) == 6.0, "broadcast lane");
        DoubleVector sum = a.add(b);
        check(sum.lane(0) == 8.0 && sum.lane(n - 1) == 8.0, "add lanes");
        DoubleVector diff = a.sub(b);
        check(diff.lane(0) == 4.0, "sub lane");
        double[] arr = new double[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 8.0 && arr[n - 1] == 8.0, "intoArray round-trip");
        DoubleVector back = DoubleVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        double reduced = a.reduceLanes(VectorOperators.ADD);
        check(Math.abs(reduced - 6.0 * n) < 1e-9, "reduceLanes ADD");
    }


    static void testDeepen() {
        VectorSpecies<Double> sp = DoubleVector.SPECIES_128;
        check(sp.length() == 2, "SPECIES_128 double length 2");
        DoubleVector d = DoubleVector.fromArray(sp, new double[] {1.5, -2.25}, 0);
        LongVector lv = (LongVector) d.viewAsIntegralLanes();
        check(lv.lane(0) == 0x3FF8000000000000L, "bits of 1.5 via viewAsIntegralLanes");
        check(lv.viewAsFloatingLanes().equals(d), "viewAs round-trip");
        check(DoubleVector.broadcast(sp, 2.0).fma(DoubleVector.broadcast(sp, 3.0),
                DoubleVector.broadcast(sp, 1.0)).lane(0) == 7.0, "fma");
        check(DoubleVector.broadcast(sp, 16.0).lanewise(VectorOperators.SQRT).lane(0) == 4.0, "SQRT");
        check(DoubleVector.broadcast(sp, Double.NaN).test(VectorOperators.IS_NAN).allTrue(), "IS_NAN");
        check(DoubleVector.broadcast(sp, 1.0).test(VectorOperators.IS_DEFAULT).toString().equals("Mask[..]"), "IS_DEFAULT");
        VectorMask<Double> m = VectorMask.fromValues(sp, true, false);
        check(DoubleVector.broadcast(sp, 5.0).reduceLanes(VectorOperators.ADD, m) == 5.0, "masked reduce ADD");
        check(((IntVector) DoubleVector.broadcast(sp, 2.75).convert(VectorOperators.D2I, 0)).lane(0) == 2, "D2I truncates");
        check(((FloatVector) DoubleVector.broadcast(sp, 2.5).convert(VectorOperators.D2F, 0)).lane(0) == 2.5f, "D2F");
        check(((IntVector) DoubleVector.broadcast(sp, 2.75).convertShape(VectorOperators.D2I, IntVector.SPECIES_128, 0)).lane(0) == 2, "convertShape");
        check(((LongVector) DoubleVector.broadcast(sp, 1.0).convert(VectorOperators.REINTERPRET_D2L, 0)).lane(0) == 0x3FF0000000000000L, "REINTERPRET_D2L bits");
        double[] src = {1.0, 8.0};
        double[] dst = new double[2];
        DoubleVector.fromArray(sp, src, 0, m).intoArray(dst, 0, m);
        check(dst[0] == 1.0 && dst[1] == 0.0, "masked load/store zero pattern");
        check(DoubleVector.broadcast(sp, 1.0).eq(DoubleVector.broadcast(sp, 1.0)).allTrue(), "eq");
        check(d.blend(DoubleVector.broadcast(sp, 9.0), m).lane(0) == 9.0
                && d.blend(DoubleVector.broadcast(sp, 9.0), m).lane(1) == -2.25, "blend");
        check(d.compress(m).lane(0) == 1.5, "compress");
        check(d.toString().equals("[1.5, -2.25]"), "toString exact");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
