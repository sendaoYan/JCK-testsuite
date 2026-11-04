/*
 * @test
 * @summary javax.crypto.ExemptionMechanism conformance: getInstance throws for unknown algorithm.
 * @run main ExemptionMechanismTest
 */

public class ExemptionMechanismTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        boolean threw = false;
        try { javax.crypto.ExemptionMechanism.getInstance("NoSuchExemptionMechanism"); }
        catch (java.security.NoSuchAlgorithmException expected) { threw = true; }
        check(threw, "getInstance(bogus) throws NoSuchAlgorithmException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
