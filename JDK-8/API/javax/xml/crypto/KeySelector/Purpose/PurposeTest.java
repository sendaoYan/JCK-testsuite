/*
 * @test
 * @summary javax.xml.crypto.KeySelector.Purpose conformance: key selector purpose constants.
 * @run main PurposeTest
 */


public class PurposeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.crypto.KeySelector.Purpose.SIGN != null, "SIGN");
        check(javax.xml.crypto.KeySelector.Purpose.VERIFY != null, "VERIFY");
        check(javax.xml.crypto.KeySelector.Purpose.ENCRYPT != null, "ENCRYPT");
        check(javax.xml.crypto.KeySelector.Purpose.DECRYPT != null, "DECRYPT");
        check(javax.xml.crypto.KeySelector.Purpose.SIGN.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
