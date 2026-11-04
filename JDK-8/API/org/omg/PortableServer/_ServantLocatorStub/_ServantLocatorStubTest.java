/*
 * @test
 * @summary org.omg.PortableServer._ServantLocatorStub conformance: stub/tie class shape.
 * @run main _ServantLocatorStubTest
 */

public class _ServantLocatorStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer._ServantLocatorStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.PortableServer.ServantLocator.class.isAssignableFrom(c), "implements org.omg.PortableServer.ServantLocator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
