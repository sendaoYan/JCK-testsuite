/*
 * @test
 * @summary org.omg.CORBA.ServerRequest conformance: abstract class shape.
 * @run main ServerRequestTest
 */

public class ServerRequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ServerRequest");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
