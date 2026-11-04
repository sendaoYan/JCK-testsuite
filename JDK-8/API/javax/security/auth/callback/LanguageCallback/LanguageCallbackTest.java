/*
 * @test
 * @summary javax.security.auth.callback.LanguageCallback conformance: setLocale/getLocale.
 * @run main LanguageCallbackTest
 */

public class LanguageCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.LanguageCallback c = new javax.security.auth.callback.LanguageCallback();
        c.setLocale(java.util.Locale.US);
        check(java.util.Locale.US.equals(c.getLocale()), "set/getLocale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
