/*
 * @test
 * @summary java.lang.annotation.Inherited conformance: class-only inheritance semantics, no
 *          propagation through interface implementation or method overriding, and meta-annotation
 *          contract.
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

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Inherited");
        check(c.isAnnotation(), "Inherited is an annotation type");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no members");
        check(c.isAnnotationPresent(Documented.class), "@Documented on Inherited");
        check(c.isAnnotationPresent(Target.class), "@Target on Inherited");
        Target t = c.getAnnotation(Target.class);
        check(t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
        check(c.isAnnotationPresent(Retention.class)
              && c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
    }

    // ---- Part 2: class inheritance semantics ----
    static void testClassInheritance() {
        check(Base.class.getAnnotation(Tag.class) != null, "Tag on Base");
        check("onBase".equals(Base.class.getAnnotation(Tag.class).value()), "Base tag value");

        Tag childTag = Child.class.getAnnotation(Tag.class);
        check(childTag != null, "Tag inherited by Child");
        check("onBase".equals(childTag.value()), "inherited instance carries same value");

        check(GrandChild.class.getAnnotation(Tag.class) != null, "Tag inherited by GrandChild");

        check("onSub".equals(OverrideChild.class.getAnnotation(Tag.class).value()),
              "subclass usage shadows inherited one");

        check(Child.class.getAnnotation(NotPassed.class) == null,
              "non-@Inherited annotation not inherited");
        check(Base.class.getAnnotation(NotPassed.class) != null, "control present on Base");

        check(Child.class.isAnnotationPresent(Tag.class), "isAnnotationPresent sees inherited");
        check(!Child.class.isAnnotationPresent(NotPassed.class), "isAnnotationPresent strict");

        check(Child.class.getDeclaredAnnotation(Tag.class) == null,
              "getDeclaredAnnotation ignores inherited");

        boolean childSeesTag = false;
        for (Annotation a : Child.class.getAnnotations()) {
            if (a.annotationType() == Tag.class) childSeesTag = true;
        }
        check(childSeesTag, "getAnnotations includes inherited Tag");
        check(Child.class.getDeclaredAnnotations().length == 0,
              "getDeclaredAnnotations empty on Child");
    }

    // ---- Part 3: no propagation through interface implementation ----
    static void testInterfaceNonInheritance() {
        check(IfaceBase.class.getAnnotation(IfaceTag.class) != null,
              "IfaceTag present on the interface itself");
        check(Impl.class.getAnnotation(IfaceTag.class) == null,
              "interface inheritance not applicable");
    }

    // ---- Part 4: no propagation through method overriding ----
    static void testMethodNonInheritance() throws Exception {
        Method base = MethodBase.class.getDeclaredMethod("work");
        Method sub = MethodSub.class.getDeclaredMethod("work");
        check(base.getAnnotation(MethodTag.class) != null, "MethodTag on base method");
        check(sub.getAnnotation(MethodTag.class) == null,
              "method annotation not inherited by overriding method");
    }

    // ---- Part 5: extended inheritance semantics ----
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

        // @Inherited has no effect on annotations with SOURCE/CLASS retention: nothing
        // runtime-visible exists at all for the subclass to inherit; the concrete
        // subclass carries nothing of its own in the declared view
        check(Conc.class.getDeclaredAnnotations().length == 0,
              "declared view on concrete subclass stays empty");
        check(Conc.class.getAnnotationsByType(Tag.class).length == 1,
              "getAnnotationsByType on concrete subclass sees inherited");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testClassInheritance();
        testInterfaceNonInheritance();
        testMethodNonInheritance();
        testExtendedSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
