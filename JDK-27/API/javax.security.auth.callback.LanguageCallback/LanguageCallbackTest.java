/*
 * @test
 * @summary javax.security.auth.callback.LanguageCallback conformance: language callback locale.
 * @run main LanguageCallbackTest
 */

import javax.security.auth.callback.*;
import java.util.Locale;

public class LanguageCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        LanguageCallback c = new LanguageCallback();
        c.setLocale(Locale.US);
        check(Locale.US.equals(c.getLocale()), "getLocale");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
