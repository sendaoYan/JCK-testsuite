/*
 * @test
 * @summary javax.security.sasl.RealmCallback conformance: SASL realm callback.
 * @run main RealmCallbackTest
 */

import javax.security.sasl.*;

public class RealmCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        RealmCallback c = new RealmCallback("prompt");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        c.setText("REALM");
        check("REALM".equals(c.getText()), "getText");
        check("d".equals(new RealmCallback("p", "d").getDefaultText()), "default text ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
