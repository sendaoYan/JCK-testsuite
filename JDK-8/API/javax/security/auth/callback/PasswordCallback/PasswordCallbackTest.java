/*
 * @test
 * @summary javax.security.auth.callback.PasswordCallback conformance: constructor and getters.
 * @run main PasswordCallbackTest
 */

public class PasswordCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.PasswordCallback c = new javax.security.auth.callback.PasswordCallback("prompt", false);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(!c.isEchoOn(), "isEchoOn false");
        c.setPassword("secret".toCharArray());
        check(java.util.Arrays.equals("secret".toCharArray(), c.getPassword()), "set/getPassword");
        c.clearPassword();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
