/*
 * @test
 * @summary jdk.jfr.Label conformance: white-box structure + black-box annotation contract.
 * @run main LabelTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.Label;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class LabelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @Label("conformance")
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Label");
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
        Label a = Holder.class.getAnnotation(Label.class);
        check(a != null, "annotation applied");
        check("conformance".equals(a.value()), "value round-trip");
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


    @Name("test.Label.E") @Label("Hello")
    static class LabeledEvent extends Event {
        @Label("idL") long id;
        @Label("") String empty;
        long plain;
    }


    static void testDeep() throws Exception {
        Class<?> c = Label.class;
        checkMeta(c, "Label", false, true);
        checkTarget(c, new String[]{"TYPE","FIELD","METHOD"}, "Label");
        checkValueSig(c, "String", null, "Label");
        EventType et = EventType.getEventType(LabeledEvent.class);
        check("Hello".equals(et.getLabel()), "type-level @Label readback");
        check(hasAnnElem(et.getAnnotationElements(), "jdk.jfr.Label"), "type annotationElements contains Label");
        check(et.getDescription() == null, "no @Description -> null");
        ValueDescriptor vid = vdOf(et, "id");
        check("idL".equals(vid.getLabel()), "field-level @Label readback");
        check(hasAnnElem(vid.getAnnotationElements(), "jdk.jfr.Label"), "field elements contains Label");
        check(vid.getDescription() == null, "field no description -> null");
        ValueDescriptor vEmpty = vdOf(et, "empty");
        check(vEmpty != null && "".equals(vEmpty.getLabel()), "@Label(\"\") -> empty string (not null), no exception");
        ValueDescriptor vPlain = vdOf(et, "plain");
        check(vPlain != null && vPlain.getLabel() == null, "unannotated field label null");
        // 隐式字段自带 Label（双侧实测）
        check("Start Time".equals(vdOf(et, "startTime").getLabel()), "implicit startTime label");
        check("Duration".equals(vdOf(et, "duration").getLabel()), "implicit duration label");
        check("Event Thread".equals(vdOf(et, "eventThread").getLabel()), "implicit eventThread label");
        check("Stack Trace".equals(vdOf(et, "stackTrace").getLabel()), "implicit stackTrace label");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Label.E");
            r.start();
            LabeledEvent e = new LabeledEvent(); e.id = 42; e.empty = ""; e.plain = 7; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Label.E", 1);
        RecordedEvent re = firstIn(p, "test.Label.E");
        check(re != null && "Hello".equals(re.getEventType().getLabel()), "label survives record-serialize-readback");
        check(re != null && re.getLong("id") == 42 && "idL".equals(vdOf(re.getEventType(), "id").getLabel()), "field label survives round-trip");
        check(re != null && "".equals(re.getString("empty")), "empty-string field value round-trip");
        Files.delete(p);
    }

}
