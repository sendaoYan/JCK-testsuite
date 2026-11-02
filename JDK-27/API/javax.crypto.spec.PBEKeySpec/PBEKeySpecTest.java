/*
 * @test
 * @summary javax.crypto.spec.PBEKeySpec conformance: PBE key spec password salt and iteration.
 * @run main PBEKeySpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class PBEKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        char[] pw = "secret".toCharArray();
        byte[] salt = new byte[]{1, 2, 3};
        PBEKeySpec s = new PBEKeySpec(pw, salt, 1000, 128);
        check(Arrays.equals(pw, s.getPassword()), "getPassword");
        check(Arrays.equals(salt, s.getSalt()), "getSalt");
        check(s.getIterationCount() == 1000, "getIterationCount");
        check(s.getKeyLength() == 128, "getKeyLength");
        s.clearPassword();
        boolean ise = false;
        try { s.getPassword(); } catch (IllegalStateException e) { ise = true; }
        check(ise, "clearPassword makes getPassword throw IllegalStateException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
