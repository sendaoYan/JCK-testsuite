/*
 * @test
 * @summary jdk.jfr.SettingDefinition conformance: type shape via reflection (headless-safe).
 * @run main SettingDefinitionTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.util.*;

public class SettingDefinitionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingDefinition", false, SettingDefinitionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == true, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        testDeepAnnotationMeta();
        testDefinitionUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static class MyControl extends SettingControl {
        public String combine(Set<String> s) { return String.join("|", s); }
        public void setValue(String s) { }
        public String getValue() { return "none"; }
    }
    @Name("test.SettingDefinition.Valid")
    static class ValidEvent extends Event {
        @SettingDefinition boolean mysetting(MyControl c) { return true; }
    }
    @Name("test.SettingDefinition.Renamed")
    static class RenamedEvent extends Event {
        @SettingDefinition @Name("renamed")
        boolean whatever(MyControl c) { return true; }
    }
    @Name("test.SettingDefinition.BadSig")
    static class BadSigEvent extends Event {
        @SettingDefinition boolean badsig() { return true; } // missing control parameter
    }
    @Name("test.SettingDefinition.Two")
    static class TwoEvent extends Event {
        @SettingDefinition boolean first(MyControl c) { return true; }
        @SettingDefinition boolean second(MyControl c) { return true; }
    }

    static void testDeepAnnotationMeta() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingDefinition");
        Retention ret = c.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.RUNTIME, "sd @Retention(RUNTIME)");
        Target tgt = c.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.METHOD, "sd @Target(METHOD) only");
        check(c.getDeclaredMethods().length == 0, "sd is a marker annotation (no members)");
    }

    static void testDefinitionUsage() throws Exception {
        // valid: method with control param returning boolean -> descriptor named after the method
        EventType t = EventType.getEventType(ValidEvent.class);
        Set<String> names = new HashSet<String>();
        for (SettingDescriptor sd : t.getSettingDescriptors()) names.add(sd.getName());
        check(names.contains("mysetting"), "sd valid signature exposes 'mysetting'");
        check(names.contains("enabled") && names.contains("threshold") && names.contains("stackTrace"), "sd implicit settings still present");
        // @Name on the method renames the descriptor
        EventType rn = EventType.getEventType(RenamedEvent.class);
        Set<String> rnames = new HashSet<String>();
        for (SettingDescriptor sd : rn.getSettingDescriptors()) rnames.add(sd.getName());
        check(rnames.contains("renamed"), "sd @Name on method renames descriptor");
        check(!rnames.contains("whatever"), "sd original method name not exposed after rename");
        // bad signature: no control parameter -> method silently ignored, no exception
        EventType bad = EventType.getEventType(BadSigEvent.class);
        Set<String> bnames = new HashSet<String>();
        for (SettingDescriptor sd : bad.getSettingDescriptors()) bnames.add(sd.getName());
        check(!bnames.contains("badsig"), "sd bad signature (no control param) silently ignored");
        check(bnames.size() == 3, "sd bad-signature event keeps only the 3 implicit settings, got " + bnames.size());
        // two definitions on one event both appear
        EventType two = EventType.getEventType(TwoEvent.class);
        Set<String> tnames = new HashSet<String>();
        for (SettingDescriptor sd : two.getSettingDescriptors()) tnames.add(sd.getName());
        check(tnames.contains("first") && tnames.contains("second"), "sd two definitions both exposed");
        check(tnames.size() == 5, "sd two definitions + 3 implicit = 5 settings, got " + tnames.size());
        // settings for defined controls are settable through a recording
        try (Recording r = new Recording()) {
            r.enable("test.SettingDefinition.Valid").with("mysetting", "x");
            check("x".equals(r.getSettings().get("test.SettingDefinition.Valid#mysetting")), "sd defined setting settable via recording");
        }
    }
}
