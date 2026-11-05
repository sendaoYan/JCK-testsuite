/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Operator conformance: operator interface.
 * @modules jdk.incubator.vector
 * @run main OperatorTest
 */

import java.lang.reflect.*;

public class OperatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Operator add = jdk.incubator.vector.VectorOperators.ADD;
        check(add.arity() == 2 && add.rangeType() == Object.class, "ADD arity 2, range Object");
        check(!add.isBoolean() && add.isAssociative(), "ADD not boolean, associative");
        check(add.compatibleWith(int.class) && add.compatibleWith(double.class), "ADD compatible all");
        jdk.incubator.vector.VectorOperators.Operator lt = jdk.incubator.vector.VectorOperators.LT;
        check(lt.isBoolean() && lt.rangeType() == boolean.class && !lt.isAssociative(), "LT boolean semantics");
        jdk.incubator.vector.VectorOperators.Operator neg = jdk.incubator.vector.VectorOperators.NEG;
        check(neg.arity() == 1 && neg.rangeType() == Object.class, "NEG arity 1");
        jdk.incubator.vector.VectorOperators.Operator fma = jdk.incubator.vector.VectorOperators.FMA;
        check(fma.arity() == 3, "FMA arity 3");
        check(jdk.incubator.vector.VectorOperators.SQRT.compatibleWith(int.class)
                == false, "SQRT incompatible with int");
        check(jdk.incubator.vector.VectorOperators.SQRT.compatibleWith(float.class), "SQRT compatible float");
        check(jdk.incubator.vector.VectorOperators.AND.compatibleWith(double.class) == false, "AND incompatible double");
        check(jdk.incubator.vector.VectorOperators.AND.compatibleWith(long.class), "AND compatible long");
        check(jdk.incubator.vector.VectorOperators.I2D.rangeType() == double.class, "conversion rangeType override");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Operator");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
