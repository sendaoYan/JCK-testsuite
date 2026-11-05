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

    // @Documented on a method-targeted annotation with a member
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface DocMethod {
        String v() default "mv";
    }

    static class DocCarrier {
        @DocMethod(v = "go")
        void go() {
        }
    }

    // @Documented does NOT grant runtime visibility: no @Retention -> default CLASS
    @Documented
    @interface DocClassRet {
    }

    @DocClassRet
    static class Invisible {
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

    // ---- Part 3: deepened semantics ----
    static void testDeepenedSemantics() throws Exception {
        // @Documented on a method-targeted annotation: usage readable with member values
        java.lang.reflect.Method m = DocCarrier.class.getDeclaredMethod("go");
        DocMethod dm = m.getAnnotation(DocMethod.class);
        check(dm != null, "DocMethod present on method");
        check("go".equals(dm.v()), "DocMethod member value");
        check(dm.annotationType() == DocMethod.class, "DocMethod annotationType");
        check(m.isAnnotationPresent(DocMethod.class), "isAnnotationPresent on method");

        // @Documented does not affect retention: a documented annotation without
        // @Retention defaults to CLASS and its usages are invisible to reflection
        check(Invisible.class.getAnnotation(DocClassRet.class) == null,
              "@Documented alone does not make usages runtime-visible");
        check(!Invisible.class.isAnnotationPresent(DocClassRet.class),
              "isAnnotationPresent strict for CLASS-retained documented annotation");
        check(Invisible.class.getAnnotations().length == 0, "no runtime annotations listed");

        // Documented is a marker carrying no members: its own toString is exact
        Documented d = Doc.class.getAnnotation(Documented.class);
        check(d.toString().equals("@java.lang.annotation.Documented()"),
              "Documented toString exact: " + d);

        // Documented carries exactly three meta-annotations:
        // @Documented (self), @Retention(RUNTIME), @Target(ANNOTATION_TYPE)
        check(Documented.class.getDeclaredAnnotations().length == 3,
              "three meta-annotations on Documented: "
              + Documented.class.getDeclaredAnnotations().length);
        check(!Documented.class.isAnnotationPresent(java.lang.annotation.Inherited.class),
              "Documented is not @Inherited");
        check(!Documented.class.isAnnotationPresent(java.lang.annotation.Repeatable.class),
              "Documented is not @Repeatable");

        // an annotation type marked @Documented but its usages on a class do not make
        // the CLASS itself documented -- the meta-flag only affects javadoc tooling
        check(Both.class.getAnnotationsByType(Doc.class).length == 1,
              "getAnnotationsByType single instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepenedSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
