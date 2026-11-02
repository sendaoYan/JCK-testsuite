/*
 * @test
 * @summary jdk.jfr.EventSettings conformance: obtained from Recording.enable and chainable.
 * @run main EventSettingsTest
 */

import jdk.jfr.*;

public class EventSettingsTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        if (!FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        try (Recording r = new Recording()) {
            EventSettings es = r.enable("jdk.FileRead");
            check(es != null, "enable returns settings");
            EventSettings es2 = es.withThreshold(java.time.Duration.ofMillis(5));
            check(es2 != null, "withThreshold");
            check(es.withStackTrace() != null, "withStackTrace");
            check(es.withoutStackTrace() != null, "withoutStackTrace");
            check(es.withoutThreshold() != null, "withoutThreshold");
            check(es.withPeriod(java.time.Duration.ofSeconds(1)) != null, "withPeriod");
            check(es.with("threshold", "10 ms") != null, "with");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
