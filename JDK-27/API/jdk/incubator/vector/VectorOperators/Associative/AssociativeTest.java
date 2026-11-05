/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Associative conformance: associative operator interface.
 * @modules jdk.incubator.vector
 * @run main AssociativeTest
 */

import java.lang.reflect.*;

public class AssociativeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Associative add = jdk.incubator.vector.VectorOperators.ADD;
        check(add.isAssociative() && add.arity() == 2, "ADD associative arity 2");
        check(jdk.incubator.vector.VectorOperators.MUL.isAssociative(), "MUL");
        check(jdk.incubator.vector.VectorOperators.MIN.isAssociative()
                && jdk.incubator.vector.VectorOperators.MAX.isAssociative(), "MIN/MAX");
        check(jdk.incubator.vector.VectorOperators.AND.isAssociative()
                && jdk.incubator.vector.VectorOperators.OR.isAssociative()
                && jdk.incubator.vector.VectorOperators.XOR.isAssociative(), "bitwise associatives");
        check(jdk.incubator.vector.VectorOperators.FIRST_NONZERO.isAssociative(), "FIRST_NONZERO");
        check(jdk.incubator.vector.VectorOperators.SUADD.isAssociative(), "SUADD associative");
        check(jdk.incubator.vector.VectorOperators.UMIN.isAssociative()
                && jdk.incubator.vector.VectorOperators.UMAX.isAssociative(), "UMIN/UMAX");
        check(!jdk.incubator.vector.VectorOperators.SUB.isAssociative(), "SUB not associative");
        check(!jdk.incubator.vector.VectorOperators.DIV.isAssociative(), "DIV not associative");
        // behavior: reductions agree with scalars
        jdk.incubator.vector.IntVector v = jdk.incubator.vector.IntVector.fromArray(
                jdk.incubator.vector.IntVector.SPECIES_128, new int[] {0, 0, 5, 0}, 0);
        check(v.reduceLanes(jdk.incubator.vector.VectorOperators.FIRST_NONZERO) == 5, "FIRST_NONZERO reduction");
        check(v.reduceLanes(add) == 5, "ADD reduction");
        check(v.reduceLanes(jdk.incubator.vector.VectorOperators.MIN) == 0
                && v.reduceLanes(jdk.incubator.vector.VectorOperators.MAX) == 5, "MIN/MAX reduction");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Associative");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Binary.class.isAssignableFrom(c), "extends Binary");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
