/*
 * @test
 * @summary jdk.jfr.Name conformance: white-box structure + black-box annotation contract.
 * @run main NameTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.Name;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class NameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @Name("conformance")
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Name");
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
        Name a = Holder.class.getAnnotation(Name.class);
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


    @Name("test.Name.E") @Label("Named")
    static class NamedEvent extends Event {
        @Name("identifier") @Label("id") long id;
        long plain;
    }
    // 无 @Name：默认事件名 = 类二进制名
    static class DefaultEvent extends Event { long id; }
    // 负例（27 侧实测）：非法名不抛异常，打 warning 后回退为类二进制名
    @Name("") static class BadEmptyEvent extends Event { long id; }
    @Name("has space") static class BadSpaceEvent extends Event { long id; }
    @Name(".lead") static class BadLeadEvent extends Event { long id; }
    @Name("test.Name.Trail.") static class TrailEvent extends Event { long id; }
    @Name("test.Name.Double..Dot") static class DoubleEvent extends Event { long id; }


    static void testDeep() throws Exception {
        Class<?> c = Name.class;
        checkMeta(c, "Name", false, true);
        checkTarget(c, new String[]{"TYPE","FIELD","METHOD"}, "Name");
        checkValueSig(c, "String", null, "Name");
        EventType et = EventType.getEventType(NamedEvent.class);
        check("test.Name.E".equals(et.getName()), "type-level @Name readback");
        check(et.getAnnotation(Name.class) != null && "test.Name.E".equals(et.getAnnotation(Name.class).value()), "getAnnotation(Name) value");
        check(hasAnnElem(et.getAnnotationElements(), "jdk.jfr.Name"), "type elements contains Name");
        check(vdOf(et, "identifier") != null, "field-level @Name renames field in metadata");
        check(vdOf(et, "id") == null, "original java field name hidden");
        check(vdOf(et, "identifier").getLabel() != null && "id".equals(vdOf(et, "identifier").getLabel()), "field label independent of rename");
        EventType def = EventType.getEventType(DefaultEvent.class);
        check(DefaultEvent.class.getName().equals(def.getName()), "default name is Class.getName(): " + def.getName());
        check(def.getAnnotation(Name.class) == null, "no @Name -> null readback");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Name.E");
            r.start();
            NamedEvent e = new NamedEvent(); e.id = 77; e.plain = 2; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Name.E", 1);
        RecordedEvent re = firstIn(p, "test.Name.E");
        check(re != null && re.getLong("identifier") == 77, "renamed field readable via consumer");
        check(re != null && "test.Name.E".equals(re.getEventType().getName()), "name survives round-trip");
        // 负例（27 侧实测）：全部非法名被忽略并回退为类二进制名（打 jfr warning，不抛异常）——与 8 侧 EIIE 相反
        String[] bads = {"BadEmptyEvent","BadSpaceEvent","BadLeadEvent","TrailEvent","DoubleEvent"};
        for (String bn : bads) {
            Class<?> bc = Class.forName("NameTest$" + bn);
            EventType be = EventType.getEventType(bc.asSubclass(Event.class));
            check(be != null, bn + " registration tolerated on 27 (no exception)");
            check(bc.getName().equals(be.getName()), bn + " falls back to Class.getName(): " + be.getName());
        }
        // 回退名的事件可正常录制（负例不破坏录制链路）
        Path p2 = tmp();
        try (Recording r = new Recording()) {
            r.enable(BadSpaceEvent.class.getName());
            r.start();
            BadSpaceEvent e = new BadSpaceEvent(); e.id = 1; e.commit();
            r.stop();
            r.dump(p2);
        }
        check(countIn(p2, BadSpaceEvent.class.getName()) == 1, "fallback-named event recordable");
        Files.delete(p2);
        Files.delete(p);
    }

}
