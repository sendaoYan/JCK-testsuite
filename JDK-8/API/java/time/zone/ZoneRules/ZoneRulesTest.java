/*
 * @test
 * @summary java.time.zone.ZoneRules API conformance: fixed-offset UTC rules.
 * @run main ZoneRulesTest
 */

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;

public class ZoneRulesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneRules() {
        ZoneRules rules = ZoneId.of("UTC").getRules();
        check(rules.isFixedOffset(), "UTC is fixed offset");
        check(rules.getOffset(Instant.EPOCH).equals(ZoneOffset.UTC), "offset at epoch");
        check(rules.getStandardOffset(Instant.EPOCH).equals(ZoneOffset.UTC),
              "standard offset");
        check(rules.getTransitions().isEmpty(), "no transitions");
    }

    public static void main(String[] args) {
        testZoneRules();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
