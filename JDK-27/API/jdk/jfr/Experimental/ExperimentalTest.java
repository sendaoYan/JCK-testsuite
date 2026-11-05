/*
 * @test
 * @summary jdk.jfr.Experimental conformance: type shape.
 * @run main ExperimentalTest
 */

import java.lang.reflect.Modifier;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class ExperimentalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Experimental");
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


    @Name("test.Experimental.E") @Experimental @Label("Exp")
    static class ExpEvent extends Event {
        @Experimental @Label("x") long x;
        @Label("y") long y;
    }
    @Name("test.Experimental.Plain")
    static class PlainEvent extends Event { long x; }


    static void testDeep() throws Exception {
        Class<?> c = Experimental.class;
        checkMeta(c, "Experimental", true, true);
        checkTarget(c, new String[]{"FIELD","TYPE"}, "Experimental");
        check(c.getDeclaredMethods().length == 0, "Experimental is marker");
        Label self = Experimental.class.getAnnotation(Label.class);
        check(self != null && "Experimental".equals(self.value()), "self @Label 'Experimental'");
        Description sd = Experimental.class.getAnnotation(Description.class);
        check(sd != null && sd.value().contains("not to be shown"), "self @Description mentions hiding");
        EventType et = EventType.getEventType(ExpEvent.class);
        check(et.getAnnotation(Experimental.class) != null, "type-level @Experimental readback");
        check(hasAnnElem(et.getAnnotationElements(), "jdk.jfr.Experimental"), "type annotationElements contains Experimental");
        ValueDescriptor vx = vdOf(et, "x"), vy = vdOf(et, "y");
        check(vx != null && vx.getAnnotation(Experimental.class) != null, "field-level readback");
        check(hasAnnElem(vx.getAnnotationElements(), "jdk.jfr.Experimental"), "field annotationElements contains Experimental");
        check(vy != null && vy.getAnnotation(Experimental.class) == null, "unannotated field absent");
        EventType pt = EventType.getEventType(PlainEvent.class);
        check(pt.getAnnotation(Experimental.class) == null, "plain event not experimental");
        check(!hasAnnElem(pt.getAnnotationElements(), "jdk.jfr.Experimental"), "plain event elements absent");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Experimental.E");
            r.start();
            ExpEvent e = new ExpEvent(); e.x = 1; e.y = 2; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Experimental.E", 1);
        RecordedEvent re = firstIn(p, "test.Experimental.E");
        check(re != null && re.getEventType().getAnnotation(Experimental.class) != null, "Experimental survives round-trip");
        check(re != null && re.getEventType().getFields().size() == 6, "4 implicit + 2 user fields");
        Files.delete(p);
    }

}
