/*
 * @test
 * @summary java.lang.annotation.Documented conformance: white-box structure + black-box behavior.
 * @run main DocumentedTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DocumentedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures ----
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface Doc {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NoDoc {
    }

    @Doc
    @NoDoc
    static class Both {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Documented");
        check(c.isAnnotation(), "Documented is an annotation type");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no members");
        check(c.isAnnotationPresent(Documented.class), "self-annotated @Documented");
        check(c.isAnnotationPresent(Retention.class)
              && c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // @Documented presence on custom annotation, readable at runtime
        Documented d = Doc.class.getAnnotation(Documented.class);
        check(d != null, "Doc has @Documented");
        check(d.annotationType() == Documented.class, "annotationType");
        check(d.equals(Doc.class.getAnnotation(Documented.class)), "equals per contract");
        check(d.toString().endsWith("Documented()"), "toString: " + d);

        // absence on the control annotation
        check(NoDoc.class.getAnnotation(Documented.class) == null, "NoDoc lacks @Documented");

        // Documented is inherited by nothing by itself: usage on Both readable via members
        check(Both.class.getAnnotation(Doc.class) != null, "Doc usable on class");
        check(Both.class.getAnnotation(NoDoc.class) != null, "NoDoc usable on class");
        check(Both.class.getAnnotations().length == 2, "both usages listed");

        // JDK's own documented annotations carry @Documented (javadoc visibility)
        check(Retention.class.getAnnotation(Documented.class) != null,
              "Retention itself is @Documented");
        check(Deprecated.class.getAnnotation(Documented.class) != null,
              "Deprecated is @Documented");

        // Documented is a pure marker: instances have no state to observe
        Annotation asAnn = d;
        check(asAnn.annotationType() == Documented.class, "viewed as plain Annotation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
