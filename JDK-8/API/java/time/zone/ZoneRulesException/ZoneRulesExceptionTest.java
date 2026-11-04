/*
 * @test
 * @summary java.time.zone.ZoneRulesException conformance: message and hierarchy.
 * @run main ZoneRulesExceptionTest
 */

import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;

public class ZoneRulesExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneRulesException() {
        ZoneRulesException e = new ZoneRulesException("msg");
        check("msg".equals(e.getMessage()), "getMessage");
        check(e instanceof DateTimeException, "is DateTimeException");
    }

    public static void main(String[] args) {
        testZoneRulesException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
