/*
 * @test
 * @summary java.lang.SuppressWarnings conformance: white-box structure + black-box behavior
 *          (SOURCE retention, TYPE target, value() returns String[]).
 * @run main SuppressWarningsTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SuppressWarningsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        Class<?> c = SuppressWarnings.class;
        check(c.isAnnotation(), "SuppressWarnings is an annotation type");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.SOURCE,
              "SOURCE retention");
        // SuppressWarnings carries no @Target meta-annotation: it applies to
        // TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, MODULE.
        check(c.getAnnotation(Target.class) == null, "no @Target restriction");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testValueType() throws Exception {
        check(SuppressWarnings.class.getMethod("value").getReturnType() == String[].class,
              "value() returns String[]");
    }


    static void testBlackBox() throws Exception {
        testValueType();
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    // Measured fact (both JDK-8 and JDK-27): SuppressWarnings is SOURCE-retained.
    // Any @SuppressWarnings site is therefore invisible at runtime; the checks
    // below lock that in and contrast it with a self-made RUNTIME annotation.
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface RuntimeVisible { String value(); }

    @SuppressWarnings("unchecked") static class SwType {}
    @SuppressWarnings({"unused", "deprecation"}) static int swField;
    @SuppressWarnings("unchecked") static void swMethod() {}
    @SuppressWarnings("rawtypes") SuppressWarningsTest() {}
    static void swParam(@SuppressWarnings("cast") int p) {}
    @SuppressWarnings("fallthrough") static void swSingle() {}
    @RuntimeVisible("x") static void rvMethod() {}

    static void testRuntimeInvisibility() throws Exception {
        check(SwType.class.getAnnotation(SuppressWarnings.class) == null,
              "TYPE site invisible at runtime (SOURCE retention)");
        check(SuppressWarningsTest.class.getDeclaredMethod("swMethod")
                  .getAnnotation(SuppressWarnings.class) == null,
              "METHOD site invisible");
        check(SuppressWarningsTest.class.getDeclaredField("swField")
                  .getAnnotation(SuppressWarnings.class) == null,
              "FIELD site invisible");
        check(SuppressWarningsTest.class.getDeclaredConstructor()
                  .getAnnotation(SuppressWarnings.class) == null,
              "CONSTRUCTOR site invisible");
        check(SuppressWarningsTest.class.getDeclaredMethod("swParam", int.class)
                  .getParameterAnnotations()[0].length == 0,
              "PARAMETER site invisible");
        check(!SwType.class.isAnnotationPresent(SuppressWarnings.class),
              "isAnnotationPresent false on TYPE site");
        check(SwType.class.getAnnotations().length == 0,
              "no runtime-visible annotations at all on the suppressed type");
        // contrast: a self-made RUNTIME annotation IS visible on the same class
        check(SuppressWarningsTest.class.getDeclaredMethod("rvMethod")
                  .getAnnotation(RuntimeVisible.class) != null,
              "RUNTIME-retained control annotation is visible");
        check("x".equals(SuppressWarningsTest.class.getDeclaredMethod("rvMethod")
                             .getAnnotation(RuntimeVisible.class).value()),
              "control annotation value round-trips");
    }

    static void testTargetVersionDifference() {
        if (MODERN) {
            check(SuppressWarnings.class.getAnnotation(Target.class) == null,
                  "modern SuppressWarnings carries no @Target (all targets allowed)");
        } else {
            ElementType[] ts = SuppressWarnings.class.getAnnotation(Target.class).value();
            check(ts.length == 6, "JDK-8 @Target has exactly 6 elements");
            java.util.EnumSet<ElementType> set = java.util.EnumSet.noneOf(ElementType.class);
            for (ElementType t : ts) set.add(t);
            check(set.contains(ElementType.TYPE), "8-side target TYPE");
            check(set.contains(ElementType.FIELD), "8-side target FIELD");
            check(set.contains(ElementType.METHOD), "8-side target METHOD");
            check(set.contains(ElementType.PARAMETER), "8-side target PARAMETER");
            check(set.contains(ElementType.CONSTRUCTOR), "8-side target CONSTRUCTOR");
            check(set.contains(ElementType.LOCAL_VARIABLE), "8-side target LOCAL_VARIABLE");
            check(!set.contains(ElementType.PACKAGE), "8-side lacks PACKAGE");
        }
    }

    static void testValueMemberShape() throws Exception {
        java.lang.reflect.Method v = SuppressWarnings.class.getMethod("value");
        check(v.getReturnType() == String[].class, "value() returns String[]");
        check(v.getParameterTypes().length == 0, "value() takes no arguments");
        check(v.getDefaultValue() == null,
              "value() has NO default: every use must supply the warning names");
        check(SuppressWarnings.class.getDeclaredMethods().length == 1,
              "value() is the sole member");
        check(!v.isAnnotationPresent(SafeVarargs.class), "value() itself is not @SafeVarargs");
    }

    static void testStructuralExtras() {
        check(SuppressWarnings.class.isAnnotation(), "is an annotation type");
        check(SuppressWarnings.class.isInterface(), "annotation types are interfaces");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(SuppressWarnings.class),
              "assignable to Annotation");
        check("java.lang.SuppressWarnings".equals(SuppressWarnings.class.getName()),
              "binary name");
        check(SuppressWarnings.class.getDeclaredFields().length == 0, "no fields");
        check(SuppressWarnings.class.getDeclaredClasses().length == 0, "no nested classes");
        check(!SuppressWarnings.class.isAnnotationPresent(java.lang.annotation.Documented.class),
              "@Documented absent");
        check(SuppressWarnings.class.getAnnotation(SuppressWarnings.class) == null,
              "not self-annotated");
        check(SuppressWarnings.class.getAnnotation(java.lang.annotation.Inherited.class) == null,
              "@Inherited absent");
    }

    static int localSiteRan() {
        @SuppressWarnings("local")
        int local = 21;
        return local * 2;
    }

    static void testLocalSiteAndMetaCount() {
        check(localSiteRan() == 42, "LOCAL_VARIABLE site compiles and runs");
        int metaCount = SuppressWarnings.class.getAnnotations().length;
        if (MODERN) {
            check(metaCount == 1, "modern meta-annotations: Retention only (no Target)");
        } else {
            check(metaCount == 2, "JDK-8 meta-annotations: Retention + Target");
        }
        check(SuppressWarnings.class.getDeclaredAnnotations().length == metaCount,
              "declared annotations count equals visible count");
    }

    // Control group for the array-member contract SuppressWarnings itself cannot
    // demonstrate (SOURCE retention): a RUNTIME annotation with a String[] member
    // hands out defensive copies on every value() call.
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface ArrayVal { String[] value(); }

    @ArrayVal({"a", "b"})
    static class ArraySite {}

    static void testArrayMemberDefensiveCopy() {
        ArrayVal av = ArraySite.class.getAnnotation(ArrayVal.class);
        check(av != null, "control array annotation present");
        String[] v1 = av.value();
        check(v1.length == 2 && "a".equals(v1[0]) && "b".equals(v1[1]),
              "array member reads back exactly");
        v1[0] = "z";
        String[] v2 = av.value();
        check("a".equals(v2[0]), "mutating a returned array does not affect the annotation");
        check(java.util.Arrays.equals(av.value(), new String[]{"a", "b"}),
              "annotation member intact after external mutation");
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRuntimeInvisibility();
        testTargetVersionDifference();
        testValueMemberShape();
        testStructuralExtras();
        testLocalSiteAndMetaCount();
        testArrayMemberDefensiveCopy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
