/*
 * @test
 * @summary jdk.jfr.EventSettings conformance: white-box structure + black-box chainable settings.
 * @run main EventSettingsTest
 */

import jdk.jfr.*;
import java.lang.reflect.*;
import java.time.Duration;
import java.util.*;

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
        testDeepValueFormats();
        testDeepChaining();
        testDeepNulls();
        testDeepCustomSettingPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static class MyControl extends SettingControl {
        private String v = "none";
        public String combine(Set<String> s) { return String.join("|", s); }
        public void setValue(String s) { v = s; }
        public String getValue() { return v; }
    }
    @Name("test.EventSettings.SE")
    static class SEEvent extends Event { long x; }
    @Name("test.EventSettings.CS")
    static class CSEvent extends Event { @SettingDefinition boolean flavor(MyControl c) { return true; } }

    static void testDeepValueFormats() throws Exception {
        try (Recording r = new Recording()) {
            r.enable("test.EventSettings.SE").withThreshold(Duration.ofMillis(3));
            check("3000000 ns".equals(r.getSettings().get("test.EventSettings.SE#threshold")), "es withThreshold(3ms) -> 3000000 ns");
            r.enable("test.EventSettings.SE").withoutThreshold();
            check("0 s".equals(r.getSettings().get("test.EventSettings.SE#threshold")), "es withoutThreshold -> 0 s");
            r.enable("test.EventSettings.SE").withPeriod(Duration.ofSeconds(5));
            check("5000000000 ns".equals(r.getSettings().get("test.EventSettings.SE#period")), "es withPeriod(5s) -> 5000000000 ns");
            r.enable("test.EventSettings.SE").withoutStackTrace();
            check("false".equals(r.getSettings().get("test.EventSettings.SE#stackTrace")), "es withoutStackTrace -> false");
            r.enable("test.EventSettings.SE").withStackTrace();
            check("true".equals(r.getSettings().get("test.EventSettings.SE#stackTrace")), "es withStackTrace -> true");
            r.enable("test.EventSettings.SE").with("period", "10 ms");
            check("10 ms".equals(r.getSettings().get("test.EventSettings.SE#period")), "es with(period,10 ms) verbatim");
            r.enable("test.EventSettings.SE").with("enabled", "false");
            check("false".equals(r.getSettings().get("test.EventSettings.SE#enabled")), "es with(enabled,false) verbatim");
        }
    }

    static void testDeepChaining() throws Exception {
        try (Recording r = new Recording()) {
            EventSettings es = r.enable("test.EventSettings.SE");
            check(es != null, "es enable returns settings");
            EventSettings s1 = es.withThreshold(Duration.ofMillis(3));
            check(s1 != null && s1 instanceof EventSettings, "es withThreshold returns EventSettings");
            EventSettings s2 = s1.withPeriod(Duration.ofSeconds(1));
            check(s2 != null, "es chain withPeriod");
            EventSettings s3 = s2.withoutStackTrace();
            check(s3 != null, "es chain withoutStackTrace");
            EventSettings s4 = s3.with("enabled", "true");
            check(s4 != null, "es chain with");
            check("3000000 ns".equals(r.getSettings().get("test.EventSettings.SE#threshold")), "es chained threshold persisted");
            check("1000000000 ns".equals(r.getSettings().get("test.EventSettings.SE#period")), "es chained period persisted");
            check("false".equals(r.getSettings().get("test.EventSettings.SE#stackTrace")), "es chained stackTrace persisted");
            EventSettings d = r.disable("test.EventSettings.SE");
            check(d != null, "es disable returns settings");
            check("false".equals(r.getSettings().get("test.EventSettings.SE#enabled")), "es disable sets enabled false");
            check("3000000 ns".equals(r.getSettings().get("test.EventSettings.SE#threshold")), "es disable retains other settings");
        }
    }

    static void testDeepNulls() throws Exception {
        try (Recording r = new Recording()) {
            try { r.enable("test.EventSettings.SE").with("threshold", null); check(false, "es with(name,null value) should NPE"); }
            catch (NullPointerException e) { check(true, "es with(name,null value) NPE"); }
            // version diff: on JDK-27 with(null name, value) NPEs (JDK-8 tolerated it)
            try { r.enable("test.EventSettings.SE").with(null, "x"); check(false, "es with(null name,..) should NPE on JDK-27"); }
            catch (NullPointerException e) { check(true, "es with(null name,..) NPE on JDK-27"); }
        }
    }

    static void testDeepCustomSettingPropagation() throws Exception {
        EventType t = EventType.getEventType(CSEvent.class);
        boolean hasFlavor = false;
        for (SettingDescriptor sd : t.getSettingDescriptors()) {
            if ("flavor".equals(sd.getName())) hasFlavor = true;
        }
        check(hasFlavor, "es custom setting visible in event type");
        try (Recording r = new Recording()) {
            r.enable("test.EventSettings.CS").with("flavor", "vanilla");
            check("vanilla".equals(r.getSettings().get("test.EventSettings.CS#flavor")), "es custom setting propagates to recording settings");
        }
    }
}
