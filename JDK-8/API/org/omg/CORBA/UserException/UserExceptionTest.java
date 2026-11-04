/*
 * @test
 * @summary org.omg.CORBA.UserException conformance: abstract class shape.
 * @run main UserExceptionTest
 */

public class UserExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.UserException");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(java.lang.Exception.class.isAssignableFrom(c), "extends java.lang.Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
