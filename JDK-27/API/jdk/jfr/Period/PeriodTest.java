/*
 * @test
 * @summary jdk.jfr.Period conformance: white-box structure + black-box annotation contract.
 * @run main PeriodTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.Period;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class PeriodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @Period("1 s")
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Period");
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
        Period a = Holder.class.getAnnotation(Period.class);
        check(a != null, "annotation applied");
        check("1 s".equals(a.value()), "value round-trip");
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


    @Name("test.Period.E") @Period("1 s")
    static class PeriodicEvent extends Event { long id; }
    @Name("test.Period.Def") @Period
    static class DefPeriodEvent extends Event { long id; }
    @Name("test.Period.Bogus") @Period("bogus")
    static class BogusEvent extends Event { long id; }


    static void testDeep() throws Exception {
        Class<?> c = Period.class;
        checkMeta(c, "Period", true, true);
        checkTarget(c, new String[]{"TYPE"}, "Period");
        checkValueSig(c, "String", "everyChunk", "Period");
        check("period".equals(Period.NAME), "NAME constant 'period'");
        Period dp = DefPeriodEvent.class.getAnnotation(Period.class);
        check(dp != null && "everyChunk".equals(dp.value()), "explicit @Period default 'everyChunk'");
        // 用户事件不暴露 period setting descriptor（双侧实测）；jdk.* 周期事件才暴露
        EventType et = EventType.getEventType(PeriodicEvent.class);
        check(!settingPresent(et, "period"), "user event has no period setting descriptor");
        check(settingPresent(et, "enabled") && settingPresent(et, "threshold"), "enabled/threshold descriptors exist");
        EventType cpuload = null;
        for (EventType t : FlightRecorder.getFlightRecorder().getEventTypes())
            if (t.getName().equals("jdk.CPULoad")) cpuload = t;
        check(cpuload != null, "jdk.CPULoad event type present");
        if (cpuload != null) {
            check(settingPresent(cpuload, "period"), "jdk.CPULoad exposes period descriptor");
            check("everyChunk".equals(settingDefault(cpuload, "period")), "jdk.CPULoad period default 'everyChunk'");
            check("false".equals(settingDefault(cpuload, "enabled")), "jdk.CPULoad enabled default 'false'");
        }
        // settings 传播闭环：注解默认值 + 显式 with 覆盖
        try (Recording r = new Recording()) {
            EventSettings es = r.enable("test.Period.E");
            check(es != null, "enable returns settings");
            check(r.getSettings().get("test.Period.E#period") == null, "period not in map until explicitly set");
            r.enable("test.Period.E").with("period", "1 s");
            check("1 s".equals(r.getSettings().get("test.Period.E#period")), "with(period,1 s) propagates to recording settings");
            r.enable("test.Period.E").with("period", "everyChunk");
            check("everyChunk".equals(r.getSettings().get("test.Period.E#period")), "with(period,everyChunk) propagates");
        }
        // 负例（双侧实测）：非法串注册被容忍；用户事件 period 设置对手动 commit 无自动触发语义
        EventType bogus = EventType.getEventType(BogusEvent.class);
        check(bogus != null, "@Period(\"bogus\") registration tolerated");
        check(!settingPresent(bogus, "period"), "bogus period still no descriptor for user event");
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Period.E").with("period", "1 s");
            r.start();
            PeriodicEvent e = new PeriodicEvent(); e.id = 3; e.commit();
            r.stop();
            r.dump(p);
        }
        check(countIn(p, "test.Period.E") == 1, "manual commit recorded exactly once (period does not auto-emit user events)");
        Files.delete(p);
    }

}
