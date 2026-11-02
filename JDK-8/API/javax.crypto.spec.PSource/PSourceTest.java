/*
 * @test
 * @summary javax.crypto.spec.PSource conformance: PSpecified getAlgorithm/getValue.
 * @run main PSourceTest
 */

public class PSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.spec.PSource.PSpecified p = new javax.crypto.spec.PSource.PSpecified(new byte[]{1,2,3});
        check("PSpecified".equals(p.getAlgorithm()), "getAlgorithm");
        check(java.util.Arrays.equals(new byte[]{1,2,3}, p.getValue()), "getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
