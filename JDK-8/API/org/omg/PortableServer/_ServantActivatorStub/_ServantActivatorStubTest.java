/*
 * @test
 * @summary org.omg.PortableServer._ServantActivatorStub conformance: stub/tie class shape.
 * @run main _ServantActivatorStubTest
 */

public class _ServantActivatorStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer._ServantActivatorStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.PortableServer.ServantActivator.class.isAssignableFrom(c), "implements org.omg.PortableServer.ServantActivator");
        // ---- deepened (jdk8u372-b07 probed): _ids exact / no-delegate BAD_OPERATION matrix ----
        org.omg.PortableServer._ServantActivatorStub stub = new org.omg.PortableServer._ServantActivatorStub();
        check(stub._ids().length == 2
           && stub._ids()[0].equals("IDL:omg.org/PortableServer/ServantActivator:2.3")
           && stub._ids()[1].equals("IDL:omg.org/PortableServer/ServantManager:1.0"),
              "_ids() exact two-element table: own 2.3 repo id + base ServantManager 1.0");
        check(org.omg.PortableServer._ServantActivatorStub._opsClass == org.omg.PortableServer.ServantActivatorOperations.class,
              "_opsClass identity: ServantActivatorOperations");
        check(org.omg.PortableServer._ServantActivatorStub._opsClass.isInterface(), "_opsClass is the Operations interface");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "stub class is not final");
        check(stub instanceof org.omg.CORBA.portable.ObjectImpl && stub instanceof org.omg.CORBA.portable.IDLEntity,
              "stub is ObjectImpl + IDLEntity");
        try {
            stub._get_delegate();
            check(false, "no-delegate _get_delegate must throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.minor == 0 && expected.getMessage().equals("The delegate has not been set!"),
                  "no-delegate -> BAD_OPERATION minor==0, message exact");
        }
        try {
            stub.incarnate(null, null);
            check(false, "no-delegate incarnate must throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.minor == 0 && expected.getMessage().equals("The delegate has not been set!"),
                  "incarnate without delegate -> BAD_OPERATION 'The delegate has not been set!'");
        }
        try {
            stub.etherealize(null, null, null, false, false);
            check(false, "no-delegate etherealize must throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.minor == 0 && expected.getMessage().equals("The delegate has not been set!"),
                  "etherealize without delegate -> BAD_OPERATION 'The delegate has not been set!'");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
