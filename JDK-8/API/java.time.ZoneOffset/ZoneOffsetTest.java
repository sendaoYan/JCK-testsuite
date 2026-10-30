/*
 * @test
 * @summary java.time.ZoneOffset API conformance: ofHours/getTotalSeconds, UTC id, and
 *          of(String).
 * @run main ZoneOffsetTest
 */

import java.time.ZoneOffset;

public class ZoneOffsetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneOffset() {
        check(ZoneOffset.ofHours(5).getTotalSeconds() == 5 * 3600, "ofHours/getTotalSeconds");
        check("Z".equals(ZoneOffset.UTC.getId()), "UTC id");
        check(ZoneOffset.of("+05:00").getTotalSeconds() == 18000, "of(String)");
    }

    public static void main(String[] args) {
        testZoneOffset();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
