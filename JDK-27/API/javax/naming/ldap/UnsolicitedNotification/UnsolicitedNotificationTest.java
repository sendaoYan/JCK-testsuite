/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotification conformance: white-box structure + black-box behavior.
 * @run main UnsolicitedNotificationTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.ldap.*;

public class UnsolicitedNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.UnsolicitedNotification");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(ExtendedResponse.class.isAssignableFrom(c), "extends ExtendedResponse");
        check(HasControls.class.isAssignableFrom(c), "extends HasControls");
        check(hasMethod(c, "getReferrals"), "getReferrals");
        check(hasMethod(c, "getException"), "getException");
    }

    static void testBlackBox() throws Exception {
        final NamingException ex = new NamingException("notify");
        UnsolicitedNotification n = new UnsolicitedNotification() {
            public String[] getReferrals() { return new String[]{"ldap://a"}; }
            public NamingException getException() { return ex; }
            public String getID() { return "1.2.3"; }
            public byte[] getEncodedValue() { return null; }
            public Control[] getControls() { return new Control[]{new BasicControl("c1")}; }
        };
        check(Arrays.equals(n.getReferrals(), new String[]{"ldap://a"}), "getReferrals");
        check(n.getException() == ex, "getException");
        check("1.2.3".equals(n.getID()), "getID");
        check(n.getEncodedValue() == null, "getEncodedValue");
        check(n.getControls().length == 1, "getControls");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
