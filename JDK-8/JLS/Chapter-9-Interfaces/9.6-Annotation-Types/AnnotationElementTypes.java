/*
 * @test
 * @summary JLS 9.6.1 (Java SE 8): an annotation interface element may have a primitive
 *          type, String, Class, an enum type, an annotation interface, or a
 *          single-dimensional array of one of those types.
 * @run main AnnotationElementTypes
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationElementTypes {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum Color { RED, GREEN }

    @interface Nested { int value(); }

    @Retention(RetentionPolicy.RUNTIME)
    @interface A {
        int i();
        String s();
        Class<?> c();
        Color color();
        Nested nested();
        int[] ints();
        String[] strs();
    }

    @A(i = 1, s = "x", c = String.class, color = Color.RED,
       nested = @Nested(7), ints = {1, 2}, strs = {"a", "b"})
    static class C {}

    public static void main(String[] args) throws Exception {
        A a = C.class.getAnnotation(A.class);
        check(a.i() == 1, "int element");
        check(a.s().equals("x"), "String element");
        check(a.c() == String.class, "Class element");
        check(a.color() == Color.RED, "enum element");
        check(a.nested().value() == 7, "annotation element");
        check(a.ints().length == 2 && a.ints()[1] == 2, "int array element");
        check(a.strs().length == 2 && a.strs()[0].equals("a"), "String array element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
