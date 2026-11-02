/*
 * @test
 * @summary org.omg.PortableServer.ServantLocator conformance: interface shape.
 * @run main ServantLocatorTest
 */

public class ServantLocatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantLocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantLocatorOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantLocatorOperations");
        check(org.omg.PortableServer.ServantManager.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantManager");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
