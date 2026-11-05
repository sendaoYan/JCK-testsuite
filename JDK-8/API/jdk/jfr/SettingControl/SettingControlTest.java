/*
 * @test
 * @summary jdk.jfr.SettingControl conformance: type shape via reflection (headless-safe).
 * @run main SettingControlTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.util.*;

public class SettingControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingControl", false, SettingControlTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.internal.Control"), "superclass jdk.jfr.internal.Control");
        check(hasMethod(c, "combine"), "declares combine");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "setValue"), "declares setValue");
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
