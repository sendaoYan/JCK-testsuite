/*
 * @test
 * @summary jdk.jfr.SettingDefinition conformance: type shape.
 * @run main SettingDefinitionTest
 */

import java.lang.reflect.Modifier;

public class SettingDefinitionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingDefinition");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == true, "isInterface");
        check(c.isAnnotation() == true, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == true, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        testDeepAnnotationMeta();
        testDefinitionUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static class MyControl extends jdk.jfr.SettingControl {
        public java.util.Set<String> seen;
        public String combine(java.util.Set<String> s) { return String.join("|", s); }
        public void setValue(String s) { }
        public String getValue() { return "none"; }
    }
    @jdk.jfr.Name("test.SettingDefinition.Valid")
    static class ValidEvent extends jdk.jfr.Event {
        @jdk.jfr.SettingDefinition boolean mysetting(MyControl c) { return true; }
    }
    @jdk.jfr.Name("test.SettingDefinition.Renamed")
    static class RenamedEvent extends jdk.jfr.Event {
        @jdk.jfr.SettingDefinition @jdk.jfr.Name("renamed")
        boolean whatever(MyControl c) { return true; }
    }
    @jdk.jfr.Name("test.SettingDefinition.BadSig")
    static class BadSigEvent extends jdk.jfr.Event {
        @jdk.jfr.SettingDefinition boolean badsig() { return true; } // missing control parameter
    }
    @jdk.jfr.Name("test.SettingDefinition.Two")
    static class TwoEvent extends jdk.jfr.Event {
        @jdk.jfr.SettingDefinition boolean first(MyControl c) { return true; }
        @jdk.jfr.SettingDefinition boolean second(MyControl c) { return true; }
    }

    static void testDeepAnnotationMeta() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingDefinition");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "sd @Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == java.lang.annotation.ElementType.METHOD, "sd @Target(METHOD) only");
        check(c.getDeclaredMethods().length == 0, "sd is a marker annotation (no members)");
    }

    static void testDefinitionUsage() throws Exception {
        // valid: method with control param returning boolean -> descriptor named after the method
        jdk.jfr.EventType t = jdk.jfr.EventType.getEventType(ValidEvent.class);
        java.util.Set<String> names = new java.util.HashSet<>();
        for (jdk.jfr.SettingDescriptor sd : t.getSettingDescriptors()) names.add(sd.getName());
        check(names.contains("mysetting"), "sd valid signature exposes 'mysetting'");
        check(names.contains("enabled") && names.contains("threshold") && names.contains("stackTrace"), "sd implicit settings still present");
        // @Name on the method renames the descriptor
        jdk.jfr.EventType rn = jdk.jfr.EventType.getEventType(RenamedEvent.class);
        java.util.Set<String> rnames = new java.util.HashSet<>();
        for (jdk.jfr.SettingDescriptor sd : rn.getSettingDescriptors()) rnames.add(sd.getName());
        check(rnames.contains("renamed"), "sd @Name on method renames descriptor");
        check(!rnames.contains("whatever"), "sd original method name not exposed after rename");
        // bad signature: no control parameter -> method silently ignored, no exception
        jdk.jfr.EventType bad = jdk.jfr.EventType.getEventType(BadSigEvent.class);
        java.util.Set<String> bnames = new java.util.HashSet<>();
        for (jdk.jfr.SettingDescriptor sd : bad.getSettingDescriptors()) bnames.add(sd.getName());
        check(!bnames.contains("badsig"), "sd bad signature (no control param) silently ignored");
        check(bnames.size() == 3, "sd bad-signature event keeps only the 3 implicit settings, got " + bnames.size());
        // two definitions on one event both appear
        jdk.jfr.EventType two = jdk.jfr.EventType.getEventType(TwoEvent.class);
        java.util.Set<String> tnames = new java.util.HashSet<>();
        for (jdk.jfr.SettingDescriptor sd : two.getSettingDescriptors()) tnames.add(sd.getName());
        check(tnames.contains("first") && tnames.contains("second"), "sd two definitions both exposed");
        check(tnames.size() == 5, "sd two definitions + 3 implicit = 5 settings, got " + tnames.size());
        // settings for defined controls are settable through a recording
        try (jdk.jfr.Recording r = new jdk.jfr.Recording()) {
            r.enable("test.SettingDefinition.Valid").with("mysetting", "x");
            check("x".equals(r.getSettings().get("test.SettingDefinition.Valid#mysetting")), "sd defined setting settable via recording");
        }
    }
}
