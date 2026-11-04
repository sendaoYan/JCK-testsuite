/*
 * @test
 * @summary org.omg.CORBA_2_3.ORB conformance: abstract class shape.
 * @run main ORBTest
 */

public class ORBTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA_2_3.ORB");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.ORB.class.isAssignableFrom(c), "extends org.omg.CORBA.ORB");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
