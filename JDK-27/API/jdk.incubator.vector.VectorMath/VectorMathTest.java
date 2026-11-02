/*
 * @test
 * @summary jdk.incubator.vector.VectorMath conformance: saturating and unsigned math.
 * @modules jdk.incubator.vector
 * @run main VectorMathTest
 */

import jdk.incubator.vector.*;

public class VectorMathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(VectorMath.addSaturating(Integer.MAX_VALUE, 1) == Integer.MAX_VALUE, "addSaturating int");
        check(VectorMath.subSaturating(Integer.MIN_VALUE, 1) == Integer.MIN_VALUE, "subSaturating int");
        check(VectorMath.minUnsigned(0, -1) == 0, "minUnsigned int");
        check(VectorMath.maxUnsigned(0L, -1L) == -1L, "maxUnsigned long");
        check(VectorMath.addSaturating((byte)120, (byte)20) == (byte)127, "addSaturating byte");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
