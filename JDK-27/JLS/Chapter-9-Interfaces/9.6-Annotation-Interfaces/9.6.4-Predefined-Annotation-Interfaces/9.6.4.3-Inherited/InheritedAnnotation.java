/*
 * @test
 * @summary JLS 9.6.4.3: the Inherited meta-annotation causes an annotation on a
 *          class to be inherited by its subclasses.
 * @run main InheritedAnnotation
 */

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class InheritedAnnotation {
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface Inheritable {}

    @Inheritable
    static class Base {}

    static class Sub extends Base {}

    public static void main(String[] args) {
        if (Sub.class.getAnnotation(Inheritable.class) == null)
            throw new AssertionError("@Inherited annotation not inherited by subclass");
        System.out.println("PASS");
    }
}
