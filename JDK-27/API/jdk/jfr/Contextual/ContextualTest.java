/*
 * @test
 * @summary jdk.jfr.Contextual conformance: type shape.
 * @run main ContextualTest
 */

import java.lang.reflect.Modifier;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class ContextualTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Contextual");
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


    @Name("test.Contextual.E") @Label("Ctx")
    static class CtxEvent extends Event {
        @Contextual @Label("who") String who;
        @Contextual long w2;
        String plain;
    }


    static void testDeep() throws Exception {
        Class<?> c = Contextual.class;
        checkMeta(c, "Contextual", false, true);
        checkTarget(c, new String[]{"FIELD"}, "Contextual");
        check(c.getDeclaredMethods().length == 0, "Contextual is marker (no value element)");
        Label self = Contextual.class.getAnnotation(Label.class);
        check(self != null && "Context".equals(self.value()), "self @Label 'Context'");
        // @Target 仅 FIELD：TYPE 不在目标集
        Set<String> ts = new HashSet<String>();
        for (ElementType e : c.getAnnotation(Target.class).value()) ts.add(e.name());
        check(!ts.contains("TYPE"), "@Target excludes TYPE");
        EventType et = EventType.getEventType(CtxEvent.class);
        check(et.getAnnotation(Contextual.class) == null, "type-level not applicable");
        ValueDescriptor vw = vdOf(et, "who");
        check(vw != null && vw.getAnnotation(Contextual.class) != null, "field-level @Contextual readback");
        check(hasAnnElem(vw.getAnnotationElements(), "jdk.jfr.Contextual"), "field elements contains Contextual");
        check(hasAnnElem(vw.getAnnotationElements(), "jdk.jfr.Label"), "coexisting @Label both visible");
        check("who".equals(vw.getLabel()), "coexisting label value intact");
        check(vdOf(et, "w2").getAnnotation(Contextual.class) != null, "second field readback");
        check(vdOf(et, "plain").getAnnotation(Contextual.class) == null, "unannotated field absent");
        check(!hasAnnElem(vdOf(et, "plain").getAnnotationElements(), "jdk.jfr.Contextual"), "unannotated elements absent");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Contextual.E");
            r.start();
            CtxEvent e = new CtxEvent(); e.who = "main"; e.w2 = 1; e.plain = "x"; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Contextual.E", 1);
        RecordedEvent re = firstIn(p, "test.Contextual.E");
        check(re != null && "main".equals(re.getString("who")), "string field round-trip");
        check(re != null && hasAnnElem(vdOf(re.getEventType(), "who").getAnnotationElements(), "jdk.jfr.Contextual"), "Contextual survives round-trip");
        Files.delete(p);
    }

}
