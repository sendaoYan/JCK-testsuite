/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Comparison conformance: comparison operator interface.
 * @modules jdk.incubator.vector
 * @run main ComparisonTest
 */

import java.lang.reflect.*;

public class ComparisonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Comparison lt = jdk.incubator.vector.VectorOperators.LT;
        check(lt.arity() == 2 && lt.isBoolean() && lt.rangeType() == boolean.class && !lt.isAssociative(), "LT metadata");
        check(jdk.incubator.vector.VectorOperators.EQ.operatorName().equals("=="), "EQ token");
        check(jdk.incubator.vector.VectorOperators.NE.operatorName().equals("!="), "NE token");
        check(jdk.incubator.vector.VectorOperators.LE.operatorName().equals("<="), "LE token");
        check(jdk.incubator.vector.VectorOperators.GT.operatorName().equals(">"), "GT token");
        check(jdk.incubator.vector.VectorOperators.GE.operatorName().equals(">="), "GE token");
        check(jdk.incubator.vector.VectorOperators.ULT.operatorName().equals("<"), "ULT token");
        check(!jdk.incubator.vector.VectorOperators.ULT.compatibleWith(double.class), "ULT integral only");
        // behavior: signed vs unsigned compare
        jdk.incubator.vector.IntVector u = jdk.incubator.vector.IntVector.fromArray(
                jdk.incubator.vector.IntVector.SPECIES_128, new int[] {-1, 2, 3, 4}, 0);
        jdk.incubator.vector.IntVector two = jdk.incubator.vector.IntVector.broadcast(
                jdk.incubator.vector.IntVector.SPECIES_128, 2);
        check(u.compare(lt, two).laneIsSet(0), "LT signed -1 < 2");
        check(!u.compare(jdk.incubator.vector.VectorOperators.ULT, two).laneIsSet(0), "ULT unsigned -1 not < 2");
        check(u.compare(jdk.incubator.vector.VectorOperators.UGT, two).laneIsSet(0), "UGT unsigned -1 > 2");
        check(u.compare(jdk.incubator.vector.VectorOperators.EQ, u).allTrue(), "EQ self allTrue");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Comparison");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
