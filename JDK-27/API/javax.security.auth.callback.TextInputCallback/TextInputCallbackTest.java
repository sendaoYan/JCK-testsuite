/*
 * @test
 * @summary javax.security.auth.callback.TextInputCallback conformance: text input callback text.
 * @run main TextInputCallbackTest
 */

import javax.security.auth.callback.*;

public class TextInputCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        TextInputCallback c = new TextInputCallback("prompt");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getDefaultText() == null, "default text null");
        c.setText("value");
        check("value".equals(c.getText()), "getText");
        check("d".equals(new TextInputCallback("p", "d").getDefaultText()), "default text ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
