/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Unary conformance: unary operator interface.
 * @modules jdk.incubator.vector
 * @run main UnaryTest
 */

import java.lang.reflect.*;

public class UnaryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Unary not = jdk.incubator.vector.VectorOperators.NOT;
        check(not.arity() == 1 && not.name().equals("NOT") && not.operatorName().equals("~"), "NOT metadata");
        check(!not.compatibleWith(double.class) && not.compatibleWith(int.class), "NOT integral only");
        check(jdk.incubator.vector.VectorOperators.NEG.operatorName().equals("-a"), "NEG token -a");
        check(jdk.incubator.vector.VectorOperators.ABS.operatorName().equals("abs"), "ABS token abs");
        check(jdk.incubator.vector.VectorOperators.SQRT.operatorName().equals("sqrt")
                && jdk.incubator.vector.VectorOperators.SQRT.compatibleWith(float.class), "SQRT token");
        check(jdk.incubator.vector.VectorOperators.ZOMO.operatorName().equals("a==0?0:-1"), "ZOMO expression token");
        check(jdk.incubator.vector.VectorOperators.BIT_COUNT.operatorName().equals("bitCount"), "BIT_COUNT token");
        check(jdk.incubator.vector.VectorOperators.TRAILING_ZEROS_COUNT.operatorName().equals("numberOfTrailingZeros"), "TZ token");
        check(jdk.incubator.vector.VectorOperators.LEADING_ZEROS_COUNT.operatorName().equals("numberOfLeadingZeros"), "LZ token");
        check(jdk.incubator.vector.VectorOperators.REVERSE_BYTES.operatorName().equals("reverseBytes"), "REVERSE_BYTES token");
        check(!jdk.incubator.vector.VectorOperators.NOT.isBoolean(), "unary not boolean");
        // behavior: ZOMO on IntVector
        jdk.incubator.vector.IntVector v = jdk.incubator.vector.IntVector.fromArray(
                jdk.incubator.vector.IntVector.SPECIES_128, new int[] {0, 3, -1, 0}, 0);
        jdk.incubator.vector.IntVector z = v.lanewise(jdk.incubator.vector.VectorOperators.ZOMO);
        check(z.lane(0) == 0 && z.lane(1) == -1 && z.lane(3) == 0, "ZOMO zero-or-minus-one behavior");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Unary");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
