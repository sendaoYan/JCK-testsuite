/*
 * @test
 * @summary java.lang.annotation.Documented conformance: white-box structure + black-box behavior,
 *          marker meta-annotation, self-annotated, RUNTIME-retained, ANNOTATION_TYPE-targeted.
 * @run main DocumentedTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
        Target t = c.getAnnotation(Target.class);
        check(t != null && t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Documented d = Doc.class.getAnnotation(Documented.class);
        check(d != null, "Doc has @Documented");
        check(d.annotationType() == Documented.class, "annotationType");
        check(d.equals(Doc.class.getAnnotation(Documented.class)), "equals per contract");
        check(d.toString().endsWith("Documented()"), "toString: " + d);

        check(NoDoc.class.getAnnotation(Documented.class) == null, "NoDoc lacks @Documented");

        check(Both.class.getAnnotation(Doc.class) != null, "Doc usable on class");
        check(Both.class.getAnnotation(NoDoc.class) != null, "NoDoc usable on class");
        check(Both.class.getAnnotations().length == 2, "both usages listed");
        check(Doc.class.isAnnotationPresent(Documented.class), "Doc is @Documented");
        check(!NoDoc.class.isAnnotationPresent(Documented.class), "NoDoc is not @Documented");

        // JDK's own documented annotations carry @Documented (javadoc visibility)
        check(Retention.class.getAnnotation(Documented.class) != null,
              "Retention itself is @Documented");
        check(Deprecated.class.getAnnotation(Documented.class) != null,
              "Deprecated is @Documented");

        Annotation asAnn = d;
        check(asAnn.annotationType() == Documented.class, "viewed as plain Annotation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
