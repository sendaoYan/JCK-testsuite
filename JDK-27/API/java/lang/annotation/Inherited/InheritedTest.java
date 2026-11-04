/*
 * @test
 * @summary java.lang.annotation.Inherited conformance: white-box structure + black-box behavior.
 * @run main InheritedTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Inherited");
        check(c.isAnnotation(), "Inherited is an annotation type");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no members");
        check(c.isAnnotationPresent(Documented.class), "@Documented on Inherited");
        check(c.isAnnotationPresent(java.lang.annotation.Target.class), "@Target on Inherited");
        check(c.isAnnotationPresent(Retention.class)
              && c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
