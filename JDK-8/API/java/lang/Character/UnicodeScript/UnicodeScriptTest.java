/*
 * @test
 * @summary java.lang.Character.UnicodeScript API conformance: of.
 * @run main UnicodeScriptTest
 */

import java.lang.Character.UnicodeScript;

public class UnicodeScriptTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnicodeScript() {
        check(UnicodeScript.LATIN != null, "LATIN constant exists");
        check(UnicodeScript.of('A') == UnicodeScript.LATIN, "of maps 'A' to LATIN");
        check(UnicodeScript.of(0x4E00) == UnicodeScript.HAN, "of maps CJK ideograph to HAN");
    }

    public static void main(String[] args) {
        testUnicodeScript();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
