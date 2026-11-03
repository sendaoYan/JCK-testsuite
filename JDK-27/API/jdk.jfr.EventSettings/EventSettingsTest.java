/*
 * @test
 * @summary jdk.jfr.EventSettings conformance: white-box structure + black-box chainable settings.
 * @run main EventSettingsTest
 */

import jdk.jfr.*;
import java.lang.reflect.*;

public class EventSettingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.EventSettings");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "withThreshold"), "withThreshold");
        check(hasMethod(c, "withStackTrace"), "withStackTrace");
        check(hasMethod(c, "withPeriod"), "withPeriod");
        check(hasMethod(c, "with"), "with");
    }

    static void testBlackBox() {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        try (Recording r = new Recording()) {
            EventSettings es = r.enable("jdk.FileRead");
            check(es != null, "enable returns settings");
            check(es.withThreshold(java.time.Duration.ofMillis(5)) != null, "withThreshold");
            check(es.withStackTrace() != null, "withStackTrace");
            check(es.withoutStackTrace() != null, "withoutStackTrace");
            check(es.withoutThreshold() != null, "withoutThreshold");
            check(es.withPeriod(java.time.Duration.ofSeconds(1)) != null, "withPeriod");
            check(es.with("threshold", "10 ms") != null, "with");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
