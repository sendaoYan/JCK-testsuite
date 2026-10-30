/*
 * @test
 * @summary JLS 9.7.3: a single-element annotation may omit "value =" and supply
 *          the value directly.
 * @run main SingleElementAnnotation
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SingleElementAnnotation {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Color { String value(); }

    @Color("red")
    static class C {}

    public static void main(String[] args) {
        Color c = C.class.getAnnotation(Color.class);
        if (!"red".equals(c.value())) throw new AssertionError("single-element value");
        System.out.println("PASS");
    }
}
