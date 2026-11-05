/*
 * @test
 * @summary jdk.incubator.vector.FloatVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main FloatVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class FloatVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.FloatVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(FloatVector.SPECIES_128 != null && FloatVector.SPECIES_256 != null, "species constants");
        check(FloatVector.SPECIES_128.elementType() == float.class, "element type float");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "mul"), "mul");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Float> sp = FloatVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        FloatVector a = FloatVector.broadcast(sp, 2.5f);
        FloatVector b = FloatVector.broadcast(sp, 1.5f);
        check(a.lane(0) == 2.5f, "broadcast lane");
        FloatVector sum = a.add(b);
        check(sum.lane(0) == 4.0f && sum.lane(n - 1) == 4.0f, "add lanes");
        FloatVector prod = a.mul(b);
        check(Math.abs(prod.lane(0) - 3.75f) < 1e-6f, "mul lane");
        float[] arr = new float[n];
        sum.intoArray(arr, 0);
        check(arr[0] == 4.0f && arr[n - 1] == 4.0f, "intoArray round-trip");
        FloatVector back = FloatVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        float reduced = a.reduceLanes(VectorOperators.ADD);
        check(Math.abs(reduced - 2.5f * n) < 1e-3f, "reduceLanes ADD");
    }


    static void testDeepen() {
        VectorSpecies<Float> sp = FloatVector.SPECIES_128;
        check(sp.length() == 4, "SPECIES_128 float length 4");
        FloatVector a = FloatVector.broadcast(sp, 2.0f), b = FloatVector.broadcast(sp, 3.0f), c = FloatVector.broadcast(sp, 1.0f);
        check(a.fma(b, c).lane(0) == 7.0f, "fma");
        check(a.lanewise(VectorOperators.FMA, b, c).lane(0) == 7.0f, "FMA lanewise");
        check(FloatVector.broadcast(sp, 9.0f).lanewise(VectorOperators.SQRT).lane(0) == 3.0f, "SQRT");
        check(FloatVector.broadcast(sp, Float.NaN).test(VectorOperators.IS_NAN).toString().equals("Mask[TTTT]"), "IS_NAN");
        check(FloatVector.broadcast(sp, Float.POSITIVE_INFINITY).test(VectorOperators.IS_FINITE).toString().equals("Mask[....]"), "IS_FINITE inf");
        check(FloatVector.broadcast(sp, Float.POSITIVE_INFINITY).test(VectorOperators.IS_INFINITE).allTrue(), "IS_INFINITE");
        check(FloatVector.zero(sp).test(VectorOperators.IS_DEFAULT).allTrue(), "IS_DEFAULT");
        check(FloatVector.broadcast(sp, -1.0f).test(VectorOperators.IS_NEGATIVE).allTrue(), "IS_NEGATIVE");
        check(FloatVector.broadcast(sp, 6.0f).div(FloatVector.broadcast(sp, 2.0f)).lane(0) == 3.0f, "div");
        check(FloatVector.broadcast(sp, -2.0f).abs().lane(0) == 2.0f, "abs");
        check(FloatVector.broadcast(sp, 2.0f).neg().lane(0) == -2.0f, "neg");
        check(FloatVector.broadcast(sp, Float.NaN).eq(FloatVector.broadcast(sp, Float.NaN)).toString().equals("Mask[....]"), "NaN eq false");
        check(FloatVector.broadcast(sp, 2.0f).compare(VectorOperators.NE, FloatVector.broadcast(sp, 3.0f)).allTrue(), "NE");
        VectorMask<Float> m = VectorMask.fromValues(sp, true, false, true, false);
        check(FloatVector.broadcast(sp, 6.0f).reduceLanes(VectorOperators.ADD, m) == 12.0f, "masked reduce ADD");
        check(((IntVector) FloatVector.broadcast(sp, 2.75f).convert(VectorOperators.F2I, 0)).lane(0) == 2, "F2I truncates");
        check(((DoubleVector) FloatVector.broadcast(sp, 2.5f).convert(VectorOperators.F2D, 0)).lane(0) == 2.5, "F2D");
        check(((IntVector) FloatVector.broadcast(sp, 1.0f).convert(VectorOperators.REINTERPRET_F2I, 0)).lane(0) == 0x3F800000, "REINTERPRET_F2I bits");
        check(FloatVector.broadcast(sp, 1.0f).viewAsIntegralLanes().elementType() == int.class, "viewAsIntegralLanes int");
        check(((IntVector) FloatVector.broadcast(sp, 1.0f).viewAsIntegralLanes()).viewAsFloatingLanes()
                .equals(FloatVector.broadcast(sp, 1.0f)), "viewAs round-trip");
        check(FloatVector.broadcast(sp, 1.0f).blend(FloatVector.broadcast(sp, 9.0f), m).lane(0) == 9.0f
                && FloatVector.broadcast(sp, 1.0f).blend(FloatVector.broadcast(sp, 9.0f), m).lane(1) == 1.0f, "blend");
        float[] src = {10.0f, 20.0f, 30.0f, 40.0f};
        float[] dst = new float[4];
        FloatVector.fromArray(sp, src, 0, m).intoArray(dst, 0, m);
        check(dst[0] == 10.0f && dst[1] == 0.0f && dst[2] == 30.0f && dst[3] == 0.0f, "masked load/store zero pattern");
        check(FloatVector.fromArray(sp, src, 0).toString().equals("[10.0, 20.0, 30.0, 40.0]"), "toString exact");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
