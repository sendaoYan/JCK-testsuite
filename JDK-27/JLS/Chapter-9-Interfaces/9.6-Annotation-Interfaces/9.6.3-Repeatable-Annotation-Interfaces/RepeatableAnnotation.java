/*
 * @test
 * @summary JLS 9.6.3: a repeatable annotation interface can be applied more than
 *          once; the repeated annotations are available via getAnnotationsByType.
 * @run main RepeatableAnnotation
 */

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RepeatableAnnotation {
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Foos.class)
    @interface Foo { int value(); }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Foos { Foo[] value(); }

    @Foo(1) @Foo(2)
    static class C {}

    public static void main(String[] args) {
        Foo[] foos = C.class.getAnnotationsByType(Foo.class);
        if (foos.length != 2 || foos[0].value() != 1 || foos[1].value() != 2)
            throw new AssertionError("repeatable count/values");
        System.out.println("PASS");
    }
}
