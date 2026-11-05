/*
 * @test
 * @summary jdk.jfr.EventSettings conformance: type shape via reflection (headless-safe).
 * @run main EventSettingsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.time.Duration;
import java.util.*;

public class EventSettingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.EventSettings", false, EventSettingsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "with"), "declares with");
        check(hasMethod(c, "withPeriod"), "declares withPeriod");
        check(hasMethod(c, "withStackTrace"), "declares withStackTrace");
        check(hasMethod(c, "withThreshold"), "declares withThreshold");
        check(hasMethod(c, "withoutStackTrace"), "declares withoutStackTrace");
        check(hasMethod(c, "withoutThreshold"), "declares withoutThreshold");
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
            // version diff: on JDK-8 with(null name, value) is tolerated; on JDK-27+ it NPEs
            try { r.enable("test.EventSettings.SE").with(null, "x"); check(true, "es with(null name,..) tolerated on JDK-8"); }
            catch (NullPointerException e) { check(false, "es with(null name,..) unexpectedly NPE on JDK-8: " + e); }
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
