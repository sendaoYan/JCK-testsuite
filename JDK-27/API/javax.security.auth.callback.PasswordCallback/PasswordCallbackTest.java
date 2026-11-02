/*
 * @test
 * @summary javax.security.auth.callback.PasswordCallback conformance: password callback echo and clear.
 * @run main PasswordCallbackTest
 */

import javax.security.auth.callback.*;
import java.util.Arrays;

public class PasswordCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PasswordCallback c = new PasswordCallback("prompt", false);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(!c.isEchoOn(), "isEchoOn false");
        char[] pw = "secret".toCharArray();
        c.setPassword(pw);
        check(Arrays.equals(pw, c.getPassword()), "getPassword");
        c.clearPassword();
        check(!Arrays.equals(pw, c.getPassword()), "clearPassword clears password");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
