/*
 * @test
 * @summary javax.security.auth.callback.TextInputCallback conformance: constructor and getters.
 * @run main TextInputCallbackTest
 */

public class TextInputCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.TextInputCallback c = new javax.security.auth.callback.TextInputCallback("prompt", "default");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check("default".equals(c.getDefaultText()), "getDefaultText");
        c.setText("value");
        check("value".equals(c.getText()), "set/getText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
