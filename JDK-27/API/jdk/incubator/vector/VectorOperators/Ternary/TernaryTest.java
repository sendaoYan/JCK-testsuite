/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Ternary conformance: ternary operator interface.
 * @modules jdk.incubator.vector
 * @run main TernaryTest
 */

import java.lang.reflect.*;

public class TernaryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Ternary fma = jdk.incubator.vector.VectorOperators.FMA;
        check(fma.arity() == 3 && fma.operatorName().equals("fma") && !fma.isAssociative(), "FMA metadata");
        check(fma.compatibleWith(float.class) && fma.compatibleWith(double.class), "FMA floating only");
        check(!fma.compatibleWith(int.class), "FMA incompatible int");
        check(jdk.incubator.vector.VectorOperators.BITWISE_BLEND.arity() == 3, "BITWISE_BLEND arity");
        check(jdk.incubator.vector.VectorOperators.BITWISE_BLEND.operatorName().equals("a^((a^b)&c)"), "BITWISE_BLEND expression token");
        check(!jdk.incubator.vector.VectorOperators.FMA.isBoolean(), "ternary not boolean");
        // behavior: FMA on FloatVector
        jdk.incubator.vector.FloatVector a = jdk.incubator.vector.FloatVector.broadcast(
                jdk.incubator.vector.FloatVector.SPECIES_128, 2.0f);
        jdk.incubator.vector.FloatVector b = jdk.incubator.vector.FloatVector.broadcast(
                jdk.incubator.vector.FloatVector.SPECIES_128, 3.0f);
        jdk.incubator.vector.FloatVector c = jdk.incubator.vector.FloatVector.broadcast(
                jdk.incubator.vector.FloatVector.SPECIES_128, 1.0f);
        check(a.lanewise(fma, b, c).lane(0) == 7.0f, "FMA behavior 2*3+1");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Ternary");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
