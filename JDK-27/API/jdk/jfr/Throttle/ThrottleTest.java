/*
 * @test
 * @summary jdk.jfr.Throttle conformance: white-box structure + black-box annotation contract.
 * @run main ThrottleTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.Throttle;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class ThrottleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @Throttle("5/s")
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Throttle");
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
        Throttle a = Holder.class.getAnnotation(Throttle.class);
        check(a != null, "annotation applied");
        check("5/s".equals(a.value()), "value round-trip");
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


    @Name("test.Throttle.T2") @Throttle("2/s")
    static class T2Event extends Event { long id; }
    @Name("test.Throttle.Off") @Throttle("off")
    static class OffEvent extends Event { long id; }
    @Name("test.Throttle.Bogus") @Throttle("bogus")
    static class BogusEvent extends Event { long id; }
    @Name("test.Throttle.No")
    static class NoEvent extends Event { long id; }


    static void testDeep() throws Exception {
        Class<?> c = Throttle.class;
        checkMeta(c, "Throttle", true, true);
        checkTarget(c, new String[]{"TYPE"}, "Throttle");
        checkValueSig(c, "String", "off", "Throttle");
        check("throttle".equals(Throttle.NAME), "NAME constant 'throttle'");
        // 与 @Period 不同：@Throttle 会为用户事件创建 throttle setting descriptor（27 实测）
        EventType t2 = EventType.getEventType(T2Event.class);
        EventType off = EventType.getEventType(OffEvent.class);
        EventType no = EventType.getEventType(NoEvent.class);
        check(settingPresent(t2, "throttle"), "@Throttle(\"2/s\") creates throttle descriptor");
        check("2/s".equals(settingDefault(t2, "throttle")), "descriptor default '2/s'");
        check(settingPresent(off, "throttle"), "@Throttle(\"off\") creates throttle descriptor");
        check("off".equals(settingDefault(off, "throttle")), "descriptor default 'off'");
        check(!settingPresent(no, "throttle"), "no annotation -> no throttle descriptor");
        check(settingPresent(no, "enabled") && settingPresent(no, "threshold"), "standard descriptors still present");
        Throttle ann = T2Event.class.getAnnotation(Throttle.class);
        check(ann != null && "2/s".equals(ann.value()), "source annotation value readback");
        // settings 传播：enable 默认不写 throttle 键，显式 with 写入
        try (Recording r = new Recording()) {
            r.enable("test.Throttle.T2");
            check(r.getSettings().get("test.Throttle.T2#throttle") == null, "throttle not in map until explicitly set");
            r.enable("test.Throttle.No").with("throttle", "5/s");
            check("5/s".equals(r.getSettings().get("test.Throttle.No#throttle")), "with(throttle,5/s) propagates to settings map");
        }
        // 负例（27 实测）：非法串注册容忍，descriptor 原样保留 + JVM 打 warning 回退 "off"
        EventType bogus = EventType.getEventType(BogusEvent.class);
        check(bogus != null, "@Throttle(\"bogus\") registration tolerated");
        check("bogus".equals(settingDefault(bogus, "throttle")), "bogus value retained raw in descriptor");
        // 录制闭环：手动 commit 在限速之上不受影响（单条）
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Throttle.T2");
            r.start();
            T2Event e = new T2Event(); e.id = 4; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Throttle.T2", 1);
        RecordedEvent re = firstIn(p, "test.Throttle.T2");
        check(re != null && re.getLong("id") == 4, "field round-trip");
        check(re != null && settingPresent(re.getEventType(), "throttle"), "throttle descriptor survives round-trip");
        Files.delete(p);
    }

}
