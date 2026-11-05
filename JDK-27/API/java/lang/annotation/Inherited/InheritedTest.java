/*
 * @test
 * @summary java.lang.annotation.Inherited conformance: white-box structure + black-box behavior,
 *          class-only inheritance, no propagation through interface implementation or method
 *          overriding.
 * @run main InheritedTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class InheritedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures ----
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface Tag {
        String value() default "base";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NotPassed {
    }

    @Tag("onBase")
    @NotPassed
    static class Base {
    }

    static class Child extends Base {
    }

    static class GrandChild extends Child {
    }

    @Tag("onSub")
    static class OverrideChild extends Base {
    }

    // @Inherited does not propagate through interface implementation
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface IfaceTag {
    }

    @IfaceTag
    interface IfaceBase {
    }

    static class Impl implements IfaceBase {
    }

    // @Inherited does not propagate through method overriding
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface MethodTag {
    }

    static class MethodBase {
        @MethodTag
        void work() {
        }
    }

    static class MethodSub extends MethodBase {
        @Override
        void work() {
        }
    }

    // interfaces do not inherit @Inherited annotations from superinterfaces either
    interface IfaceSub extends IfaceBase {
    }

    // an annotation on an abstract superclass is inherited by the concrete subclass
    @Tag("abs")
    static abstract class AbsBase {
    }

    static class Conc extends AbsBase {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Inherited");
        check(c.isAnnotation(), "Inherited is an annotation type");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no members");
        check(c.isAnnotationPresent(Documented.class), "@Documented on Inherited");
        check(c.isAnnotationPresent(java.lang.annotation.Target.class), "@Target on Inherited");
        Target t = c.getAnnotation(Target.class);
        check(t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
        check(c.isAnnotationPresent(Retention.class)
              && c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // direct presence
        check(Base.class.getAnnotation(Tag.class) != null, "Tag on Base");
        check("onBase".equals(Base.class.getAnnotation(Tag.class).value()), "Base tag value");

        // inherited lookup: subclass sees the superclass annotation
        Tag childTag = Child.class.getAnnotation(Tag.class);
        check(childTag != null, "Tag inherited by Child");
        check("onBase".equals(childTag.value()), "inherited instance carries same value");

        // multi-level inheritance
        check(GrandChild.class.getAnnotation(Tag.class) != null, "Tag inherited by GrandChild");

        // subclass may override with its own usage
        check("onSub".equals(OverrideChild.class.getAnnotation(Tag.class).value()),
              "subclass usage shadows inherited one");

        // control: same placement without @Inherited is NOT inherited
        check(Child.class.getAnnotation(NotPassed.class) == null,
              "non-@Inherited annotation not inherited");
        check(Base.class.getAnnotation(NotPassed.class) != null, "control present on Base");

        // isAnnotationPresent honors inheritance too
        check(Child.class.isAnnotationPresent(Tag.class), "isAnnotationPresent sees inherited");
        check(!Child.class.isAnnotationPresent(NotPassed.class), "isAnnotationPresent strict");

        // getDeclaredAnnotation bypasses inheritance
        check(Child.class.getDeclaredAnnotation(Tag.class) == null,
              "getDeclaredAnnotation ignores inherited");

        // getAnnotations includes inherited; getDeclaredAnnotations does not
        boolean childSeesTag = false;
        for (Annotation a : Child.class.getAnnotations()) {
            if (a.annotationType() == Tag.class) childSeesTag = true;
        }
        check(childSeesTag, "getAnnotations includes inherited Tag");
        check(Child.class.getDeclaredAnnotations().length == 0,
              "getDeclaredAnnotations empty on Child");

        // @Inherited applies to class inheritance only, not interface implementation
        check(IfaceBase.class.getAnnotation(IfaceTag.class) != null,
              "IfaceTag present on the interface itself");
        check(Impl.class.getAnnotation(IfaceTag.class) == null,
              "interface inheritance not applicable");

        // @Inherited does not propagate through method overriding
        Method base = MethodBase.class.getDeclaredMethod("work");
        Method sub = MethodSub.class.getDeclaredMethod("work");
        check(base.getAnnotation(MethodTag.class) != null, "MethodTag on base method");
        check(sub.getAnnotation(MethodTag.class) == null,
              "method annotation not inherited by overriding method");
    }

    // ---- Part 3: extended inheritance semantics ----
    static void testExtendedSemantics() {
        // superinterface -> subinterface: also NOT inherited (interfaces never inherit)
        check(IfaceSub.class.getAnnotation(IfaceTag.class) == null,
              "sub-interface does not inherit from super-interface");
        check(IfaceBase.class.getAnnotation(IfaceTag.class) != null,
              "the annotated interface itself keeps the annotation");

        // abstract superclass -> concrete subclass: still inherited
        Tag concTag = Conc.class.getAnnotation(Tag.class);
        check(concTag != null, "annotation on abstract superclass inherited");
        check("abs".equals(concTag.value()), "inherited value from abstract superclass");

        // getAnnotationsByType honors @Inherited just like getAnnotation
        check(Child.class.getAnnotationsByType(Tag.class).length == 1,
              "getAnnotationsByType sees the inherited annotation");

        // the Inherited meta-annotation itself: marker, documented, NOT self-inherited
        check(!Inherited.class.isAnnotationPresent(Inherited.class),
              "Inherited is not itself @Inherited");
        check(Inherited.class.isAnnotationPresent(Documented.class),
              "Inherited is @Documented");
        check(!Inherited.class.isAnnotationPresent(java.lang.annotation.Repeatable.class),
              "Inherited is not @Repeatable");
        check(Inherited.class.getDeclaredAnnotations().length == 3,
              "three meta-annotations on Inherited: "
              + Inherited.class.getDeclaredAnnotations().length);

        // the concrete subclass carries nothing of its own in the declared view while
        // the inherited view is populated
        check(Conc.class.getDeclaredAnnotations().length == 0,
              "declared view on concrete subclass stays empty");
        check(Conc.class.getAnnotationsByType(Tag.class).length == 1,
              "getAnnotationsByType on concrete subclass sees inherited");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExtendedSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
