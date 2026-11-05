/*
 * @test
 * @summary jdk.jfr.Timestamp conformance: type shape via reflection (headless-safe).
 * @run main TimestampTest
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

public class TimestampTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Timestamp", false, TimestampTest.class.getClassLoader());
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
        check(hasField(c, "MILLISECONDS_SINCE_EPOCH"), "declares field MILLISECONDS_SINCE_EPOCH");
        check(hasField(c, "TICKS"), "declares field TICKS");
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


    @Name("test.Timestamp.E") @Timestamp(Timestamp.TICKS) @Label("Stamp")
    static class StampEvent extends Event {
        @Timestamp(Timestamp.TICKS) @Label("t") long t;
        @Timestamp(Timestamp.MILLISECONDS_SINCE_EPOCH) long epoch;
        @Timestamp("bogus") long bogus;
        long plain;
    }


    static void testDeep() throws Exception {
        Class<?> c = Timestamp.class;
        checkMeta(c, "Timestamp", false, true);
        checkTarget(c, new String[]{"FIELD","TYPE","METHOD"}, "Timestamp");
        checkValueSig(c, "String", "MILLISECONDS_SINCE_EPOCH", "Timestamp");
        check(Timestamp.class.getAnnotation(ContentType.class) != null, "carries @ContentType");
        Set<String> consts = new HashSet<String>(java.util.Arrays.asList("MILLISECONDS_SINCE_EPOCH","TICKS"));
        Set<String> got = new HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) got.add(f.getName());
        check(got.equals(consts), "constant set exactly {MILLISECONDS_SINCE_EPOCH,TICKS}: " + got);
        EventType et = EventType.getEventType(StampEvent.class);
        check(et.getAnnotation(Timestamp.class) != null && "TICKS".equals(et.getAnnotation(Timestamp.class).value()), "type-level readback");
        check(hasAnnElem(et.getAnnotationElements(), "jdk.jfr.Timestamp"), "type elements contains Timestamp");
        check("TICKS".equals(vdOf(et, "t").getAnnotation(Timestamp.class).value()), "field TICKS readback");
        check("MILLISECONDS_SINCE_EPOCH".equals(vdOf(et, "epoch").getAnnotation(Timestamp.class).value()), "field MILLISECONDS_SINCE_EPOCH readback");
        check(vdOf(et, "plain").getAnnotation(Timestamp.class) == null, "unannotated field null");
        // 隐式 startTime 自带 @Timestamp(TICKS)（双侧实测）
        Timestamp st = vdOf(et, "startTime").getAnnotation(Timestamp.class);
        check(st != null && "TICKS".equals(st.value()), "implicit startTime @Timestamp(TICKS)");
        check("bogus".equals(vdOf(et, "bogus").getAnnotation(Timestamp.class).value()), "bogus value retained raw, no exception");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Timestamp.E");
            r.start();
            StampEvent e = new StampEvent(); e.t = 1234567890L; e.epoch = 5; e.bogus = 6; e.plain = 1; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Timestamp.E", 1);
        RecordedEvent re = firstIn(p, "test.Timestamp.E");
        check(re != null && re.getLong("t") == 1234567890L, "value round-trip");
        check(re != null && "TICKS".equals(vdOf(re.getEventType(), "t").getAnnotation(Timestamp.class).value()), "Timestamp survives round-trip");
        check(re != null && re.getStartTime() != null && !re.getStartTime().isBefore(java.time.Instant.EPOCH.minusSeconds(1)), "startTime sane");
        Files.delete(p);
    }

}
