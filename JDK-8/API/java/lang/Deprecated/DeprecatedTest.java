/*
 * @test
 * @summary java.lang.Deprecated API conformance: RUNTIME retention, METHOD target, runtime
 *          presence on a deprecated method, and the Java 9+ since()/forRemoval() members
 *          (via reflection).
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

    static void testRetentionAndTarget() {
        check(Deprecated.class.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "RUNTIME retention");
        boolean hasMethod = false;
        for (ElementType e : Deprecated.class.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
        }
        check(hasMethod, "@Target includes METHOD");
    }

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

    // ==== Contract deepening (lcN1, 2026-11) ====
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    @Deprecated static class OldType {}
    @Deprecated static int oldField;
    @Deprecated DeprecatedTest() {}
    static void paramSite(@Deprecated int p) {}

    static void testMembersAbsent() throws Exception {
        // javap on JDK-8 shows java.lang.Deprecated declares no members at all
        check(Deprecated.class.getDeclaredMethods().length == 0,
              "JDK-8 Deprecated declares zero members (javap-confirmed)");
        check(Deprecated.class.getDeclaredFields().length == 0, "no fields");
        check(Deprecated.class.isAnnotation(), "is an annotation type");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(Deprecated.class),
              "extends java.lang.annotation.Annotation");
    }

    static void testTargetExactValueSet() {
        ElementType[] ts = Deprecated.class.getAnnotation(Target.class).value();
        check(ts.length == 7, "JDK-8 @Target has exactly 7 elements, got " + ts.length);
        java.util.EnumSet<ElementType> set = java.util.EnumSet.noneOf(ElementType.class);
        for (ElementType t : ts) set.add(t);
        check(set.contains(ElementType.CONSTRUCTOR), "target CONSTRUCTOR");
        check(set.contains(ElementType.FIELD), "target FIELD");
        check(set.contains(ElementType.LOCAL_VARIABLE), "target LOCAL_VARIABLE");
        check(set.contains(ElementType.METHOD), "target METHOD");
        check(set.contains(ElementType.PACKAGE), "target PACKAGE");
        check(set.contains(ElementType.PARAMETER), "target PARAMETER");
        check(set.contains(ElementType.TYPE), "target TYPE");
        boolean hasModule = false;
        try { hasModule = set.contains(ElementType.valueOf("MODULE")); }
        catch (IllegalArgumentException absentIn8) { }
        check(!hasModule, "JDK-8 target lacks MODULE (added in 9)");
        check(!set.contains(ElementType.ANNOTATION_TYPE), "target lacks ANNOTATION_TYPE");
    }

    static void testDocumentedMeta() {
        check(Deprecated.class.isAnnotationPresent(java.lang.annotation.Documented.class),
              "@Documented present");
        check(Deprecated.class.getAnnotation(java.lang.annotation.Inherited.class) == null,
              "@Inherited absent");
        // RUNTIME retention: the meta-annotations themselves are reflected
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

    static void testAnnotationInstanceContract() throws Exception {
        Deprecated d = DeprecatedTest.class.getDeclaredMethod("old").getAnnotation(Deprecated.class);
        check(d != null, "RUNTIME retention gives a live instance");
        check(d.annotationType() == Deprecated.class, "annotationType() == Deprecated.class");
        check(d.equals(d), "equals self");
        Deprecated d2 = DeprecatedTest.class.getDeclaredMethod("old").getAnnotation(Deprecated.class);
        check(d.equals(d2), "two lookups yield equal proxy instances");
        check(d.hashCode() == d2.hashCode(), "equal instances share hashCode");
        check(d.hashCode() == 0,
              "memberless annotation proxy hashCode == 0");
        check("@java.lang.Deprecated()".equals(d.toString()),
              "memberless toString exact: " + d.toString());
        check(Deprecated.class.getAnnotation(java.lang.annotation.Retention.class).value()
                  == java.lang.annotation.RetentionPolicy.RUNTIME,
              "RUNTIME retention re-checked");
    }

    @Deprecated() static void explicitEmpty() {}

    static void testStructuralExtras() {
        check(Deprecated.class.getName().equals("java.lang.Deprecated"), "binary name");
        check(Deprecated.class.isInterface(), "annotation type is an interface");
        check(Deprecated.class.getDeclaredClasses().length == 0, "no nested classes");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(Deprecated.class),
              "assignable to Annotation");
        check(Deprecated.class.getAnnotation(Deprecated.class) == null,
              "Deprecated is not itself @Deprecated");
    }

    static void testRealJdkApiPresence() throws Exception {
        // stable cross-version example: java.util.Date accessors are @Deprecated
        check(java.util.Date.class.getMethod("getDate").isAnnotationPresent(Deprecated.class),
              "java.util.Date.getDate() is @Deprecated");
        check(DeprecatedTest.class.getDeclaredMethod("explicitEmpty")
                  .isAnnotationPresent(Deprecated.class),
              "explicit empty-value form @Deprecated() compiles and retains");
        // JDK-8-only history: these were not yet annotated in 8
        check(!Object.class.getDeclaredMethod("finalize").isAnnotationPresent(Deprecated.class),
              "JDK-8 Object.finalize carries no @Deprecated annotation");
        check(!Class.class.getMethod("newInstance").isAnnotationPresent(Deprecated.class),
              "JDK-8 Class.newInstance carries no @Deprecated annotation");
    }

    public static void main(String[] args) throws Exception {
        testRetentionAndTarget();
        testPresent();
        testSinceForRemoval();
        testMembersAbsent();
        testTargetExactValueSet();
        testDocumentedMeta();
        testMultiTargetUsage();
        testAnnotationInstanceContract();
        testStructuralExtras();
        testRealJdkApiPresence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
