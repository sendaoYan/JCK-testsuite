/*
 * @test
 * @summary java.lang.annotation.Retention conformance: white-box structure + black-box behavior,
 *          single member value(), and three retention policies driving runtime visibility.
 * @run main RetentionTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

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

    @SrcKept
    @ClassKept
    @RuntimeKept
    @RuntimeValued("v")
    static class Held {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Retention");
        check(c.isAnnotation(), "Retention is an annotation type");
        check(c.isAnnotationPresent(Retention.class), "self-annotated @Retention(RUNTIME)");
        check(c.isAnnotationPresent(Target.class), "@Target(ANNOTATION_TYPE)");
        check(c.getDeclaredMethods().length == 1, "single member");
        check(c.getDeclaredMethod("value").getReturnType() == RetentionPolicy.class,
              "value() returns RetentionPolicy");
        check(c.getDeclaredMethod("value").getDefaultValue() == null, "no default for value()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // meta-annotation value readable at runtime for RUNTIME-retained annotations
        Retention r = RuntimeKept.class.getAnnotation(Retention.class);
        check(r != null, "@Retention visible on RUNTIME-kept annotation");
        check(r.value() == RetentionPolicy.RUNTIME, "value RUNTIME");
        check(r.annotationType() == Retention.class, "annotationType");

        // Retention itself is RUNTIME-retained (otherwise no policy would be visible at all)
        Retention self = Retention.class.getAnnotation(Retention.class);
        check(self != null && self.value() == RetentionPolicy.RUNTIME,
              "Retention itself is @Retention(RUNTIME)");

        // Retention is @Documented @Target(ANNOTATION_TYPE)
        check(Retention.class.isAnnotationPresent(Documented.class), "Retention is @Documented");
        Target t = Retention.class.getAnnotation(Target.class);
        check(t != null && t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "Retention is @Target(ANNOTATION_TYPE)");

        // retention policy drives reflection visibility:
        // SOURCE and CLASS usages are invisible to AnnotatedElement at runtime
        check(Held.class.getAnnotation(SrcKept.class) == null, "SOURCE usage invisible");
        check(Held.class.getAnnotation(ClassKept.class) == null, "CLASS usage invisible");
        check(Held.class.getAnnotation(RuntimeKept.class) != null, "RUNTIME usage visible");
        check(Held.class.getAnnotations().length == 2, "only the two RUNTIME annotations listed");

        // member values round-trip through retention
        RuntimeValued rv = Held.class.getAnnotation(RuntimeValued.class);
        check(rv != null && rv.value().equals("v"), "member value readable");
        check(rv.toString().endsWith("RuntimeValued(\"v\")"), "toString lone-value form: " + rv);

        // default retention is CLASS (no @Retention at all)
        check(NoRetention.class.getAnnotation(Retention.class) == null,
              "annotation without @Retention has none visible (defaults to CLASS)");
        Object o = new NoRetention() {
            @Override public Class<? extends Annotation> annotationType() { return NoRetention.class; }
        };
        check(o instanceof Annotation, "hand-written instance still an Annotation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
