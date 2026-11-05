/*
 * @test
 * @summary jdk.jfr.Relational conformance: type shape via reflection (headless-safe).
 * @run main RelationalTest
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

public class RelationalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Relational", false, RelationalTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == true, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
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


    @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.FIELD}) @MetadataDefinition @Relational
    @interface PatientId { String value() default "?"; }
    @Name("test.Relational.E") @Label("Rel")
    static class RelEvent extends Event {
        @PatientId("P123") @Label("pid") String pid;
        @Label("plain") String plain;
    }


    static void testDeep() throws Exception {
        Class<?> c = Relational.class;
        checkMeta(c, "Relational", false, true);
        checkTarget(c, new String[]{"ANNOTATION_TYPE"}, "Relational");
        check(c.getDeclaredMethods().length == 0, "Relational is marker");
        Label self = Relational.class.getAnnotation(Label.class);
        check(self != null && "Relation".equals(self.value()), "self @Label 'Relation'");
        // 元注解位：@Relational 只能标注注解类型；自定义关系注解经其组合
        check(PatientId.class.getAnnotation(Relational.class) != null, "custom annotation carries @Relational");
        check(PatientId.class.getAnnotation(MetadataDefinition.class) != null, "custom annotation carries @MetadataDefinition");
        EventType et = EventType.getEventType(RelEvent.class);
        check(et.getAnnotation(Relational.class) == null, "cannot apply directly to event type");
        ValueDescriptor vd = vdOf(et, "pid");
        check(vd != null, "field present");
        PatientId pa = vd.getAnnotation(PatientId.class);
        check(pa != null && "P123".equals(pa.value()), "custom relational annotation readback with value");
        check(hasAnnElem(vd.getAnnotationElements(), PatientId.class.getName()), "field elements contains custom annotation");
        check(vdOf(et, "plain").getAnnotation(PatientId.class) == null, "unannotated field absent");
        // 序列化后注解元素仍在（consumer 侧可见）
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Relational.E");
            r.start();
            RelEvent e = new RelEvent(); e.pid = "P123"; e.plain = "x"; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Relational.E", 1);
        RecordedEvent re = firstIn(p, "test.Relational.E");
        check(re != null && "P123".equals(re.getString("pid")), "field value round-trip");
        check(re != null && hasAnnElem(vdOf(re.getEventType(), "pid").getAnnotationElements(), PatientId.class.getName()), "custom relational survives round-trip");
        Files.delete(p);
    }

}
