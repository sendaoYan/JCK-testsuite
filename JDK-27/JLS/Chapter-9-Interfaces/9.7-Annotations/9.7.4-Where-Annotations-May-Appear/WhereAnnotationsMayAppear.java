/*
 * @test
 * @summary JLS 9.7.4: annotations may appear on declarations and type uses,
 *          including class, method, field, parameter, local variable, and type
 *          argument positions.
 * @run main WhereAnnotationsMayAppear
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class WhereAnnotationsMayAppear {
    @Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.FIELD,
             ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface A {}

    @A
    static class C<@A T> {
        @A String field;

        @A T identity(@A T t) {
            @A T local = t;
            return local;
        }
    }

    public static void main(String[] args) {
        if (C.class.getDeclaredFields()[0].getAnnotation(A.class) == null)
            throw new AssertionError("field annotation");
        System.out.println("PASS");
    }
}
