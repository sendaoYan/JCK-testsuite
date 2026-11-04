/*
 * @test
 * @summary javax.security.auth.callback.NameCallback conformance: constructor and getters.
 * @run main NameCallbackTest
 */

public class NameCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.NameCallback c = new javax.security.auth.callback.NameCallback("prompt", "default");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check("default".equals(c.getDefaultName()), "getDefaultName");
        c.setName("value");
        check("value".equals(c.getName()), "set/getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
