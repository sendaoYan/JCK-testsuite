/*
 * @test
 * @summary java.lang.reflect.GenericDeclaration conformance: getTypeParameters on class/method.
 * @run main GenericDeclarationTest
 */

import java.lang.reflect.GenericDeclaration;

public class GenericDeclarationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample<T extends Number> {
    }

    public static <X> void m() {
    }

    static void testGenericDeclaration() throws Exception {
        GenericDeclaration c = Sample.class;
        check(c.getTypeParameters().length == 1, "class type parameter count");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter name");

        GenericDeclaration m = GenericDeclarationTest.class.getMethod("m");
        check(m.getTypeParameters().length == 1, "method type parameter count");
        check("X".equals(m.getTypeParameters()[0].getName()), "method type parameter name");
    }

    public static void main(String[] args) throws Exception {
        testGenericDeclaration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
