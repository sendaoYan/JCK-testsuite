/*
 * @test
 * @summary org.omg.PortableServer.ServantLocatorPOA conformance: abstract class shape.
 * @run main ServantLocatorPOATest
 */

public class ServantLocatorPOATest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantLocatorPOA");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.PortableServer.Servant.class.isAssignableFrom(c), "extends org.omg.PortableServer.Servant");
        check(org.omg.PortableServer.ServantLocatorOperations.class.isAssignableFrom(c), "implements org.omg.PortableServer.ServantLocatorOperations");
        check(org.omg.CORBA.portable.InvokeHandler.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.InvokeHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
