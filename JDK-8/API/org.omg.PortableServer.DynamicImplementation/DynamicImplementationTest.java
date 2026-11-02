/*
 * @test
 * @summary org.omg.PortableServer.DynamicImplementation conformance: abstract class shape.
 * @run main DynamicImplementationTest
 */

public class DynamicImplementationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.DynamicImplementation");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.PortableServer.Servant.class.isAssignableFrom(c), "extends org.omg.PortableServer.Servant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
