/*
 * @test
 * @summary jdk.jfr.ValueDescriptor conformance: type shape via reflection (headless-safe).
 * @run main ValueDescriptorTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.util.*;

public class ValueDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.ValueDescriptor", false, ValueDescriptorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getAnnotation"), "declares getAnnotation");
        check(hasMethod(c, "getAnnotationElements"), "declares getAnnotationElements");
        check(hasMethod(c, "getContentType"), "declares getContentType");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getFields"), "declares getFields");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getTypeId"), "declares getTypeId");
        testDeepPlainConstruction();
        testDeepAnnotatedConstruction();
        testDeepNegatives();
        testDeepRoundtripThroughFactory();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static void expectEx(String what, Class<? extends Throwable> type, String contains, Runnable r) {
        try { r.run(); check(false, what + ": expected " + type.getSimpleName()); }
        catch (Throwable t) {
            check(type.isInstance(t), what + ": expected " + type.getSimpleName() + " got " + t.getClass().getName() + " (" + t.getMessage() + ")");
            check(contains == null || String.valueOf(t.getMessage()).contains(contains), what + ": msg '" + t.getMessage() + "' lacks '" + contains + "'");
        }
    }

    @jdk.jfr.Name("test.ValueDescriptor.Init")
    static class VDEvent extends jdk.jfr.Event { long x; }

    static void testDeepPlainConstruction() throws Exception {
        // keep the same init-first ordering as the JDK-27 test (Thread type
        // description is ordering-sensitive there; harmless on JDK-8)
        jdk.jfr.EventType.getEventType(VDEvent.class);
        ValueDescriptor vd = new ValueDescriptor(String.class, "n");
        check("n".equals(vd.getName()), "vd name roundtrip");
        check("java.lang.String".equals(vd.getTypeName()), "vd typeName java.lang.String");
        check(vd.getLabel() == null, "vd label null without @Label");
        check(vd.getDescription() == null, "vd description null without @Description");
        check(vd.getContentType() == null, "vd contentType null without @ContentType");
        check(vd.getTypeId() > 0, "vd typeId > 0");
        check(!vd.isArray(), "vd not array");
        check(vd.getFields().isEmpty(), "vd String has no child fields");
        check(vd.getAnnotation(Label.class) == null, "vd getAnnotation(Label) null without annotation");
        check(vd.getAnnotationElements().isEmpty(), "vd annotationElements empty");
        // primitive types
        ValueDescriptor iv = new ValueDescriptor(int.class, "i");
        check("int".equals(iv.getTypeName()), "vd primitive typeName 'int'");
        check(iv.getTypeId() > 0, "vd primitive typeId > 0");
        ValueDescriptor lv = new ValueDescriptor(long.class, "l");
        check("long".equals(lv.getTypeName()), "vd primitive typeName 'long'");
        // Thread is an allowed structured type (version diff: JDK-8 exposes no child fields for it)
        ValueDescriptor tv = new ValueDescriptor(Thread.class, "th");
        check("java.lang.Thread".equals(tv.getTypeName()), "vd Thread typeName");
        check(tv.getFields().isEmpty(), "vd Thread has no child fields on JDK-8");
    }

    static void testDeepAnnotatedConstruction() {
        List<AnnotationElement> anns = new ArrayList<AnnotationElement>();
        anns.add(new AnnotationElement(Label.class, "LN"));
        anns.add(new AnnotationElement(Description.class, "DN"));
        ValueDescriptor vd = new ValueDescriptor(String.class, "n", anns);
        check("LN".equals(vd.getLabel()), "vd label from @Label element");
        check("DN".equals(vd.getDescription()), "vd description from @Description element");
        check(vd.getAnnotation(Label.class) != null && "LN".equals(vd.getAnnotation(Label.class).value()), "vd getAnnotation(Label) roundtrip");
        check(vd.getAnnotationElements().size() == 2, "vd two annotation elements");
        // empirical (8+27 identical): a null annotation list is rejected with NPE
        expectEx("vd null annotation list", NullPointerException.class, null,
                 () -> new ValueDescriptor(String.class, "n2", null));
    }

    static void testDeepNegatives() {
        expectEx("vd null name", NullPointerException.class, null, () -> new ValueDescriptor(String.class, null));
        expectEx("vd null type", NullPointerException.class, null, () -> new ValueDescriptor(null, "n"));
        expectEx("vd unsupported type", IllegalArgumentException.class,
                 "Only primitive types, java.lang.Thread, java.lang.String and java.lang.Class are allowed",
                 () -> new ValueDescriptor(Object.class, "n"));
        // version diff: JDK-8 tolerates an empty name; JDK-27+ rejects it as invalid Java identifier
        try { new ValueDescriptor(String.class, ""); check(true, "vd empty name tolerated on JDK-8"); }
        catch (Throwable t) { check(false, "vd empty name should be tolerated on JDK-8, got " + t); }
    }

    static void testDeepRoundtripThroughFactory() {
        List<AnnotationElement> anns = new ArrayList<AnnotationElement>();
        anns.add(new AnnotationElement(Name.class, "test.ValueDescriptor.F"));
        List<ValueDescriptor> fields = new ArrayList<ValueDescriptor>();
        List<AnnotationElement> fieldAnns = new ArrayList<AnnotationElement>();
        fieldAnns.add(new AnnotationElement(Label.class, "FieldLabel"));
        fields.add(new ValueDescriptor(long.class, "num", fieldAnns));
        EventFactory f = EventFactory.create(anns, fields);
        EventType t = f.getEventType();
        ValueDescriptor vd = t.getField("num");
        check(vd != null, "vd factory field present");
        check("FieldLabel".equals(vd.getLabel()), "vd factory field label roundtrip");
        check("long".equals(vd.getTypeName()), "vd factory field primitive typeName");
        check(!vd.isArray(), "vd factory field not array");
        check(vd.getAnnotation(Label.class) != null && "FieldLabel".equals(vd.getAnnotation(Label.class).value()), "vd factory field annotation proxy");
        // implicit fields of any event type are ValueDescriptors too
        ValueDescriptor start = t.getField("startTime");
        check(start != null && "startTime".equals(start.getName()), "vd implicit startTime descriptor");
        check(t.getField("duration").getTypeId() > 0, "vd implicit duration typeId > 0");
        check(t.getField("eventThread") != null && t.getField("stackTrace") != null, "vd implicit eventThread/stackTrace present");
    }
}
