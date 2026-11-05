/*
 * @test
 * @summary jdk.jfr.AnnotationElement conformance: type shape via reflection (headless-safe).
 * @run main AnnotationElementTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.util.*;

public class AnnotationElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.AnnotationElement", false, AnnotationElementTest.class.getClassLoader());
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
        check(hasMethod(c, "getTypeId"), "declares getTypeId");
        check(hasMethod(c, "getTypeName"), "declares getTypeName");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "getValueDescriptors"), "declares getValueDescriptors");
        check(hasMethod(c, "getValues"), "declares getValues");
        check(hasMethod(c, "hasValue"), "declares hasValue");
        testDeepConstruction();
        testDeepEquality();
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

    static void testDeepConstruction() {
        AnnotationElement ae = new AnnotationElement(Label.class, "L");
        check("jdk.jfr.Label".equals(ae.getTypeName()), "ae typeName jdk.jfr.Label");
        check(ae.getTypeId() > 0, "ae typeId > 0");
        check("L".equals(ae.getValue("value")), "ae getValue(value)");
        check(ae.hasValue("value"), "ae hasValue(value) true");
        check(!ae.hasValue("other"), "ae hasValue(other) false");
        List<Object> vals = ae.getValues();
        check(vals.size() == 1 && "L".equals(vals.get(0)), "ae getValues [L]");
        check(ae.getAnnotationElements().isEmpty(), "ae Label has no meta annotation elements");
        check(ae.getAnnotation(Label.class) == null, "ae getAnnotation(Label) null (not self-referential)");
        // Map constructor
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("value", "M");
        AnnotationElement am = new AnnotationElement(Label.class, m);
        check("M".equals(am.getValue("value")), "ae map ctor value roundtrip");
        check(am.getValues().size() == 1, "ae map ctor single value");
        // value type is not validated at construction time
        AnnotationElement ai = new AnnotationElement(Label.class, 5);
        check(ai.getValues().size() == 1 && Integer.valueOf(5).equals(ai.getValues().get(0)), "ae int value stored verbatim (no type validation)");
        check(ae.toString() != null, "ae toString non-null");
    }

    static void testDeepEquality() {
        AnnotationElement a = new AnnotationElement(Label.class, "L");
        AnnotationElement b = new AnnotationElement(Label.class, "L");
        // Empirical (8+27 identical): no value-based equals; two fresh elements are not equal
        check(a.equals(a), "ae equals self");
        check(!a.equals(b), "ae fresh instances not value-equal (empirical contract)");
        check(a.hashCode() == a.hashCode(), "ae hashCode stable across calls");
        check(!a.equals("L"), "ae not equals foreign type");
    }

    static void testDeepNegatives() {
        expectEx("ae null class", NullPointerException.class, null, () -> new AnnotationElement(null, "x"));
        expectEx("ae single-arg Name (has value member)", IllegalArgumentException.class,
                 "Number of declared methods must match size of value map", () -> new AnnotationElement(Name.class));
        AnnotationElement ae = new AnnotationElement(Label.class, "L");
        expectEx("ae getValue(missing)", IllegalArgumentException.class,
                 "No value with name 'x'", () -> ae.getValue("x"));
    }

    static void testDeepRoundtripThroughFactory() {
        List<AnnotationElement> anns = new ArrayList<AnnotationElement>();
        anns.add(new AnnotationElement(Name.class, "test.AnnotationElement.F"));
        anns.add(new AnnotationElement(Label.class, "AEL"));
        List<ValueDescriptor> fields = new ArrayList<ValueDescriptor>();
        fields.add(new ValueDescriptor(String.class, "s"));
        EventFactory f = EventFactory.create(anns, fields);
        EventType t = f.getEventType();
        boolean nameOk = false, labelOk = false;
        for (AnnotationElement ae : t.getAnnotationElements()) {
            if ("jdk.jfr.Name".equals(ae.getTypeName()) && "test.AnnotationElement.F".equals(ae.getValue("value"))) nameOk = true;
            if ("jdk.jfr.Label".equals(ae.getTypeName()) && "AEL".equals(ae.getValue("value"))) labelOk = true;
        }
        check(nameOk, "ae Name roundtrips through EventFactory EventType");
        check(labelOk, "ae Label roundtrips through EventFactory EventType");
        check("test.AnnotationElement.F".equals(t.getName()), "ae factory name derived from Name element");
        check("AEL".equals(t.getLabel()), "ae factory label derived from Label element");
    }
}
