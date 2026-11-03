/*
 * @test
 * @summary javax.naming.ldap.ExtendedResponse conformance: white-box structure + black-box behavior.
 * @run main ExtendedResponseTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

public class ExtendedResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.ExtendedResponse");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(hasMethod(c, "getID"), "getID");
        check(hasMethod(c, "getEncodedValue"), "getEncodedValue");
    }

    static void testBlackBox() {
        ExtendedResponse resp = new ExtendedResponse() {
            public String getID() { return "1.2.3"; }
            public byte[] getEncodedValue() { return new byte[]{4, 5}; }
        };
        check("1.2.3".equals(resp.getID()), "getID");
        check(Arrays.equals(resp.getEncodedValue(), new byte[]{4, 5}), "getEncodedValue");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
