/*
 * @test
 * @summary jdk.jfr.Timespan conformance: white-box structure + black-box annotation contract.
 * @run main TimespanTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.Timespan;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class TimespanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @Timespan("5 s")
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Timespan");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "isInterface");
        check(c.isAnnotation(), "isAnnotation");
        check(Annotation.class.isAssignableFrom(c), "implements Annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "one interface");
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, "runtime retention");
        Target t = c.getAnnotation(Target.class);
        check(t != null, "has target");
        Method m = c.getDeclaredMethod("value");
        check(m.getReturnType() == String.class, "value element type");
    }

    static void testBlackBox() throws Exception {
        Timespan a = Holder.class.getAnnotation(Timespan.class);
        check(a != null, "annotation applied");
        check("5 s".equals(a.value()), "value round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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


    @Name("test.Timespan.E") @Timespan(Timespan.SECONDS) @Label("TS")
    static class TSEvent extends Event {
        @Timespan(Timespan.SECONDS) @Label("d") long d;
        @Timespan(Timespan.TICKS) long tick;
        @Timespan("bogus") long bogus;
        long plain;
    }


    static void testDeep() throws Exception {
        Class<?> c = Timespan.class;
        checkMeta(c, "Timespan", false, true);
        checkTarget(c, new String[]{"FIELD","TYPE","METHOD"}, "Timespan");
        checkValueSig(c, "String", "NANOSECONDS", "Timespan");
        check(Timespan.class.getAnnotation(ContentType.class) != null, "carries @ContentType");
        // 常量集（双侧实测仅 5 个：无 MINUTES/HOURS/DAYS）
        Set<String> consts = new HashSet<String>(java.util.Arrays.asList("TICKS","SECONDS","MILLISECONDS","NANOSECONDS","MICROSECONDS"));
        Set<String> got = new HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) got.add(f.getName());
        check(got.equals(consts), "constant set exactly {TICKS,SECONDS,MILLISECONDS,NANOSECONDS,MICROSECONDS}: " + got);
        check("SECONDS".equals(Timespan.SECONDS), "SECONDS constant value");
        EventType et = EventType.getEventType(TSEvent.class);
        check(et.getAnnotation(Timespan.class) != null && "SECONDS".equals(et.getAnnotation(Timespan.class).value()), "type-level @Timespan readback");
        check(hasAnnElem(et.getAnnotationElements(), "jdk.jfr.Timespan"), "type elements contains Timespan");
        ValueDescriptor vd = vdOf(et, "d");
        check(vd != null && "SECONDS".equals(vd.getAnnotation(Timespan.class).value()), "field SECONDS readback");
        check("TICKS".equals(vdOf(et, "tick").getAnnotation(Timespan.class).value()), "field TICKS readback");
        check(vdOf(et, "plain").getAnnotation(Timespan.class) == null, "unannotated field null");
        // 隐式 duration 字段自带 @Timespan(TICKS)（双侧实测）
        Timespan dur = vdOf(et, "duration").getAnnotation(Timespan.class);
        check(dur != null && "TICKS".equals(dur.value()), "implicit duration field @Timespan(TICKS)");
        // 负例：非法值被容忍并原样保留（双侧实测）
        check("bogus".equals(vdOf(et, "bogus").getAnnotation(Timespan.class).value()), "bogus value retained raw, no exception");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Timespan.E");
            r.start();
            TSEvent e = new TSEvent(); e.d = 1500; e.tick = 7; e.bogus = 9; e.plain = 1; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Timespan.E", 1);
        RecordedEvent re = firstIn(p, "test.Timespan.E");
        check(re != null && re.getLong("d") == 1500, "long value round-trip (annotation does not transform payload)");
        check(re != null && "SECONDS".equals(vdOf(re.getEventType(), "d").getAnnotation(Timespan.class).value()), "Timespan survives round-trip");
        Files.delete(p);
    }

}
