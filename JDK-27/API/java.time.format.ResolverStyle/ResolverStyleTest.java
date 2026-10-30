/*
 * @test
 * @summary java.time.format.ResolverStyle API conformance: constants and valueOf.
 * @run main ResolverStyleTest
 */

import java.time.format.ResolverStyle;

public class ResolverStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testResolverStyle() {
        check("STRICT".equals(ResolverStyle.STRICT.name()), "STRICT name");
        check("LENIENT".equals(ResolverStyle.LENIENT.name()), "LENIENT name");
        check(ResolverStyle.valueOf("SMART") == ResolverStyle.SMART, "valueOf SMART");
    }

    public static void main(String[] args) {
        testResolverStyle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
