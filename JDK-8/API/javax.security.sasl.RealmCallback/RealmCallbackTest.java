/*
 * @test
 * @summary javax.security.sasl.RealmCallback conformance: constructor and getters.
 * @run main RealmCallbackTest
 */

public class RealmCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.sasl.RealmCallback c = new javax.security.sasl.RealmCallback("prompt", "default");
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check("default".equals(c.getDefaultText()), "getDefaultText");
        c.setText("realm");
        check("realm".equals(c.getText()), "set/getText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
