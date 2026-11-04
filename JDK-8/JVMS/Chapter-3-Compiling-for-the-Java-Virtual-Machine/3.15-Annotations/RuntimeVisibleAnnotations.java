/*
 * @test
 * @summary JVMS 3.15/4.7.16-4.7.17: annotations with RUNTIME retention are
 *          compiled into RuntimeVisibleAnnotations attributes and remain
 *          inspectable through reflection on classes, fields and methods.
 * @run main RuntimeVisibleAnnotations
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RuntimeVisibleAnnotations {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Mark {
        String name();
        int count() default 1;
    }

    @Mark(name = "cls")
    static class Annotated {
        @Mark(name = "field")
        int f;

        @Mark(name = "method", count = 3)
        void m() {}
    }

    public static void main(String[] args) throws Exception {
        Mark c = Annotated.class.getAnnotation(Mark.class);
        check(c != null && "cls".equals(c.name()), "class annotation preserved");

        Field f = Annotated.class.getDeclaredField("f");
        Mark fa = f.getAnnotation(Mark.class);
        check(fa != null && "field".equals(fa.name()), "field annotation preserved");

        Method m = Annotated.class.getDeclaredMethod("m");
        Mark ma = m.getAnnotation(Mark.class);
        check(ma != null && "method".equals(ma.name()) && ma.count() == 3,
                "method annotation preserved with explicit element");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
