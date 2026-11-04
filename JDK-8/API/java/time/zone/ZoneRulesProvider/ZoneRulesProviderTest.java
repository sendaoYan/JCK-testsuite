/*
 * @test
 * @summary java.time.zone.ZoneRulesProvider API conformance: getRules and available zone ids.
 * @run main ZoneRulesProviderTest
 */

import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesProvider;

public class ZoneRulesProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneRulesProvider() {
        ZoneRules rules = ZoneRulesProvider.getRules("UTC", false);
        check(rules.isFixedOffset(), "UTC fixed offset");
        check(ZoneRulesProvider.getAvailableZoneIds().contains("UTC"),
              "available ids contain UTC");
    }

    public static void main(String[] args) {
        testZoneRulesProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
