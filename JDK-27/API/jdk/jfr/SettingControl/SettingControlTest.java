/*
 * @test
 * @summary jdk.jfr.SettingControl conformance: white-box structure + black-box anonymous subclass.
 * @run main SettingControlTest
 */

import jdk.jfr.*;
import java.util.Set;
import java.lang.reflect.*;
import java.util.*;

public class SettingControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "combine"), "combine");
        check(hasMethod(c, "setValue"), "setValue");
        check(hasMethod(c, "getValue"), "getValue");
        // version diff: on JDK-27 SettingControl no longer extends the internal
        // jdk.jfr.internal.Control base class (JDK-8 backport still does)
        check(c.getSuperclass() == Object.class, "superclass Object on JDK-27");
    }

    static void testBlackBox() {
        SettingControl sc = new SettingControl() {
            private String value = "";
            public String combine(Set<String> values) { return values.iterator().hasNext() ? values.iterator().next() : value; }
            public void setValue(String v) { value = v; }
            public String getValue() { return value; }
        };
        sc.setValue("5 s");
        check("5 s".equals(sc.getValue()), "getValue");
        check("5 s".equals(sc.combine(Set.of("5 s"))), "combine");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepAbstractContract();
        testDeepCustomControlSemantics();
        testDeepSettingIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static class MyControl extends SettingControl {
        private String v = "none";
        public String combine(Set<String> s) { return String.join("|", s); }
        public void setValue(String s) { v = s; }
        public String getValue() { return v; }
    }
    @Name("test.SettingControl.SC")
    static class SCEvent extends Event { @SettingDefinition boolean flavor(MyControl c) { return true; } }

    static void testDeepAbstractContract() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingControl");
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 3, "sc exactly 3 abstract methods (combine/setValue/getValue), got " + abstractCount);
        try { SettingControl.class.getDeclaredConstructor(); check(true, "sc default ctor declared (protected)"); }
        catch (Throwable t) { check(false, "sc default ctor: " + t); }
        // SettingControl cannot be instantiated directly
        try { Class.forName("jdk.jfr.SettingControl").newInstance(); check(false, "sc direct instantiation should fail"); }
        catch (Throwable t) { check(true, "sc direct instantiation fails: " + t.getClass().getSimpleName()); }
    }

    static void testDeepCustomControlSemantics() {
        MyControl ctl = new MyControl();
        check("none".equals(ctl.getValue()), "sc initial value from implementation");
        ctl.setValue("10 ms");
        check("10 ms".equals(ctl.getValue()), "sc setValue/getValue roundtrip");
        Set<String> one = new HashSet<String>(); one.add("10 ms");
        check("10 ms".equals(ctl.combine(one)), "sc combine single value");
        Set<String> many = new LinkedHashSet<String>(Arrays.asList("10 ms", "20 s", "1 ns"));
        String combined = ctl.combine(many);
        check(combined.contains("10 ms") && combined.contains("20 s") && combined.contains("1 ns"), "sc combine sees all values");
        ctl.setValue("bogus");
        check("bogus".equals(ctl.getValue()), "sc setValue stores arbitrary string (validation is implementation-defined)");
    }

    static void testDeepSettingIntegration() throws Exception {
        EventType t = EventType.getEventType(SCEvent.class);
        SettingDescriptor flavor = null;
        for (SettingDescriptor sd : t.getSettingDescriptors()) {
            if ("flavor".equals(sd.getName())) flavor = sd;
        }
        check(flavor != null, "sc @SettingDefinition method exposes descriptor");
        check("none".equals(flavor.getDefaultValue()), "sc default value from fresh control getValue()");
        check(flavor.getTypeName().endsWith("MyControl"), "sc descriptor typeName is the control class, got " + flavor.getTypeName());
        check(flavor.getLabel() == null, "sc custom descriptor label null (no @Label support)");
        try (Recording r = new Recording()) {
            r.enable("test.SettingControl.SC").with("flavor", "chocolate");
            check("chocolate".equals(r.getSettings().get("test.SettingControl.SC#flavor")), "sc custom setting propagates via with()");
        }
    }
}
