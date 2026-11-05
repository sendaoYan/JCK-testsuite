/*
 * @test
 * @summary javax.naming.ldap.Control conformance: white-box structure + black-box behavior.
 * @run main ControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import java.util.Arrays;

public class ControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.Control");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(Control.CRITICAL == true, "CRITICAL==true");
        check(Control.NONCRITICAL == false, "NONCRITICAL==false");
        check(hasMethod(c, "getID"), "getID");
        check(hasMethod(c, "isCritical"), "isCritical");
        check(hasMethod(c, "getEncodedValue"), "getEncodedValue");
    }

    static void testBlackBox() {
        // Anonymous implementation verifies the contract is implementable/callable.
        Control c = new Control() {
            public String getID() { return "1.2.3"; }
            public boolean isCritical() { return true; }
            public byte[] getEncodedValue() { return new byte[]{7}; }
        };
        check("1.2.3".equals(c.getID()), "anon getID");
        check(c.isCritical(), "anon isCritical");
        check(Arrays.equals(c.getEncodedValue(), new byte[]{7}), "anon getEncodedValue");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dCtrlSurface();
        dCtrlCustomImplementation();
        System.out.println("deep: Control contracts OK");
    }

    static class DeepControl implements Control {
        final String id; final boolean crit; final byte[] val;
        DeepControl(String id, boolean crit, byte[] val) { this.id = id; this.crit = crit; this.val = val; }
        public String getID() { return id; }
        public boolean isCritical() { return crit; }
        public byte[] getEncodedValue() { return val; }
    }

    static void dCtrlSurface() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : Control.class.getMethods()) names.add(m.getName());
        check(names.contains("getID"), "declares getID");
        check(names.contains("isCritical"), "declares isCritical");
        check(names.contains("getEncodedValue"), "declares getEncodedValue");
        check(Control.class.getMethods().length == 3, "exactly three methods");
    }

    static void dCtrlCustomImplementation() {
        byte[] ber = {0x30, 0x00};
        DeepControl c = new DeepControl("1.2.3.4", true, ber);
        check("1.2.3.4".equals(c.getID()), "getID");
        check(c.isCritical(), "isCritical");
        check(c.getEncodedValue() == ber, "getEncodedValue");
        // BER encoding is abstract at this level: null is a legal answer
        DeepControl nullVal = new DeepControl("x", false, null);
        check(nullVal.getEncodedValue() == null, "null encoded value legal");
        // a control participates in arrays as any other
        Control[] arr = {c, nullVal, new BasicControl("y")};
        check(arr[2].getID().equals("y") && !arr[2].isCritical(), "interop with BasicControl");
    }

}
