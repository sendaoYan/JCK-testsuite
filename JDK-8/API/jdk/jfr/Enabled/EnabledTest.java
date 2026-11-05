/*
 * @test
 * @summary jdk.jfr.Enabled conformance: type shape via reflection (headless-safe).
 * @run main EnabledTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class EnabledTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Enabled", false, EnabledTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == true, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        check(hasMethod(c, "value"), "declares value");
        check(hasField(c, "NAME"), "declares field NAME");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===== 深化 2026-11-05（子代理B：元注解契约+value默认值+录制回读闭环+负例，实测基线见 DESIGN.md）=====
    static ValueDescriptor vdOf(EventType et, String field) {
        for (ValueDescriptor vd : et.getFields()) if (vd.getName().equals(field)) return vd;
        return null;
    }
    static boolean hasAnnElem(List<AnnotationElement> l, String typeName) {
        for (AnnotationElement ae : l) if (ae.getTypeName().equals(typeName)) return true;
        return false;
    }
    static String settingDefault(EventType et, String name) {
        for (SettingDescriptor sd : et.getSettingDescriptors()) if (sd.getName().equals(name)) return sd.getDefaultValue();
        return null;
    }
    static boolean settingPresent(EventType et, String name) {
        for (SettingDescriptor sd : et.getSettingDescriptors()) if (sd.getName().equals(name)) return true;
        return false;
    }
    static boolean typeRegistered(String name) {
        for (EventType t : FlightRecorder.getFlightRecorder().getEventTypes()) if (t.getName().equals(name)) return true;
        return false;
    }
    static int countIn(Path p, String name) throws Exception {
        int n = 0;
        for (RecordedEvent e : RecordingFile.readAllEvents(p)) if (e.getEventType().getName().equals(name)) n++;
        return n;
    }
    static RecordedEvent firstIn(Path p, String name) throws Exception {
        for (RecordedEvent e : RecordingFile.readAllEvents(p)) if (e.getEventType().getName().equals(name)) return e;
        return null;
    }
    static void checkTarget(Class<?> c, String[] expect, String tag) {
        Target t = c.getAnnotation(Target.class);
        check(t != null, tag + " declares @Target");
        if (t == null) return;
        ElementType[] got = t.value();
        check(got.length == expect.length, tag + " @Target arity " + got.length + "==" + expect.length);
        Set<String> gs = new HashSet<String>();
        for (ElementType e : got) gs.add(e.name());
        for (String e : expect) check(gs.contains(e), tag + " @Target contains " + e);
    }
    static void checkMeta(Class<?> c, String simple, boolean inherited, boolean metaDef) {
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, simple + " @Retention(RUNTIME)");
        check(!c.isAnnotationPresent(Documented.class), simple + " not @Documented");
        check(c.isAnnotationPresent(Inherited.class) == inherited, simple + " @Inherited==" + inherited);
        check(!c.isAnnotationPresent(Repeatable.class), simple + " not @Repeatable");
        boolean md = false;
        for (Annotation a : c.getAnnotations()) if (a.annotationType().getName().equals("jdk.jfr.MetadataDefinition")) md = true;
        check(md == metaDef, simple + " @MetadataDefinition==" + metaDef);
        check(c.getDeclaredAnnotations().length > 0, simple + " declares meta-annotations");
    }
    static void checkValueSig(Class<?> c, String retType, Object defVal, String tag) {
        try {
            Method m = c.getDeclaredMethod("value");
            check(m.getReturnType().getSimpleName().equals(retType), tag + " value() returns " + retType);
            check(java.util.Objects.equals(String.valueOf(m.getDefaultValue()), String.valueOf(defVal)), tag + " value() default " + defVal);
        } catch (NoSuchMethodException e) { check(false, tag + " value() declared"); }
    }
    static void checkLoopFacts(Path p, String name, int expect) throws Exception {
        int bulk = countIn(p, name);
        check(bulk == expect, name + " readAllEvents count==" + expect + " (got " + bulk + ")");
        int streamed = 0;
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) { RecordedEvent e = rf.readEvent(); if (e.getEventType().getName().equals(name)) streamed++; }
        }
        check(streamed == expect, name + " streaming count equals readAllEvents");
        RecordedEvent e = firstIn(p, name);
        if (e != null) {
            check(e.getStartTime() != null, name + " startTime non-null");
            check(!e.getStartTime().isAfter(e.getEndTime()), name + " startTime<=endTime");
            check(e.getDuration() != null && !e.getDuration().isNegative(), name + " duration>=0");
            check(e.getThread() != null, name + " thread non-null");
            check(e.getEventType().getId() > 0, name + " event type id>0");
        }
    }
    static Path tmp() throws Exception { return Files.createTempFile("jbfr", ".jfr"); }


    @Name("test.Enabled.Off") @Enabled(false)
    static class OffEvent extends Event { long id; }
    @Name("test.Enabled.On") @Enabled(true)
    static class OnEvent extends Event { long id; }
    @Name("test.Enabled.Def")
    static class DefEvent extends Event { long id; }


    static void testDeep() throws Exception {
        Class<?> c = Enabled.class;
        checkMeta(c, "Enabled", true, true);
        checkTarget(c, new String[]{"TYPE"}, "Enabled");
        checkValueSig(c, "boolean", "true", "Enabled");
        check("enabled".equals(Enabled.NAME), "NAME constant 'enabled'");
        EventType off = EventType.getEventType(OffEvent.class);
        EventType on = EventType.getEventType(OnEvent.class);
        EventType def = EventType.getEventType(DefEvent.class);
        check("false".equals(settingDefault(off, "enabled")), "@Enabled(false) -> setting default 'false'");
        check("true".equals(settingDefault(on, "enabled")), "@Enabled(true) -> setting default 'true'");
        check("true".equals(settingDefault(def, "enabled")), "no annotation -> default 'true'");
        check(!off.isEnabled(), "off event not enabled before recording");
        // 门控闭环 1：@Enabled(false) 且未显式 enable -> commit 被静默丢弃（recording 内其它事件已 enable）
        Path p1 = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Enabled.Def");
            r.start();
            check(!EventType.getEventType(OffEvent.class).isEnabled(), "off stays disabled during recording");
            check(new OffEvent().shouldCommit() == false, "off shouldCommit false");
            new OffEvent().commit();
            DefEvent de = new DefEvent(); de.id = 1; de.commit();
            r.stop();
            r.dump(p1);
        }
        check(countIn(p1, "test.Enabled.Off") == 0, "@Enabled(false) event dropped silently");
        check(countIn(p1, "test.Enabled.Def") == 1, "default-enabled sibling recorded");
        Files.delete(p1);
        // 门控闭环 2：显式 r.enable 覆盖 @Enabled(false) -> 录制成功且 settings 键可见
        Path p2 = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Enabled.Off");
            check("true".equals(r.getSettings().get("test.Enabled.Off#enabled")), "explicit enable propagates to settings map");
            r.start();
            check(EventType.getEventType(OffEvent.class).isEnabled(), "explicit enable flips isEnabled");
            check(new OffEvent().shouldCommit() == true, "shouldCommit true after enable");
            new OffEvent().commit();
            r.stop();
            r.dump(p2);
        }
        checkLoopFacts(p2, "test.Enabled.Off", 1);
        Files.delete(p2);
        // 门控闭环 3：.with("enabled","false") 覆盖默认 true -> 丢弃
        Path p3 = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Enabled.On").with("enabled", "false");
            r.enable("test.Enabled.Def");
            r.start();
            new OnEvent().commit();
            DefEvent de = new DefEvent(); de.id = 2; de.commit();
            r.stop();
            r.dump(p3);
        }
        check(countIn(p3, "test.Enabled.On") == 0, "with(enabled,false) drops default-on event");
        check(countIn(p3, "test.Enabled.Def") == 1, "sibling still recorded");
        Files.delete(p3);
    }

}
