/*
 * @test
 * @summary java.lang.annotation.Target conformance: white-box structure + black-box behavior.
 * @run main TargetTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TargetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures: restricted targets vs unrestricted ----
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface OnlyField {
    }

    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @interface MethodOrParam {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Anywhere {
    }

    static class Carrier {
        @OnlyField
        static int field;

        @MethodOrParam("m")
        void method(@MethodOrParam("p") int p) {
        }
    }

    @Anywhere
    static class TypeUse {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Target");
        check(c.isAnnotation(), "Target is an annotation type");
        check(c.isAnnotationPresent(Retention.class), "has @Retention");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
        check(c.getDeclaredMethods().length == 1, "single member");
        check(c.getDeclaredMethod("value").getReturnType() == ElementType[].class,
              "value() returns ElementType[]");
        // self-targeted to ANNOTATION_TYPE only
        Target self = Target.class.getAnnotation(Target.class);
        check(self != null, "self-annotated");
        check(self.value().length == 1 && self.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // restricted annotation IS present on permitted targets
        Field f = Carrier.class.getDeclaredField("field");
        check(f.getAnnotation(OnlyField.class) != null, "@Target(FIELD) usable on field");

        Method m = Carrier.class.getDeclaredMethod("method", int.class);
        check(m.getAnnotation(MethodOrParam.class) != null, "@Target(METHOD,...) usable on method");
        check(m.getAnnotation(MethodOrParam.class).value().equals("m"), "method member value");
        check(m.getParameters()[0].getAnnotation(MethodOrParam.class) != null,
              "@Target(...,PARAMETER) usable on parameter");
        check(m.getParameters()[0].getAnnotation(MethodOrParam.class).value().equals("p"),
              "parameter member value");

        // unrestricted annotation usable on type (no @Target = any target)
        check(TypeUse.class.getAnnotation(Anywhere.class) != null,
              "no @Target: usable on type");

        // reading the Target value from the annotation declarations
        Target tf = OnlyField.class.getAnnotation(Target.class);
        check(tf != null && tf.value().length == 1 && tf.value()[0] == ElementType.FIELD,
              "OnlyField declared targets readable");
        Target tmp = MethodOrParam.class.getAnnotation(Target.class);
        check(tmp != null && tmp.value().length == 2
              && tmp.value()[0] == ElementType.METHOD && tmp.value()[1] == ElementType.PARAMETER,
              "MethodOrParam targets in declared order");
        check(Anywhere.class.getAnnotation(Target.class) == null,
              "Anywhere has no @Target (unrestricted)");

        // wrong-target usage is a compile-time error, so at runtime we verify the
        // positive space only; assert annotations did NOT leak to other elements
        check(Carrier.class.getAnnotation(OnlyField.class) == null,
              "field-only annotation absent from class");
        check(f.getAnnotation(Anywhere.class) == null, "Anywhere not used on this field");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
