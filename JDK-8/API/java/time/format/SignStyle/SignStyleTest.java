/*
 * @test
 * @summary java.time.format.SignStyle API conformance: constants and valueOf.
 * @run main SignStyleTest
 */

import java.time.format.SignStyle;

public class SignStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSignStyle() {
        check("NORMAL".equals(SignStyle.NORMAL.name()), "NORMAL name");
        check(SignStyle.valueOf("ALWAYS") == SignStyle.ALWAYS, "valueOf ALWAYS");
        check(SignStyle.valueOf("EXCEEDS_PAD") == SignStyle.EXCEEDS_PAD,
              "valueOf EXCEEDS_PAD");
    }

    public static void main(String[] args) {
        testSignStyle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
