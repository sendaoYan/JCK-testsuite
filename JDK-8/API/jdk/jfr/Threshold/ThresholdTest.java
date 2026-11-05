/*
 * @test
 * @summary jdk.jfr.Threshold conformance: type shape via reflection (headless-safe).
 * @run main ThresholdTest
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

public class ThresholdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Threshold", false, ThresholdTest.class.getClassLoader());
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


    @Name("test.Thr.T0") @Threshold("0 ns")
    static class T0Event extends Event { long id; }
    @Name("test.Thr.T20") @Threshold("20 ms")
    static class T20Event extends Event { long id; }
    @Name("test.Thr.Def")
    static class DefEvent extends Event { long id; }
    @Name("test.Thr.Bogus") @Threshold("bogus")
    static class BogusEvent extends Event { long id; }
    @Name("test.Thr.Neg") @Threshold("-1 s")
    static class NegEvent extends Event { long id; }


    static void testDeep() throws Exception {
        Class<?> c = Threshold.class;
        checkMeta(c, "Threshold", true, true);
        checkTarget(c, new String[]{"TYPE"}, "Threshold");
        checkValueSig(c, "String", "0 ns", "Threshold");
        check("threshold".equals(Threshold.NAME), "NAME constant 'threshold'");
        EventType t0 = EventType.getEventType(T0Event.class);
        EventType t20 = EventType.getEventType(T20Event.class);
        EventType def = EventType.getEventType(DefEvent.class);
        check("0 ns".equals(settingDefault(t0, "threshold")), "@Threshold(\"0 ns\") descriptor default");
        check("20 ms".equals(settingDefault(t20, "threshold")), "@Threshold(\"20 ms\") descriptor default");
        check("0 ns".equals(settingDefault(def, "threshold")), "no annotation -> '0 ns'");
        // 闭环 1：阈值 0 ns -> 零时长事件被录制
        Path p1 = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Thr.T0");
            r.start();
            new T0Event().commit();
            r.stop();
            r.dump(p1);
        }
        checkLoopFacts(p1, "test.Thr.T0", 1);
        RecordedEvent re = firstIn(p1, "test.Thr.T0");
        check(re != null && re.getDuration().toNanos() == 0, "zero-duration commit -> duration 0");
        Files.delete(p1);
        // 闭环 2：阈值 20 ms -> 零时长事件低于阈值被丢弃，shouldCommit=false
        Path p2 = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Thr.T0"); // sibling 保持录制非空
            r.enable("test.Thr.T20");
            r.start();
            check(new T20Event().shouldCommit() == false, "below threshold -> shouldCommit false");
            new T20Event().commit();
            r.stop();
            r.dump(p2);
        }
        check(countIn(p2, "test.Thr.T20") == 0, "below-threshold event dropped");
        check(countIn(p2, "test.Thr.T0") == 0, "T0 committed nothing here");
        Files.delete(p2);
        // 闭环 3：录制级 with("threshold","0 ns") 覆盖注解 -> 恢复录制
        Path p3 = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Thr.T20").with("threshold", "0 ns");
            r.start();
            new T20Event().commit();
            r.stop();
            r.dump(p3);
        }
        check(countIn(p3, "test.Thr.T20") == 1, "recording-level threshold override restores capture");
        Files.delete(p3);
        // 负例（双侧实测）：非法/负值 duration 串在注册时被容忍，描述符保留原始串（27 侧额外打 warning 回退，8 侧静默）
        EventType bogus = EventType.getEventType(BogusEvent.class);
        check(bogus != null, "@Threshold(\"bogus\") registration tolerated (no exception)");
        check("bogus".equals(settingDefault(bogus, "threshold")), "bogus value retained raw in descriptor");
        EventType neg = EventType.getEventType(NegEvent.class);
        check(neg != null, "@Threshold(\"-1 s\") registration tolerated");
        check("-1 s".equals(settingDefault(neg, "threshold")), "negative value retained raw");
        try (Recording r = new Recording()) {
            r.enable("test.Thr.Bogus");
            r.start();
            r.stop();
        }
        check(true, "enable+start with bogus-threshold event tolerated");
    }

}
