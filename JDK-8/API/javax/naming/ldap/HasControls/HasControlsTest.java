/*
 * @test
 * @summary javax.naming.ldap.HasControls conformance: interface hierarchy and members.
 * @run main HasControlsTest
 */



import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.HasControls;
import javax.naming.ldap.UnsolicitedNotification;
public class HasControlsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.HasControls");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getControls"), "has method getControls");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dHcSurface();
        dHcUse();
        System.out.println("deep: HasControls contracts OK");
    }

    static void dHcSurface() throws Exception {
        java.lang.reflect.Method[] ms = HasControls.class.getMethods();
        check(ms.length == 1 && ms[0].getName().equals("getControls"),
                "exactly one method: getControls");
        check(HasControls.class.isInterface(), "is interface");
    }

    static class DeepWithControls implements HasControls {
        final Control[] controls;
        DeepWithControls(Control[] controls) { this.controls = controls; }
        public Control[] getControls() throws NamingException { return controls; }
    }

    static class DeepThrowing implements HasControls {
        public Control[] getControls() throws NamingException {
            throw new javax.naming.CommunicationException("controls unavailable");
        }
    }

    static void dHcUse() throws Exception {
        Control[] cs = {new BasicControl("1.1", true, null), new BasicControl("2.2")};
        DeepWithControls h = new DeepWithControls(cs);
        check(h.getControls() == cs, "returns the control array");
        check(h.getControls().length == 2, "array length");
        // empty and null arrays are both meaningful
        check(new DeepWithControls(new Control[0]).getControls().length == 0, "empty array");
        check(new DeepWithControls(null).getControls() == null, "null array (no controls)");
        // implementations may throw NamingException to signal failure
        boolean threw = false;
        try { new DeepThrowing().getControls(); }
        catch (javax.naming.CommunicationException e) { threw = true; }
        check(threw, "NamingException propagation");
        // UnsolicitedNotification extends HasControls
        check(HasControls.class.isAssignableFrom(javax.naming.ldap.UnsolicitedNotification.class),
                "UnsolicitedNotification is a HasControls");
    }

}
