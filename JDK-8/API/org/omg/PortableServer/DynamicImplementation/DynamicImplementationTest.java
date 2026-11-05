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
        // ---- deepened (jdk8u372-b07 probed): abstract face / DSI channel / unset delegate ----
        check(c.getSuperclass() == org.omg.PortableServer.Servant.class, "extends Servant");
        java.lang.reflect.Method inv = c.getDeclaredMethod("invoke", org.omg.CORBA.ServerRequest.class);
        check(java.lang.reflect.Modifier.isAbstract(inv.getModifiers()), "invoke(ServerRequest) is abstract (DSI)");
        check(c.getDeclaredMethods().length == 1, "only one own declared method: invoke");
        final java.util.List<String> dsiLog = new java.util.ArrayList<>();
        org.omg.PortableServer.DynamicImplementation dyn =
            new org.omg.PortableServer.DynamicImplementation() {
                public void invoke(org.omg.CORBA.ServerRequest request) { dsiLog.add(request + ":invoked"); }
                public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] oid) {
                    return new String[]{"IDL:DeepE/DSI:1.0"};
                }
            };
        check(dyn instanceof org.omg.PortableServer.Servant, "DSI servant is a Servant");
        check(java.util.Arrays.equals(dyn._all_interfaces(null, null), new String[]{"IDL:DeepE/DSI:1.0"}),
              "anonymous DSI subclass routes _all_interfaces");
        try {
            dyn._orb();
            check(false, "unset delegate must throw");
        } catch (org.omg.CORBA.BAD_INV_ORDER expected) {
            check(expected.getMessage().equals("The Servant has not been associated with an ORB instance"),
                  "inherits Servant unset-delegate BAD_INV_ORDER contract");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
