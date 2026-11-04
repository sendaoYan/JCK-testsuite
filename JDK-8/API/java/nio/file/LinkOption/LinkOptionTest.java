/*
 * @test
 * @summary java.nio.file.LinkOption conformance: enum constants.
 * @run main LinkOptionTest
 */

import java.nio.file.LinkOption;

public class LinkOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLinkOption() {
        check(LinkOption.valueOf("NOFOLLOW_LINKS") == LinkOption.NOFOLLOW_LINKS, "valueOf NOFOLLOW_LINKS");
        check(LinkOption.values().length >= 1, "at least 1 values");
        check(LinkOption.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testLinkOption();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
