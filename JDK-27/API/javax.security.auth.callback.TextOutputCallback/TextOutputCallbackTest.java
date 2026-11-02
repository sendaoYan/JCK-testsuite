/*
 * @test
 * @summary javax.security.auth.callback.TextOutputCallback conformance: text output callback type and message.
 * @run main TextOutputCallbackTest
 */

import javax.security.auth.callback.*;

public class TextOutputCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        TextOutputCallback c = new TextOutputCallback(TextOutputCallback.INFORMATION, "msg");
        check(c.getMessageType() == TextOutputCallback.INFORMATION, "getMessageType");
        check("msg".equals(c.getMessage()), "getMessage");
        check(TextOutputCallback.WARNING == 1 && TextOutputCallback.ERROR == 2, "type constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
