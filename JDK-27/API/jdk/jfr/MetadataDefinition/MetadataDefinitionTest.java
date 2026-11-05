/*
 * @test
 * @summary jdk.jfr.MetadataDefinition conformance: type shape.
 * @run main MetadataDefinitionTest
 */

import java.lang.reflect.Modifier;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class MetadataDefinitionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.MetadataDefinition");
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


    @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.FIELD, ElementType.TYPE}) @MetadataDefinition @Label("MId")
    @interface MId { String value() default "mv"; }
    @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.FIELD, ElementType.TYPE})
    @interface NoMeta { String value() default "nv"; }
    @Name("test.MetadataDefinition.E") @MId("type-level")
    static class MDEvent extends Event {
        @MId("f1") @Label("a") long a;
        @NoMeta("f2") long b;
    }


    static void testDeep() throws Exception {
        Class<?> c = MetadataDefinition.class;
        checkMeta(c, "MetadataDefinition", false, false); // 自身不携带 @MetadataDefinition
        checkTarget(c, new String[]{"TYPE"}, "MetadataDefinition");
        check(c.getDeclaredMethods().length == 0, "MetadataDefinition is marker");
        // 核心契约：决定自定义注解是否进入 JFR 元数据（实测正负对照）
        EventType et = EventType.getEventType(MDEvent.class);
        MId ta = et.getAnnotation(MId.class);
        check(ta != null && "type-level".equals(ta.value()), "type-level custom annotation WITH @MetadataDefinition visible");
        check(hasAnnElem(et.getAnnotationElements(), MId.class.getName()), "type elements contains custom annotation");
        check(et.getAnnotation(NoMeta.class) == null, "type-level custom annotation WITHOUT @MetadataDefinition invisible");
        check(!hasAnnElem(et.getAnnotationElements(), NoMeta.class.getName()), "type elements excludes non-meta annotation");
        ValueDescriptor va = vdOf(et, "a"), vb = vdOf(et, "b");
        MId fa = va.getAnnotation(MId.class);
        check(fa != null && "f1".equals(fa.value()), "field-level WITH visible");
        check(hasAnnElem(va.getAnnotationElements(), MId.class.getName()), "field elements contains MId");
        check(vb.getAnnotation(NoMeta.class) == null, "field-level WITHOUT invisible via getAnnotation");
        check(!hasAnnElem(vb.getAnnotationElements(), NoMeta.class.getName()), "field elements excludes NoMeta");
        check(vb.getAnnotationElements().isEmpty(), "field without meta annotations has empty element list");
        // @Retention(RUNTIME) 仍是必要条件（配合 @MetadataDefinition）
        check(MId.class.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME, "custom annotation needs RUNTIME retention");
        check(NoMeta.class.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME, "negative control also RUNTIME (isolates @MetadataDefinition as the differentiator)");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.MetadataDefinition.E");
            r.start();
            MDEvent e = new MDEvent(); e.a = 1; e.b = 2; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.MetadataDefinition.E", 1);
        RecordedEvent re = firstIn(p, "test.MetadataDefinition.E");
        check(re != null && re.getEventType().getAnnotation(MId.class) != null, "custom meta annotation survives round-trip");
        check(re != null && !hasAnnElem(re.getEventType().getAnnotationElements(), NoMeta.class.getName()), "non-meta stays invisible after round-trip");
        Files.delete(p);
    }

}
