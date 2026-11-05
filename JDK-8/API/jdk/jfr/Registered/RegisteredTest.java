/*
 * @test
 * @summary jdk.jfr.Registered conformance: type shape via reflection (headless-safe).
 * @run main RegisteredTest
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

public class RegisteredTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Registered", false, RegisteredTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == true, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        check(hasMethod(c, "value"), "declares value");
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


    @Name("test.Registered.NR") @Registered(false)
    static class NotRegEvent extends Event { long id; }
    @Name("test.Registered.Auto")
    static class AutoEvent extends Event { long id; }
    @Name("test.Registered.False") @Registered(false)
    static class FalseEvent extends Event { long id; }


    static void testDeep() throws Exception {
        Class<?> c = Registered.class;
        checkMeta(c, "Registered", true, false); // 注意：@Registered 本身不带 @MetadataDefinition
        checkTarget(c, new String[]{"TYPE"}, "Registered");
        checkValueSig(c, "boolean", "true", "Registered");
        Registered rf = FalseEvent.class.getAnnotation(Registered.class);
        check(rf != null && rf.value() == false, "@Registered(false) annotation value readback");
        // 默认注册状态机：@Registered(false) 不注册，getEventType 抛 ISE（双侧实测消息）
        check(!typeRegistered("test.Registered.NR"), "@Registered(false) absent from FlightRecorder.getEventTypes()");
        try {
            EventType.getEventType(NotRegEvent.class);
            check(false, "getEventType on unregistered must throw");
        } catch (IllegalStateException ise) {
            check(ise.getMessage() != null && ise.getMessage().contains("not registered"), "ISE message contains 'not registered': " + ise.getMessage());
        }
        FlightRecorder.register(NotRegEvent.class);
        check(typeRegistered("test.Registered.NR"), "register() -> present in getEventTypes()");
        EventType et = EventType.getEventType(NotRegEvent.class);
        check(et != null && "test.Registered.NR".equals(et.getName()), "getEventType resolves after register");
        check(et.getFields().size() == 5, "fields materialize (4 implicit + 1 user)");
        FlightRecorder.unregister(NotRegEvent.class);
        check(!typeRegistered("test.Registered.NR"), "unregister() -> absent again");
        try {
            EventType.getEventType(NotRegEvent.class);
            check(false, "getEventType after unregister must throw");
        } catch (IllegalStateException ise2) {
            check(ise2.getMessage() != null && ise2.getMessage().contains("not registered"), "ISE again after unregister");
        }
        // 重新注册后可录制（闭环）
        FlightRecorder.register(NotRegEvent.class);
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Registered.NR");
            r.start();
            NotRegEvent e = new NotRegEvent(); e.id = 8; e.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Registered.NR", 1);
        // 默认 true：首次解析（getEventType）触发隐式注册
        EventType auto = EventType.getEventType(AutoEvent.class);
        check(auto != null, "default @Registered(true) resolves");
        check(typeRegistered("test.Registered.Auto"), "auto-registered after first resolution");
        check(auto != null && auto.getAnnotation(Registered.class) == null, "no explicit @Registered -> null readback");
        Files.delete(p);
    }

}
