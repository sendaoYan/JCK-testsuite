/*
 * @test
 * @summary java.lang.annotation.Native conformance: SOURCE-retained, FIELD-targeted, documented
 *          marker annotation whose usages are invisible at runtime.
 * @run main NativeTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Native;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class NativeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures ----
    static class Constants {
        @Native
        public static final int ANSI_FG_RED = 31;

        public static final int PLAIN = 0; // no @Native
    }

    // ---- Part 1: structural contract ----
    static void testStructural() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Native");
        check(c.isAnnotation(), "Native is an annotation type");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no members");

        Retention ret = Native.class.getAnnotation(Retention.class);
        check(ret != null && ret.value() == RetentionPolicy.SOURCE, "@Retention(SOURCE)");
        Target tgt = Native.class.getAnnotation(Target.class);
        check(tgt != null && tgt.value().length == 1 && tgt.value()[0] == ElementType.FIELD,
              "@Target(FIELD)");
        check(Native.class.isAnnotationPresent(Documented.class), "@Documented on Native");
    }

    // ---- Part 2: behavior ----
    static void testBehavior() throws Exception {
        // SOURCE retention: usages are discarded by the compiler -> invisible at runtime
        Field f = Constants.class.getField("ANSI_FG_RED");
        check(f.getAnnotation(Native.class) == null, "SOURCE-retained usage invisible at runtime");
        check(f.getAnnotations().length == 0, "no annotations left on the field");

        // meta-annotation values are still readable on the annotation type itself
        check(Native.class.isAnnotation(), "annotation type introspectable");
        Annotation a = Native.class.getAnnotation(Retention.class);
        check(a != null, "meta @Retention survives (it is RUNTIME retained)");

        // contrast: a plain field carries nothing
        Field plain = Constants.class.getField("PLAIN");
        check(plain.getAnnotations().length == 0, "plain field has none");

        // the marker contract: Native has no members, instances carry no state
        Native instance = new Native() {
            @Override public Class<? extends Annotation> annotationType() { return Native.class; }
        };
        check(instance.annotationType() == Native.class, "hand-written instance type");

        // JDK self-use examples are SOURCE-visible only in source, not at runtime
        check(Integer.class.getField("MAX_VALUE").getAnnotation(Native.class) == null,
              "JDK usage of @Native (Integer.MAX_VALUE) not visible at runtime");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
