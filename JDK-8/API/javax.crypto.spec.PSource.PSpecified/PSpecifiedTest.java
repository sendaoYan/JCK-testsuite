/*
 * @test
 * @summary javax.crypto.spec.PSource.PSpecified conformance: constructor and getters.
 * @run main PSpecifiedTest
 */

public class PSpecifiedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.spec.PSource.PSpecified p = new javax.crypto.spec.PSource.PSpecified(new byte[]{1,2,3});
        check("PSpecified".equals(p.getAlgorithm()), "getAlgorithm");
        check(java.util.Arrays.equals(new byte[]{1,2,3}, p.getValue()), "getValue");
        check(javax.crypto.spec.PSource.PSpecified.DEFAULT.getValue().length == 0, "DEFAULT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
