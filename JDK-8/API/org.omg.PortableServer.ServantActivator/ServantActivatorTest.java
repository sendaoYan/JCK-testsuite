/*
 * @test
 * @summary org.omg.PortableServer.ServantActivator conformance: interface shape.
 * @run main ServantActivatorTest
 */

public class ServantActivatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantActivator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantActivatorOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantActivatorOperations");
        check(org.omg.PortableServer.ServantManager.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantManager");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
