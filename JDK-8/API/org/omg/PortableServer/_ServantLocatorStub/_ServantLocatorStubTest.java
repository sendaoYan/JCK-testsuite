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
        // ---- deepened (jdk8u372-b07 probed): _ids exact / no-delegate BAD_OPERATION matrix ----
        org.omg.PortableServer._ServantLocatorStub stub = new org.omg.PortableServer._ServantLocatorStub();
        check(stub._ids().length == 2
           && stub._ids()[0].equals("IDL:omg.org/PortableServer/ServantLocator:1.0")
           && stub._ids()[1].equals("IDL:omg.org/PortableServer/ServantManager:1.0"),
              "_ids() exact: own repo id is 1.0 (vs ServantActivator's 2.3) + base ServantManager 1.0");
        check(org.omg.PortableServer._ServantLocatorStub._opsClass == org.omg.PortableServer.ServantLocatorOperations.class,
              "_opsClass identity: ServantLocatorOperations");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "stub class is not final");
        try {
            stub.preinvoke(null, null, null, null);
            check(false, "no-delegate preinvoke must throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.minor == 0 && expected.getMessage().equals("The delegate has not been set!"),
                  "preinvoke without delegate -> BAD_OPERATION 'The delegate has not been set!'");
        }
        try {
            stub.postinvoke(null, null, null, null, null);
            check(false, "no-delegate postinvoke must throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.minor == 0 && expected.getMessage().equals("The delegate has not been set!"),
                  "postinvoke without delegate -> BAD_OPERATION 'The delegate has not been set!'");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
