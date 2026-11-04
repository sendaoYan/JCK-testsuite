/*
 * @test
 * @summary javax.security.auth.callback.ConfirmationCallback conformance: constructor and getters.
 * @run main ConfirmationCallbackTest
 */

public class ConfirmationCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.ConfirmationCallback c = new javax.security.auth.callback.ConfirmationCallback(
            "prompt", javax.security.auth.callback.ConfirmationCallback.YES_NO_OPTION,
            javax.security.auth.callback.ConfirmationCallback.YES, javax.security.auth.callback.ConfirmationCallback.NO);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getOptionType() == javax.security.auth.callback.ConfirmationCallback.YES_NO_OPTION, "getOptionType");
        check(c.getDefaultOption() == javax.security.auth.callback.ConfirmationCallback.NO, "getDefaultOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
