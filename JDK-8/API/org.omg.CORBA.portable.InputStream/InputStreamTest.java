/*
 * @test
 * @summary org.omg.CORBA.portable.InputStream conformance: abstract class shape.
 * @run main InputStreamTest
 */

public class InputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.InputStream");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(java.io.InputStream.class.isAssignableFrom(c), "extends java.io.InputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
