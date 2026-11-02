/*
 * @test
 * @summary javax.security.auth.callback.NameCallback conformance: name callback prompt and name.
 * @run main NameCallbackTest
 */

import javax.security.auth.callback.*;

public class NameCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        NameCallback c = new NameCallback("prompt");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getDefaultName() == null, "default name null");
        c.setName("alice");
        check("alice".equals(c.getName()), "getName");
        check("bob".equals(new NameCallback("p", "bob").getDefaultName()), "default name ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
