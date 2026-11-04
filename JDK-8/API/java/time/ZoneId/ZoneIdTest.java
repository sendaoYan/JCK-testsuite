/*
 * @test
 * @summary java.time.ZoneId API conformance: of/getId, systemDefault, equality with
 *          ZoneOffset.UTC, and getRules.
 * @run main ZoneIdTest
 */

import java.time.ZoneId;
import java.time.ZoneOffset;

public class ZoneIdTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneId() {
        ZoneId utc = ZoneId.of("UTC");
        check("UTC".equals(utc.getId()), "of/getId");
        check(ZoneId.systemDefault() != null, "systemDefault non-null");
        check(ZoneId.of("UTC").normalized().equals(ZoneOffset.UTC),
              "ZoneId.of UTC normalizes to ZoneOffset.UTC");
        check(ZoneId.of("Z").equals(ZoneOffset.UTC), "ZoneId.of Z equals ZoneOffset.UTC");
        check(utc.getRules() != null, "getRules non-null");
    }

    public static void main(String[] args) {
        testZoneId();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
