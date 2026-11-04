/*
 * @test
 * @summary org.omg.CORBA.SystemException conformance: abstract class shape.
 * @run main SystemExceptionTest
 */

public class SystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.SystemException");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(java.lang.RuntimeException.class.isAssignableFrom(c), "extends java.lang.RuntimeException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
