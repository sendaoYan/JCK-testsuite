/*
 * @test
 * @summary javax.security.auth.callback.TextOutputCallback conformance: constructor and getters.
 * @run main TextOutputCallbackTest
 */

public class TextOutputCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.TextOutputCallback c = new javax.security.auth.callback.TextOutputCallback(
            javax.security.auth.callback.TextOutputCallback.INFORMATION, "message");
        check(c.getMessageType() == javax.security.auth.callback.TextOutputCallback.INFORMATION, "getMessageType");
        check("message".equals(c.getMessage()), "getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
