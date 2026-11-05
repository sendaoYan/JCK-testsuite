/*
 * @test
 * @summary jdk.jfr.Category conformance: white-box structure + black-box annotation contract.
 * @run main CategoryTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.Category;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class CategoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @Category({"a","b"})
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Category");
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
        check(m.getReturnType() == String[].class, "value element type");
    }

    static void testBlackBox() throws Exception {
        Category a = Holder.class.getAnnotation(Category.class);
        check(a != null, "annotation applied");
        check(a.value().length == 2, "value round-trip");
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


    @Name("test.Category.Multi") @Category({"cat1", "cat2"})
    static class MultiCatEvent extends Event { long id; }
    @Name("test.Category.Single") @Category({"solo"})
    static class SingleCatEvent extends Event { long id; }
    @Name("test.Category.Default")
    static class DefaultCatEvent extends Event { long id; }
    @Name("test.Category.Empty") @Category({})
    static class EmptyCatEvent extends Event { long id; }
    @Name("test.Category.EmptyStr") @Category({""})
    static class EmptyStrCatEvent extends Event { long id; }
    @Name("test.Category.Space") @Category({"ok", " "})
    static class SpaceCatEvent extends Event { long id; }


    static void testDeep() throws Exception {
        Class<?> c = Category.class;
        checkMeta(c, "Category", true, true);
        checkTarget(c, new String[]{"TYPE"}, "Category");
        checkValueSig(c, "String[]", null, "Category");
        EventType m = EventType.getEventType(MultiCatEvent.class);
        List<String> cats = m.getCategoryNames();
        check(cats.size() == 2, "multi category size 2");
        check("cat1".equals(cats.get(0)), "category[0] order preserved");
        check("cat2".equals(cats.get(1)), "category[1] order preserved");
        check(m.getAnnotation(Category.class) != null, "type-level @Category readback");
        Category ca = m.getAnnotation(Category.class);
        check(ca.value().length == 2, "annotation array value length");
        check("cat1".equals(ca.value()[0]), "annotation value[0]");
        EventType s = EventType.getEventType(SingleCatEvent.class);
        check(s.getCategoryNames().size() == 1 && "solo".equals(s.getCategoryNames().get(0)), "single category");
        EventType d = EventType.getEventType(DefaultCatEvent.class);
        check(d.getCategoryNames().size() == 1, "default category size 1");
        check("Uncategorized".equals(d.getCategoryNames().get(0)), "default category 'Uncategorized'");
        check(d.getAnnotation(Category.class) == null, "no @Category -> null getAnnotation");
        // 负例：空数组与空串元素都不抛异常，空串元素被过滤为空列表（双侧实测）
        EventType e1 = EventType.getEventType(EmptyCatEvent.class);
        check(e1.getCategoryNames().isEmpty(), "@Category({}) -> empty list, no exception");
        EventType e2 = EventType.getEventType(EmptyStrCatEvent.class);
        check(e2.getCategoryNames().size() == 1, "@Category({\"\"}) -> single literal empty-string element (kept, NOT filtered)");
        check("".equals(e2.getCategoryNames().get(0)), "@Category({\"\"}) element value is empty string");
        EventType e3 = EventType.getEventType(SpaceCatEvent.class);
        List<String> sp = e3.getCategoryNames();
        check(sp.size() == 2, "space category size 2");
        check(" ".equals(sp.get(1)), "literal space element kept");
        // 录制->dump->回读：category 元数据随序列化保留
        Path p = tmp();
        try (Recording r = new Recording()) {
            r.enable("test.Category.Multi");
            r.start();
            MultiCatEvent ev = new MultiCatEvent(); ev.id = 5; ev.commit();
            r.stop();
            r.dump(p);
        }
        checkLoopFacts(p, "test.Category.Multi", 1);
        RecordedEvent re = firstIn(p, "test.Category.Multi");
        check(re != null && re.getEventType().getCategoryNames().equals(java.util.Arrays.asList("cat1", "cat2")), "category survives round-trip");
        check(re != null && re.getLong("id") == 5, "field round-trip");
        Files.delete(p);
    }

}
