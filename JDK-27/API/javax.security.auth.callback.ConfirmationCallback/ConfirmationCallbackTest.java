/*
 * @test
 * @summary javax.security.auth.callback.ConfirmationCallback conformance: confirmation callback options and constants.
 * @run main ConfirmationCallbackTest
 */

import javax.security.auth.callback.*;

public class ConfirmationCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(ConfirmationCallback.YES == 0 && ConfirmationCallback.NO == 1 && ConfirmationCallback.CANCEL == 2 && ConfirmationCallback.OK == 3, "response constants");
        ConfirmationCallback c = new ConfirmationCallback("prompt", ConfirmationCallback.WARNING, ConfirmationCallback.YES_NO_OPTION, ConfirmationCallback.NO);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getMessageType() == ConfirmationCallback.WARNING, "getMessageType");
        check(c.getOptionType() == ConfirmationCallback.YES_NO_OPTION, "getOptionType");
        check(c.getDefaultOption() == ConfirmationCallback.NO, "getDefaultOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
