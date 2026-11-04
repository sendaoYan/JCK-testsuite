/*
 * @test
 * @summary org.omg.CORBA.Request conformance: abstract class shape.
 * @run main RequestTest
 */

public class RequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.Request");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
