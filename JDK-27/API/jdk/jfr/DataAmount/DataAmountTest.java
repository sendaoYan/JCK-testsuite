/*
 * @test
 * @summary jdk.jfr.DataAmount conformance: white-box structure + black-box annotation contract.
 * @run main DataAmountTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.DataAmount;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class DataAmountTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @DataAmount("1 B")
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.DataAmount");
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
        DataAmount a = Holder.class.getAnnotation(DataAmount.class);
        check(a != null, "annotation applied");
        check("1 B".equals(a.value()), "value round-trip");
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


    @Name("test.DataAmount.E") @DataAmount(DataAmount.BYTES) @Label("DA")
    static class DAEvent extends Event {
        @DataAmount(DataAmount.BYTES) @Label("bytes") long bytes;
        @DataAmount(DataAmount.BITS) long bits;
        @DataAmount("bogus") long bogus;
        long plain;
    }


    static void testDeep() throws Exception {
        Class<?> c = DataAmount.class;
        checkMeta(c, "DataAmount", false, true);
        checkTarget(c, new String[]{"FIELD","TYPE","METHOD"}, "DataAmount");
        checkValueSig(c, "String", "BYTES", "DataAmount");
        check(DataAmount.class.getAnnotation(ContentType.class) != null, "carries @ContentType");
        Set<String> consts = new HashSet<String>(java.util.Arrays.asList("BITS","BYTES"));
        Set<String> got = new HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) got.add(f.getName());
        check(got.equals(consts), "constant set exactly {BITS,BYTES}: " + got);
        check("BYTES".equals(DataAmount.BYTES) && "BITS".equals(DataAmount.BITS), "constant values");
        EventType et = EventType.getEventType(DAEvent.class);
        check(et.getAnnotation(DataAmount.class) != null && "BYTES".equals(et.getAnnotation(DataAmount.class).value()), "type-level readback");
        check(hasAnnElem(et.getAnnotationElements(), "jdk.jfr.DataAmount"), "type elements contains DataAmount");
        check("BYTES".equals(vdOf(et, "bytes").getAnnotation(DataAmount.class).value()), "field BYTES readback");
        check("BITS".equals(vdOf(et, "bits").getAnnotation(DataAmount.class).value()), "field BITS readback");
        check(vdOf(et, "plain").getAnnotation(DataAmount.class) == null, "unannotated field null");
        check("bogus".equals(vdOf(et, "bogus").getAnnotation(DataAmount.class).value()), "bogus value retained raw, no exception");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.DataAmount.E");
            r.start();
            DAEvent e = new DAEvent(); e.bytes = 4096; e.bits = 8; e.bogus = 1; e.plain = 2; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.DataAmount.E", 1);
        RecordedEvent re = firstIn(p, "test.DataAmount.E");
        check(re != null && re.getLong("bytes") == 4096, "value round-trip");
        check(re != null && "BYTES".equals(vdOf(re.getEventType(), "bytes").getAnnotation(DataAmount.class).value()), "DataAmount survives round-trip");
        Files.delete(p);
    }

}
