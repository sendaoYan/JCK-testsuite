/*
 * @test
 * @summary org.omg.CORBA_2_3.portable.OutputStream conformance: abstract class shape.
 * @run main OutputStreamTest
 */

public class OutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA_2_3.portable.OutputStream");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.portable.OutputStream.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.OutputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
