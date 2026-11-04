/*
 * @test
 * @summary org.omg.CORBA.portable.ObjectImpl conformance: abstract class shape.
 * @run main ObjectImplTest
 */

public class ObjectImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.ObjectImpl");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "implements org.omg.CORBA.Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
