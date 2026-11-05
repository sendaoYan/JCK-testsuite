/*
 * @test
 * @summary jdk.jfr.Frequency conformance: annotation with runtime retention.
 * @run main FrequencyTest
 */

import jdk.jfr.Frequency;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class FrequencyTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(Frequency.class.isAnnotation(), "annotation");
        check(Frequency.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "runtime retention");
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


    @Name("test.Frequency.E") @Label("Freq")
    static class FreqEvent extends Event {
        @Frequency @Label("hz") long hz;
        @Label("plain") long plain;
    }


    static void testDeep() throws Exception {
        Class<?> c = Frequency.class;
        checkMeta(c, "Frequency", false, true);
        checkTarget(c, new String[]{"FIELD","METHOD"}, "Frequency");
        check(c.getDeclaredMethods().length == 0, "Frequency is marker (NO value element)");
        check(Frequency.class.getAnnotation(ContentType.class) != null, "carries @ContentType");
        Label self = Frequency.class.getAnnotation(Label.class);
        check(self != null && "Frequency".equals(self.value()), "self @Label 'Frequency'");
        Description sd = Frequency.class.getAnnotation(Description.class);
        check(sd != null && sd.value().contains("Hertz"), "self @Description mentions Hertz");
        // @Target 不含 TYPE：不允许标注事件类型（反射锁定）
        Set<String> ts = new HashSet<String>();
        for (ElementType e : c.getAnnotation(Target.class).value()) ts.add(e.name());
        check(!ts.contains("TYPE"), "@Target excludes TYPE");
        EventType et = EventType.getEventType(FreqEvent.class);
        check(et.getAnnotation(Frequency.class) == null, "cannot annotate type (absent by construction)");
        ValueDescriptor vd = vdOf(et, "hz");
        check(vd != null && vd.getAnnotation(Frequency.class) != null, "field-level @Frequency readback");
        check(hasAnnElem(vd.getAnnotationElements(), "jdk.jfr.Frequency"), "field annotationElements contains Frequency");
        check(vdOf(et, "plain").getAnnotation(Frequency.class) == null, "unannotated field absent");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Frequency.E");
            r.start();
            FreqEvent e = new FreqEvent(); e.hz = 60; e.plain = 1; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Frequency.E", 1);
        RecordedEvent re = firstIn(p, "test.Frequency.E");
        check(re != null && re.getLong("hz") == 60, "hz field round-trip");
        check(re != null && vdOf(re.getEventType(), "hz").getAnnotation(Frequency.class) != null, "Frequency survives round-trip");
        Files.delete(p);
    }

}
