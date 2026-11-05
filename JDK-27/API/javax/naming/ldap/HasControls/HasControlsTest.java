/*
 * @test
 * @summary javax.naming.ldap.HasControls conformance: white-box structure + black-box behavior.
 * @run main HasControlsTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.HasControls;
import javax.naming.ldap.UnsolicitedNotification;

public class HasControlsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.HasControls");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getControls"), "getControls");
    }

    static void testBlackBox() throws Exception {
        HasControls hc = new HasControls() {
            public Control[] getControls() { return new Control[]{new BasicControl("1.2.3")}; }
        };
        Control[] controls = hc.getControls();
        check(controls != null, "getControls not null");
        check(controls.length == 1, "getControls length");
        check("1.2.3".equals(controls[0].getID()), "getControls content");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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
