/*
 * @test
 * @summary java.lang.annotation.Retention conformance: RUNTIME-retained meta-annotation,
 *          single member value(), and the three retention policies driving runtime visibility.
 * @run main RetentionTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class RetentionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures: three retention policies on identical member shapes ----
    @Retention(RetentionPolicy.SOURCE)
    @interface SrcKept {
    }

    @Retention(RetentionPolicy.CLASS)
    @interface ClassKept {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface RuntimeKept {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface RuntimeValued {
        String value() default "d";
    }

    // default retention (CLASS) when @Retention is absent
    @interface NoRetention {
    }

    // same shape, but actually USED on an element: its usage must be invisible
    @interface DefaultRetention {
    }

    @DefaultRetention
    static class DefaultHeld {
    }

    @SrcKept
    @ClassKept
    @RuntimeKept
    @RuntimeValued("v")
    static class Held {
    }

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Retention");
        check(c.isAnnotation(), "Retention is an annotation type");
        check(c.getDeclaredMethods().length == 1, "single member");
        check(c.getDeclaredMethod("value").getReturnType() == RetentionPolicy.class,
              "value() returns RetentionPolicy");
        check(c.getDeclaredMethod("value").getDefaultValue() == null, "no default for value()");
    }

    // ---- Part 2: meta-annotation contract ----
    static void testMetaAnnotation() {
        // Retention itself is @Retention(RUNTIME), @Target(ANNOTATION_TYPE)
        Retention self = Retention.class.getAnnotation(Retention.class);
        check(self != null, "Retention self-annotated");
        check(self.value() == RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        Target t = Retention.class.getAnnotation(Target.class);
        check(t != null, "Retention has @Target");
        check(t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
    }

    // ---- Part 3: retention drives runtime visibility ----
    static void testVisibility() {
        // meta-annotation value readable at runtime for RUNTIME-retained annotations
        Retention r = RuntimeKept.class.getAnnotation(Retention.class);
        check(r != null, "@Retention visible on RUNTIME-kept annotation");
        check(r.value() == RetentionPolicy.RUNTIME, "value RUNTIME");
        check(r.annotationType() == Retention.class, "annotationType");

        // SOURCE and CLASS usages are invisible to AnnotatedElement at runtime
        check(Held.class.getAnnotation(SrcKept.class) == null, "SOURCE usage invisible");
        check(Held.class.getAnnotation(ClassKept.class) == null, "CLASS usage invisible");
        check(Held.class.getAnnotation(RuntimeKept.class) != null, "RUNTIME usage visible");
        check(Held.class.getAnnotations().length == 2, "only the two RUNTIME annotations listed");

        // member values round-trip through retention
        RuntimeValued rv = Held.class.getAnnotation(RuntimeValued.class);
        check(rv != null && "v".equals(rv.value()), "member value readable");
        check(rv.toString().contains("v"), "toString carries member value: " + rv);

        // default retention is CLASS (no @Retention at all)
        check(NoRetention.class.getAnnotation(Retention.class) == null,
              "annotation without @Retention has none visible (defaults to CLASS)");
        Object o = new NoRetention() {
            @Override public Class<? extends Annotation> annotationType() { return NoRetention.class; }
        };
        check(o instanceof Annotation, "hand-written instance still an Annotation");
    }

    // ---- Part 4: default-is-CLASS contract and isAnnotationPresent sync ----
    static void testDefaultAndSync() throws Exception {
        // an annotation with no @Retention defaults to CLASS: its USAGE is invisible
        // to reflection -- the core "default retention == CLASS" contract
        check(DefaultHeld.class.getAnnotation(DefaultRetention.class) == null,
              "default-retention usage invisible to getAnnotation");
        check(!DefaultHeld.class.isAnnotationPresent(DefaultRetention.class),
              "isAnnotationPresent in sync: invisible");
        check(DefaultHeld.class.getAnnotations().length == 0,
              "default-retention usage absent from getAnnotations");
        check(DefaultHeld.class.getDeclaredAnnotations().length == 0,
              "absent from getDeclaredAnnotations as well");

        // isAnnotationPresent is in sync with getAnnotation for every policy
        check(!Held.class.isAnnotationPresent(SrcKept.class), "SOURCE not present");
        check(!Held.class.isAnnotationPresent(ClassKept.class), "CLASS not present");
        check(Held.class.isAnnotationPresent(RuntimeKept.class), "RUNTIME present");

        // Retention itself is @Documented (javadoc visibility of the meta-annotation)
        check(Retention.class.isAnnotationPresent(java.lang.annotation.Documented.class),
              "Retention is @Documented");

        // Retention.value() reads back exactly this signature on both JDKs
        java.lang.reflect.Method v = Retention.class.getMethod("value");
        check(v.toString().equals(
                "public abstract java.lang.annotation.RetentionPolicy "
              + "java.lang.annotation.Retention.value()"),
              "Retention.value() exact signature: " + v);

        // Retention instances are value-based: same policy -> equal, stable hashCode
        Retention r1 = RuntimeKept.class.getAnnotation(Retention.class);
        Retention r2 = RuntimeKept.class.getAnnotation(Retention.class);
        check(r1.equals(r2), "Retention equal across fetches");
        check(r1.hashCode() == r2.hashCode(), "Retention hashCode stable");
        check(!r1.equals(SrcKept.class.getAnnotation(Retention.class)),
              "different policies not equal");

        // Target[] of Retention is defensively copied
        Target t = Retention.class.getAnnotation(Target.class);
        ElementType[] tv = t.value();
        tv[0] = null;
        check(t.value()[0] == ElementType.ANNOTATION_TYPE, "Retention @Target value copied");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testMetaAnnotation();
        testVisibility();
        testDefaultAndSync();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
