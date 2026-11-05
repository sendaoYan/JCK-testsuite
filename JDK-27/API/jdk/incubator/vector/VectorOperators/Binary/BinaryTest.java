/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators.Binary conformance: binary operator interface.
 * @modules jdk.incubator.vector
 * @run main BinaryTest
 */

import java.lang.reflect.*;

public class BinaryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() {
        jdk.incubator.vector.VectorOperators.Binary sub = jdk.incubator.vector.VectorOperators.SUB;
        check(sub.arity() == 2 && sub.operatorName().equals("-") && !sub.isAssociative(), "SUB metadata");
        check(jdk.incubator.vector.VectorOperators.AND.operatorName().equals("&"), "AND token");
        check(jdk.incubator.vector.VectorOperators.OR.operatorName().equals("|"), "OR token");
        check(jdk.incubator.vector.VectorOperators.XOR.operatorName().equals("^"), "XOR token");
        check(jdk.incubator.vector.VectorOperators.AND_NOT.operatorName().equals("&~"), "AND_NOT token");
        check(jdk.incubator.vector.VectorOperators.LSHL.operatorName().equals("<<"), "LSHL token");
        check(jdk.incubator.vector.VectorOperators.ASHR.operatorName().equals(">>"), "ASHR token");
        check(jdk.incubator.vector.VectorOperators.LSHR.operatorName().equals(">>>"), "LSHR token");
        check(jdk.incubator.vector.VectorOperators.ROL.operatorName().equals("rotateLeft"), "ROL token");
        check(jdk.incubator.vector.VectorOperators.ROR.operatorName().equals("rotateRight"), "ROR token");
        check(jdk.incubator.vector.VectorOperators.SADD.operatorName().equals("+")
                && !jdk.incubator.vector.VectorOperators.SADD.compatibleWith(double.class), "SADD saturating integral");
        check(jdk.incubator.vector.VectorOperators.COMPRESS_BITS.operatorName().equals("compressBits"), "COMPRESS_BITS token");
        check(jdk.incubator.vector.VectorOperators.EXPAND_BITS.operatorName().equals("expandBits"), "EXPAND_BITS token");
        check(!jdk.incubator.vector.VectorOperators.SUB.isBoolean(), "binary not boolean");
        // behavior: AND_NOT and rotate on IntVector
        jdk.incubator.vector.IntVector f0 = jdk.incubator.vector.IntVector.broadcast(
                jdk.incubator.vector.IntVector.SPECIES_128, 0xF0);
        jdk.incubator.vector.IntVector ff = jdk.incubator.vector.IntVector.broadcast(
                jdk.incubator.vector.IntVector.SPECIES_128, 0xFF);
        check(f0.lanewise(jdk.incubator.vector.VectorOperators.AND_NOT, ff).lane(0) == 0, "AND_NOT behavior");
        check(jdk.incubator.vector.IntVector.broadcast(jdk.incubator.vector.IntVector.SPECIES_128, 1)
                .lanewise(jdk.incubator.vector.VectorOperators.ROL, 4).lane(0) == 16, "ROL behavior");
    }

    public static void main(String[] a) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.incubator.vector.VectorOperators$Binary");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(jdk.incubator.vector.VectorOperators.Operator.class.isAssignableFrom(c), "extends Operator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
