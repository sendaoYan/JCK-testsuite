/*
 * @test
 * @summary java.lang.annotation.Documented conformance: marker meta-annotation, self-annotated,
 *          RUNTIME-retained and ANNOTATION_TYPE-targeted, and runtime presence on documented
 *          annotations.
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

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
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

    // ---- Part 2: behavior ----
    static void testBehavior() {
        Documented d = Doc.class.getAnnotation(Documented.class);
        check(d != null, "Doc has @Documented");
        check(d.annotationType() == Documented.class, "annotationType");
        check(d.equals(Doc.class.getAnnotation(Documented.class)), "equals per contract");
        check(d.toString().contains("Documented"), "toString carries name: " + d);

        // absence on the control annotation
        check(NoDoc.class.getAnnotation(Documented.class) == null, "NoDoc lacks @Documented");

        // Documented affects javadoc inclusion, not runtime visibility: both annotations are
        // RUNTIME-retained and thus readable, but only Doc is marked @Documented
        check(Both.class.getAnnotation(Doc.class) != null, "Doc usable on class");
        check(Both.class.getAnnotation(NoDoc.class) != null, "NoDoc usable on class");
        check(Both.class.getAnnotations().length == 2, "both usages listed");
        check(Doc.class.isAnnotationPresent(Documented.class), "Doc is @Documented");
        check(!NoDoc.class.isAnnotationPresent(Documented.class), "NoDoc is not @Documented");

        // JDK's own documented annotations carry @Documented (javadoc visibility)
        check(Retention.class.getAnnotation(Documented.class) != null, "Retention is @Documented");
        check(Deprecated.class.getAnnotation(Documented.class) != null, "Deprecated is @Documented");

        // Documented is a pure marker: instances have no state to observe
        Annotation asAnn = d;
        check(asAnn.annotationType() == Documented.class, "viewed as plain Annotation");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
