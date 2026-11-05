/*
 * @test
 * @summary java.lang.Deprecated conformance: white-box structure + black-box behavior
 *          (RUNTIME retention, METHOD target, runtime presence, Java 9+ since()/forRemoval()).
 * @run main DeprecatedTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class DeprecatedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Deprecated
    static void old() {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        Class<?> c = Deprecated.class;
        check(c.isAnnotation(), "Deprecated is an annotation type");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "RUNTIME retention");
        boolean hasMethod = false, hasType = false;
        for (ElementType e : c.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
            if (e == ElementType.TYPE) hasType = true;
        }
        check(hasMethod && hasType, "@Target includes METHOD and TYPE");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPresent() throws Exception {
        check(DeprecatedTest.class.getDeclaredMethod("old").isAnnotationPresent(Deprecated.class),
              "@Deprecated retained at runtime on a method");
    }

    static void testSinceForRemoval() {
        try {
            java.lang.reflect.Method since = Deprecated.class.getMethod("since");
            java.lang.reflect.Method fr = Deprecated.class.getMethod("forRemoval");
            check(since.getReturnType() == String.class, "since() returns String [Java 9+]");
            check(fr.getReturnType() == boolean.class, "forRemoval() returns boolean [Java 9+]");
        } catch (NoSuchMethodException e) {
            // JDK 8: since()/forRemoval() are absent.
        }
    }


    static void testBlackBox() throws Exception {
        testPresent();
        testSinceForRemoval();
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    @Deprecated static class OldType {}
    @Deprecated static int oldField;
    @Deprecated DeprecatedTest() {}
    static void paramSite(@Deprecated int p) {}
    @Deprecated(since = "9", forRemoval = true) static void gone() {}
    @Deprecated(since = "1.2") static void legacy() {}
    @Deprecated() static void explicitEmpty() {}

    static void testMembersAndDefaults() throws Exception {
        check(Deprecated.class.getDeclaredMethods().length == 2,
              "exactly two members since()/forRemoval()");
        java.lang.reflect.Method since = Deprecated.class.getMethod("since");
        java.lang.reflect.Method fr = Deprecated.class.getMethod("forRemoval");
        check("".equals(since.getDefaultValue()), "since() default is empty string");
        check(Boolean.FALSE.equals(fr.getDefaultValue()), "forRemoval() default is false");
        check(since.getDefaultValue() != null, "since() default non-null (no optional members)");
        check(Deprecated.class.getDeclaredFields().length == 0, "no fields");
        check(Deprecated.class.isAnnotation(), "is an annotation type");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(Deprecated.class),
              "extends java.lang.annotation.Annotation");
    }

    static void testSinceForRemovalRoundTrip() throws Exception {
        Deprecated g = DeprecatedTest.class.getDeclaredMethod("gone").getAnnotation(Deprecated.class);
        check(g != null, "custom since/forRemoval annotation retained at runtime");
        check("9".equals(g.since()), "since() reads back assigned value");
        check(g.forRemoval(), "forRemoval() reads back assigned value");
        check("@java.lang.Deprecated(forRemoval=true, since=\"9\")".equals(g.toString()),
              "membered toString exact: " + g.toString());
        Deprecated l = DeprecatedTest.class.getDeclaredMethod("legacy").getAnnotation(Deprecated.class);
        check("1.2".equals(l.since()), "second custom since value");
        check(!l.forRemoval(), "forRemoval defaults to false when omitted");
        Deprecated d = DeprecatedTest.class.getDeclaredMethod("old").getAnnotation(Deprecated.class);
        check("".equals(d.since()), "bare @Deprecated: since() default empty string");
        check(!d.forRemoval(), "bare @Deprecated: forRemoval() default false");
        check("@java.lang.Deprecated(forRemoval=false, since=\"\")".equals(d.toString()),
              "all-defaults toString exact: " + d.toString());
        check(d.annotationType() == Deprecated.class, "annotationType() == Deprecated.class");
        check(d.equals(d), "equals self");
        check(!d.equals(g), "different member values are not equal");
        check(d.hashCode() == DeprecatedTest.class.getDeclaredMethod("old")
                  .getAnnotation(Deprecated.class).hashCode(),
              "equal lookups share hashCode");
    }

    static void testTargetExactValueSet() {
        ElementType[] ts = Deprecated.class.getAnnotation(Target.class).value();
        check(ts.length == 8, "@Target has exactly 8 elements (MODULE added in 9), got " + ts.length);
        java.util.EnumSet<ElementType> set = java.util.EnumSet.noneOf(ElementType.class);
        for (ElementType t : ts) set.add(t);
        check(set.contains(ElementType.CONSTRUCTOR), "target CONSTRUCTOR");
        check(set.contains(ElementType.FIELD), "target FIELD");
        check(set.contains(ElementType.LOCAL_VARIABLE), "target LOCAL_VARIABLE");
        check(set.contains(ElementType.METHOD), "target METHOD");
        check(set.contains(ElementType.PACKAGE), "target PACKAGE");
        check(set.contains(ElementType.PARAMETER), "target PARAMETER");
        check(set.contains(ElementType.TYPE), "target TYPE");
        check(set.contains(ElementType.MODULE), "target MODULE (9+)");
        check(set.contains(ElementType.RECORD_COMPONENT) == false,
              "no RECORD_COMPONENT target");
    }

    static void testDocumentedMeta() {
        check(Deprecated.class.isAnnotationPresent(java.lang.annotation.Documented.class),
              "@Documented present");
        check(Deprecated.class.getAnnotation(java.lang.annotation.Inherited.class) == null,
              "@Inherited absent");
        check(Deprecated.class.getAnnotations().length == 3,
              "three runtime-visible meta-annotations (Documented, Retention, Target)");
    }

    static void testMultiTargetUsage() throws Exception {
        check(OldType.class.isAnnotationPresent(Deprecated.class), "usable on TYPE");
        check(DeprecatedTest.class.getDeclaredMethod("old").getAnnotation(Deprecated.class) != null,
              "usable on METHOD");
        check(DeprecatedTest.class.getDeclaredField("oldField").getAnnotation(Deprecated.class) != null,
              "usable on FIELD");
        check(DeprecatedTest.class.getDeclaredConstructor().getAnnotation(Deprecated.class) != null,
              "usable on CONSTRUCTOR");
        check(DeprecatedTest.class.getDeclaredMethod("paramSite", int.class)
                  .getParameterAnnotations()[0].length == 1,
              "usable on PARAMETER");
    }

    static void testStructuralExtras() {
        check(Deprecated.class.getName().equals("java.lang.Deprecated"), "binary name");
        check(Deprecated.class.isInterface(), "annotation type is an interface");
        check(Deprecated.class.getDeclaredClasses().length == 0, "no nested classes");
        check(Deprecated.class.getAnnotation(Deprecated.class) == null,
              "Deprecated is not itself @Deprecated");
    }

    static void testRealJdkApiPresence() throws Exception {
        check(java.util.Date.class.getMethod("getDate").isAnnotationPresent(Deprecated.class),
              "java.util.Date.getDate() is @Deprecated");
        Deprecated ni = Class.class.getMethod("newInstance").getAnnotation(Deprecated.class);
        check(ni != null, "Class.newInstance() is @Deprecated on modern JDKs");
        check("9".equals(ni.since()), "Class.newInstance since == \"9\"");
        check(!ni.forRemoval(), "Class.newInstance forRemoval == false");
        Deprecated fin = Object.class.getDeclaredMethod("finalize").getAnnotation(Deprecated.class);
        check(fin != null, "Object.finalize() is @Deprecated");
        check(fin.forRemoval(), "Object.finalize forRemoval == true (removal planned)");
        check("9".equals(fin.since()), "Object.finalize since == \"9\"");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMembersAndDefaults();
        testSinceForRemovalRoundTrip();
        testTargetExactValueSet();
        testDocumentedMeta();
        testMultiTargetUsage();
        testStructuralExtras();
        testRealJdkApiPresence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
