/*
 * @test
 * @summary jdk.incubator.vector.VectorMask conformance: mask operations.
 * @modules jdk.incubator.vector
 * @run main VectorMaskTest
 */

import jdk.incubator.vector.*;

public class VectorMaskTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        VectorMask<Integer> m = IntVector.SPECIES_PREFERRED.maskAll(true);
        check(m.length() > 0, "length");
        check(m.allTrue(), "allTrue");
        check(m.trueCount() == m.length(), "trueCount");
        check(m.toVector().length() > 0, "toVector");
        check(!m.not().anyTrue(), "not anyTrue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
