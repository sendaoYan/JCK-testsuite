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

        @Native
        public static final long LONG_MASK = 0xFFFFFFFFL;

        @Native
        public static final double E_OFFSET = 2.5;
    }

    // interface constants may carry @Native too (they are fields)
    interface NativeIface {
        @Native
        int CONST = 5;
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
        check(Native.class.getAnnotation(Documented.class) != null, "@Documented directly readable");
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

        // the constants keep their values regardless of SOURCE-retained annotations
        check(Constants.ANSI_FG_RED == 31, "annotated constant value intact");
        check(Constants.PLAIN == 0, "plain constant value intact");

        // contrast: a plain field carries nothing
        Field plain = Constants.class.getField("PLAIN");
        check(plain.getAnnotations().length == 0, "plain field has none");

        // the marker contract: Native has no members, instances carry no state
        Native instance = new Native() {
            @Override public Class<? extends Annotation> annotationType() { return Native.class; }
        };
        check(instance.annotationType() == Native.class, "hand-written instance type");
        check(instance.equals(instance), "hand-written instance reflexive");

        // JDK self-use examples are SOURCE-visible only in source, not at runtime
        check(Integer.class.getField("MAX_VALUE").getAnnotation(Native.class) == null,
              "JDK usage of @Native (Integer.MAX_VALUE) not visible at runtime");
    }

    // ---- Part 3: deepened semantics ----
    static void testDeepenedSemantics() throws Exception {
        // @Native on non-int field kinds: long and double constants, equally invisible
        check(Constants.class.getField("LONG_MASK").getAnnotation(Native.class) == null,
              "long field usage invisible at runtime");
        check(Constants.class.getField("E_OFFSET").getAnnotation(Native.class) == null,
              "double field usage invisible at runtime");
        check(Constants.LONG_MASK == 0xFFFFFFFFL, "long constant value intact");
        check(Constants.E_OFFSET == 2.5, "double constant value intact");

        // interface constants (implicitly public static final fields) accept @Native
        Field ifaceField = NativeIface.class.getField("CONST");
        check(ifaceField.getAnnotation(Native.class) == null,
              "interface constant usage invisible at runtime");
        check(NativeIface.CONST == 5, "interface constant value intact");

        // Native carries exactly three meta-annotations: @Documented, @Retention(SOURCE),
        // @Target(FIELD)
        check(Native.class.getDeclaredAnnotations().length == 3,
              "three meta-annotations on Native: "
              + Native.class.getDeclaredAnnotations().length);
        check(!Native.class.isAnnotationPresent(java.lang.annotation.Inherited.class),
              "Native is not @Inherited");
        check(!Native.class.isAnnotationPresent(java.lang.annotation.Repeatable.class),
              "Native is not @Repeatable");

        // the annotation type itself remains fully reflectable despite SOURCE retention:
        // retention governs usages, never the type's own Class object
        check(Native.class.isAnnotation(), "Native Class object reflectable");
        check(Native.class.getDeclaredMethods().length == 0, "marker: no members");
    }

    public static void main(String[] args) throws Exception {
        testStructural();
        testBehavior();
        testDeepenedSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
