/*
 * @test
 * @summary javax.naming.ldap.ExtendedRequest conformance: white-box structure + black-box behavior.
 * @run main ExtendedRequestTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

public class ExtendedRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.ExtendedRequest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(hasMethod(c, "getID"), "getID");
        check(hasMethod(c, "getEncodedValue"), "getEncodedValue");
        check(hasMethod(c, "createExtendedResponse"), "createExtendedResponse");
    }

    static void testBlackBox() throws Exception {
        ExtendedRequest req = new ExtendedRequest() {
            public String getID() { return "1.2.3.4"; }
            public byte[] getEncodedValue() { return new byte[]{1}; }
            public ExtendedResponse createExtendedResponse(String id, byte[] berValue, int offset, int length) {
                return new ExtendedResponse() {
                    public String getID() { return id; }
                    public byte[] getEncodedValue() { return new byte[]{9}; }
                };
            }
        };
        check("1.2.3.4".equals(req.getID()), "getID");
        check(Arrays.equals(req.getEncodedValue(), new byte[]{1}), "getEncodedValue");
        check(req.createExtendedResponse("x", null, 0, 0) != null, "createExtendedResponse callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
