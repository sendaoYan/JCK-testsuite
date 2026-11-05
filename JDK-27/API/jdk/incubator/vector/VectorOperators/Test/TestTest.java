/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Test conformance: test operator interface.
 * @modules jdk.incubator.vector
 * @run main TestTest
 */

import java.lang.reflect.*;

public class TestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Test nan = jdk.incubator.vector.VectorOperators.IS_NAN;
        check(nan.arity() == 1 && nan.isBoolean() && nan.rangeType() == boolean.class, "IS_NAN boolean unary");
        check(nan.operatorName().equals("isNaN"), "IS_NAN token");
        check(nan.compatibleWith(float.class) && nan.compatibleWith(double.class), "IS_NAN floating only");
        check(!nan.compatibleWith(int.class), "IS_NAN incompatible int");
        check(jdk.incubator.vector.VectorOperators.IS_DEFAULT.operatorName().equals("bits(a)==0"), "IS_DEFAULT token");
        check(jdk.incubator.vector.VectorOperators.IS_NEGATIVE.operatorName().equals("bits(a)<0"), "IS_NEGATIVE token");
        check(jdk.incubator.vector.VectorOperators.IS_FINITE != null && jdk.incubator.vector.VectorOperators.IS_INFINITE != null, "IS_FINITE/IS_INFINITE");
        check(jdk.incubator.vector.VectorOperators.IS_DEFAULT.compatibleWith(int.class), "IS_DEFAULT works on ints");
        // behavior: tests on FloatVector
        jdk.incubator.vector.FloatVector v = jdk.incubator.vector.FloatVector.broadcast(
                jdk.incubator.vector.FloatVector.SPECIES_128, Float.NaN);
        check(v.test(nan).toString().equals("Mask[TTTT]"), "IS_NAN behavior");
        check(jdk.incubator.vector.FloatVector.broadcast(jdk.incubator.vector.FloatVector.SPECIES_128, 1.0f)
                .test(jdk.incubator.vector.VectorOperators.IS_FINITE).allTrue(), "IS_FINITE behavior");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Test");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
