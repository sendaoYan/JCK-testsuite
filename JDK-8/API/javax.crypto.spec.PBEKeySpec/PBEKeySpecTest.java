/*
 * @test
 * @summary javax.crypto.spec.PBEKeySpec conformance: constructor and getters.
 * @run main PBEKeySpecTest
 */

public class PBEKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.spec.PBEKeySpec s = new javax.crypto.spec.PBEKeySpec("pw".toCharArray(), new byte[]{1,2,3,4}, 100);
        check(java.util.Arrays.equals("pw".toCharArray(), s.getPassword()), "getPassword");
        check(java.util.Arrays.equals(new byte[]{1,2,3,4}, s.getSalt()), "getSalt");
        check(s.getIterationCount() == 100, "getIterationCount");
        check(s.getKeyLength() == 0, "getKeyLength");
        s.clearPassword();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
