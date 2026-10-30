/*
 * @test
 * @summary JLS 9.6.4.2: a Retention(SOURCE) annotation is not recorded in the
 *          class file, while Retention(RUNTIME) is available reflectively.
 * @run main RetentionAnnotation
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RetentionAnnotation {
    @Retention(RetentionPolicy.SOURCE)
    @interface SourceOnly {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface RuntimeOnly {}

    @SourceOnly @RuntimeOnly
    static class C {}

    public static void main(String[] args) {
        if (C.class.getAnnotation(RuntimeOnly.class) == null)
            throw new AssertionError("RUNTIME annotation was not retained");
        if (C.class.getAnnotation(SourceOnly.class) != null)
            throw new AssertionError("SOURCE annotation was retained");
        System.out.println("PASS");
    }
}
