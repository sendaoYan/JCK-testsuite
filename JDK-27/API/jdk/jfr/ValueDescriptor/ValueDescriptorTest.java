/*
 * @test
 * @summary jdk.jfr.ValueDescriptor conformance: white-box structure + black-box construction/read.
 * @run main ValueDescriptorTest
 */

import jdk.jfr.*;
import java.util.List;
import java.lang.reflect.*;
import java.util.*;

public class ValueDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.ValueDescriptor");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getTypeName"), "getTypeName");
        check(hasMethod(c, "getLabel"), "getLabel");
        check(hasMethod(c, "isArray"), "isArray");
        check(hasMethod(c, "getFields"), "getFields");
    }

    static void testBlackBox() {
        ValueDescriptor vd = new ValueDescriptor(String.class, "name", List.of(new AnnotationElement(Label.class, "Name")));
        check("name".equals(vd.getName()), "name");
        check("java.lang.String".equals(vd.getTypeName()), "type name");
        check("Name".equals(vd.getLabel()), "label");
        check(vd.getAnnotationElements() != null && vd.getAnnotationElements().size() >= 1, "annotation elements");
        check(!vd.isArray(), "not array");
        check(vd.getFields() != null, "fields list");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

    @Name("test.ValueDescriptor.Init")
    static class VDEvent extends Event { long x; }

    static void testDeepPlainConstruction() throws Exception {
        // initialize JFR metadata before describing the structured Thread type:
        // creating a Thread ValueDescriptor pre-init leaves the type library
        // uninitialized-then-poisoned on JDK-27 (InternalError on later init)
        EventType.getEventType(VDEvent.class);
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
        // Thread is an allowed structured type (version diff: JDK-27 expands its child fields)
        ValueDescriptor tv = new ValueDescriptor(Thread.class, "th");
        check("java.lang.Thread".equals(tv.getTypeName()), "vd Thread typeName");
        check(!tv.getFields().isEmpty(), "vd Thread exposes child fields on JDK-27, got " + tv.getFields().size());
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
        expectEx("vd null name", NullPointerException.class, "name", () -> new ValueDescriptor(String.class, null));
        expectEx("vd null type", NullPointerException.class, "type", () -> new ValueDescriptor(null, "n"));
        expectEx("vd unsupported type", IllegalArgumentException.class,
                 "Only primitive types, java.lang.Thread, java.lang.String and java.lang.Class are allowed",
                 () -> new ValueDescriptor(Object.class, "n"));
        // version diff: JDK-27 rejects an empty name as an invalid Java identifier (JDK-8 tolerated it)
        expectEx("vd empty name", IllegalArgumentException.class,
                 "is not a valid Java identifier", () -> new ValueDescriptor(String.class, ""));
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
