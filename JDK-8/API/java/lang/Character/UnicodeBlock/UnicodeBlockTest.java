/*
 * @test
 * @summary java.lang.Character.UnicodeBlock API conformance: of.
 * @run main UnicodeBlockTest
 */

import java.lang.Character.UnicodeBlock;

public class UnicodeBlockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnicodeBlock() {
        check(UnicodeBlock.BASIC_LATIN != null, "BASIC_LATIN constant exists");
        check(UnicodeBlock.of('A') == UnicodeBlock.BASIC_LATIN, "of maps 'A' to BASIC_LATIN");
        check(UnicodeBlock.of((char) 0x4E00) == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
              "of maps CJK ideograph");
    }

    public static void main(String[] args) {
        testUnicodeBlock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
