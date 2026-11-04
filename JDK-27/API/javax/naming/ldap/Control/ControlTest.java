/*
 * @test
 * @summary javax.naming.ldap.Control conformance: white-box structure + black-box behavior.
 * @run main ControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
