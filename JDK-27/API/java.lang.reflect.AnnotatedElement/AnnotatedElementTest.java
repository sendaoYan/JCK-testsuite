/*
 * @test
 * @summary java.lang.reflect.AnnotatedElement conformance: white-box structure + black-box behavior.
 * @run main AnnotatedElementTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;

public class AnnotatedElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Repeatable(Tags.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @interface Tag {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @interface Tags {
        Tag[] value();
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Inh {
    }

    @Tag("a")
    @Tag("b")
    @Deprecated
    static class Multi {
        @Tag("f1")
        public int field;

        @Tag("m1")
        public void method() {
        }
    }

    @Inh
    static class Base {
    }

    static class Derived extends Base {
    }

    @Deprecated
    static class Annotated {
    }

    static class Plain {
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        AnnotatedElement e = Annotated.class;
        check(e.isAnnotationPresent(Deprecated.class), "isAnnotationPresent");
        check(e.getAnnotation(Deprecated.class) != null, "getAnnotation");
        check(e.getDeclaredAnnotation(Deprecated.class) != null, "getDeclaredAnnotation");
        check(e.getAnnotations().length == 1, "getAnnotations length");
        check(e.getDeclaredAnnotations().length == 1, "getDeclaredAnnotations length");
        check(e.getAnnotationsByType(Deprecated.class).length == 1, "getAnnotationsByType");
        check(e.getDeclaredAnnotationsByType(Deprecated.class).length == 1,
              "getDeclaredAnnotationsByType");

        AnnotatedElement p = Plain.class;
        check(!p.isAnnotationPresent(Deprecated.class), "plain not annotated");
        check(p.getAnnotation(Deprecated.class) == null, "plain getAnnotation null");
        check(p.getAnnotations().length == 0, "plain getAnnotations");
        check(AnnotatedElement.class.isInterface(), "is an interface");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. repeatable annotations: byType flattens, direct views show container
        AnnotatedElement multi = Multi.class;
        Tag[] byType = multi.getAnnotationsByType(Tag.class);
        check(byType.length == 2, "repeatable flattened to two");
        check("a".equals(byType[0].value()) && "b".equals(byType[1].value()),
              "declaration order kept");
        check(multi.getAnnotation(Tag.class) == null, "direct single absent when repeated");
        check(multi.getDeclaredAnnotationsByType(Tag.class).length == 2, "declared-byType two");
        check(multi.getAnnotations().length == 2, "directly present: container + Deprecated");

        // 2. @Inherited propagation to subclasses
        AnnotatedElement derived = Derived.class;
        check(derived.getAnnotation(Inh.class) != null, "inherited visible on subclass");
        check(derived.isAnnotationPresent(Inh.class), "inherited isAnnotationPresent");
        check(derived.getDeclaredAnnotation(Inh.class) == null, "inherited not declared on subclass");
        check(derived.getAnnotationsByType(Inh.class).length == 1, "inherited byType");
        AnnotatedElement base = Base.class;
        check(base.getDeclaredAnnotation(Inh.class) != null, "declared on superclass");
        check(AnnotatedElementTest.class.getAnnotation(Inh.class) == null,
              "unrelated class not inherited");

        // 3. the contract is uniform across member kinds
        AnnotatedElement f = Multi.class.getField("field");
        check(f.getAnnotation(Tag.class) != null && "f1".equals(f.getAnnotation(Tag.class).value()),
              "field annotation value");
        check(f.getAnnotationsByType(Tag.class).length == 1, "field byType");
        AnnotatedElement m = Multi.class.getMethod("method");
        check(m.isAnnotationPresent(Tag.class), "method isAnnotationPresent");
        check(m.getDeclaredAnnotations().length == 1, "method declared one");
        AnnotatedElement ctor = Multi.class.getDeclaredConstructor();
        check(ctor.getAnnotation(Tag.class) == null, "ctor unannotated null");
        check(ctor.getAnnotations().length == 0, "ctor no annotations");

        // 4. absent types and null arguments
        AnnotatedElement plain = Plain.class;
        check(plain.getAnnotationsByType(Tag.class).length == 0, "plain byType zero");
        check(plain.getDeclaredAnnotationsByType(Tag.class).length == 0, "plain declared-byType zero");
        boolean npe = false;
        try { plain.isAnnotationPresent(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "isAnnotationPresent(null) NPE");

        // 5. inherited single Tag (non-repeatable path) and getDeclaredAnnotation semantics
        check(multi.getDeclaredAnnotation(Tags.class) != null,
              "container is the declared annotation");
        check(multi.getAnnotation(Tags.class).value().length == 2, "container holds both");

        // 6. record components also honor the contract (propagated accessor)
        RecordComponent[] rcs = Rec.class.getRecordComponents();
        AnnotatedElement acc = rcs[0].getAccessor();
        check(acc.isAnnotationPresent(Deprecated.class), "record accessor annotated");
        check(rcs[0].isAnnotationPresent(Deprecated.class) == false,
              "component itself not annotated for method-target");
    }

    record Rec(@Deprecated int x) {
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
