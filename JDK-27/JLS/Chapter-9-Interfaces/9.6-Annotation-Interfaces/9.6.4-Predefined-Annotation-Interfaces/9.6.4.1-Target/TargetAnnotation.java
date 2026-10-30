/*
 * @test
 * @summary JLS 9.6.4.1: the Target meta-annotation restricts the contexts in
 *          which an annotation interface may apply; a conforming use is accepted.
 * @run main TargetAnnotation
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TargetAnnotation {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface OnlyOnMethods {}

    static class C {
        @OnlyOnMethods
        void m() {}
    }

    public static void main(String[] args) throws Exception {
        if (C.class.getDeclaredMethod("m").getAnnotation(OnlyOnMethods.class) == null)
            throw new AssertionError("@Target(METHOD) annotation not present");
        System.out.println("PASS");
    }
}
