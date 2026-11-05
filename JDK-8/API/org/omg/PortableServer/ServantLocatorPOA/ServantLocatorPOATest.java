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
        // ---- deepened (jdk8u372-b07 probed): _all_interfaces exact / _this states / _invoke dispatch ----
        org.omg.PortableServer.ServantLocatorPOA tie = new org.omg.PortableServer.ServantLocatorPOA() {
            public org.omg.PortableServer.Servant preinvoke(byte[] oid, org.omg.PortableServer.POA poa, String op,
                    org.omg.PortableServer.ServantLocatorPackage.CookieHolder cookie) { return null; }
            public void postinvoke(byte[] oid, org.omg.PortableServer.POA poa, String op,
                    java.lang.Object cookie, org.omg.PortableServer.Servant servant) { }
        };
        check(java.util.Arrays.equals(tie._all_interfaces(null, null),
                  new String[]{"IDL:omg.org/PortableServer/ServantLocator:1.0",
                               "IDL:omg.org/PortableServer/ServantManager:1.0"}),
              "_all_interfaces exact repository-id table (Locator is 1.0)");
        try {
            tie._this();
            check(false, "unassociated _this() must throw");
        } catch (org.omg.CORBA.BAD_INV_ORDER expected) {
            check(expected.minor == 0
               && expected.getMessage().equals("The Servant has not been associated with an ORB instance"),
                  "tie inherits Servant unset-delegate BAD_INV_ORDER contract");
        }
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.Object activated = tie._this(orb);
        check(activated != null && activated.toString().startsWith("IOR:"),
              "_this(orb) mints an IOR-backed reference offline");
        check(tie._orb() == orb, "tie._orb() == associated ORB");
        try {
            tie._invoke("bogusop", orb.create_output_stream().create_input_stream(), null);
            check(false, "unknown operation must throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.minor == 0, "_invoke(unknown op) -> BAD_OPERATION minor==0");
        }
        try {
            tie._invoke("preinvoke", orb.create_output_stream().create_input_stream(), null);
            check(false, "preinvoke with empty arguments must throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.minor == 0, "_invoke(preinvoke, empty args) -> BAD_OPERATION minor==0");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
