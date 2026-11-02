/*
 * @test
 * @summary jdk.incubator.vector.VectorOperators conformance: operator constants.
 * @modules jdk.incubator.vector
 * @run main VectorOperatorsTest
 */

import jdk.incubator.vector.*;

public class VectorOperatorsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(VectorOperators.ADD != null, "ADD");
        check(VectorOperators.NEG != null, "NEG");
        check(VectorOperators.LT != null, "LT");
        check(VectorOperators.NOT != null, "NOT");
        check(VectorOperators.SQRT != null, "SQRT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
