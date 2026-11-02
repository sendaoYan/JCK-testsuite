/*
 * @test
 * @summary jdk.incubator.vector.VectorShape conformance: shape enum constants.
 * @modules jdk.incubator.vector
 * @run main VectorShapeTest
 */

import jdk.incubator.vector.*;

public class VectorShapeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(VectorShape.S_64_BIT.vectorBitSize() == 64, "64 bit");
        check(VectorShape.S_128_BIT.vectorBitSize() == 128, "128 bit");
        check(VectorShape.forBitSize(128) == VectorShape.S_128_BIT, "forBitSize");
        check(VectorShape.preferredShape() != null, "preferredShape");
        check(VectorShape.values().length >= 5, "values");
        check(VectorShape.valueOf("S_64_BIT") == VectorShape.S_64_BIT, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
