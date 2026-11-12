/*
 * @test
 * @summary JVMS 4.7.20/4.7.21: a RUNTIME TYPE_USE/TYPE_PARAMETER annotation
 *          placed at each of the type-annotation target positions — type
 *          parameter declaration, supertype, field type, method return type,
 *          formal parameter type, throws clause and receiver type — must be
 *          carried by RuntimeVisibleTypeAnnotations and readable back through
 *          java.lang.reflect at the matching position.
 * @run main TypeAnnotationTargetTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;

public class TypeAnnotationTargetTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
    @interface A {}

    static class Super {}

    static class Generic<@A T> extends @A Super {
        @A String field;

        @A String ret() { return null; }

        void param(@A String s) {}

        void thr() throws @A Exception {}

        void recv(@A Generic<T> this) {}
    }

    public static void main(String[] args) throws Exception {
        int n = 0;

        // target_type 0x00: class type parameter declaration
        TypeVariable<?> tv = (TypeVariable<?>) Generic.class.getTypeParameters()[0];
        if (tv.getAnnotation(A.class) == null)
            throw new AssertionError("type-parameter declaration annotation missing");
        n++;

        // target_type 0x10: supertype
        if (Generic.class.getAnnotatedSuperclass().getAnnotation(A.class) == null)
            throw new AssertionError("supertype annotation missing");
        n++;

        // target_type 0x13: field type
        if (Generic.class.getDeclaredField("field").getAnnotatedType().getAnnotation(A.class) == null)
            throw new AssertionError("field-type annotation missing");
        n++;

        // target_type 0x14: method return type
        if (Generic.class.getDeclaredMethod("ret").getAnnotatedReturnType().getAnnotation(A.class) == null)
            throw new AssertionError("return-type annotation missing");
        n++;

        // target_type 0x16: formal parameter type
        if (Generic.class.getDeclaredMethod("param", String.class)
                .getAnnotatedParameterTypes()[0].getAnnotation(A.class) == null)
            throw new AssertionError("parameter-type annotation missing");
        n++;

        // target_type 0x17: throws clause
        if (Generic.class.getDeclaredMethod("thr")
                .getAnnotatedExceptionTypes()[0].getAnnotation(A.class) == null)
            throw new AssertionError("throws annotation missing");
        n++;

        // target_type 0x15: receiver type
        AnnotatedType recv = Generic.class.getDeclaredMethod("recv").getAnnotatedReceiverType();
        if (recv == null || recv.getAnnotation(A.class) == null)
            throw new AssertionError("receiver-type annotation missing");
        n++;

        System.out.println("PASS (" + n + " target positions)");
    }
}
