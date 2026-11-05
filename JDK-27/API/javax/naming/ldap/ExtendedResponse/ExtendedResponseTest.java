/*
 * @test
 * @summary javax.naming.ldap.ExtendedResponse conformance: white-box structure + black-box behavior.
 * @run main ExtendedResponseTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import java.util.Arrays;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dExSurface();
        dExUse();
        System.out.println("deep: ExtendedResponse contracts OK");
    }

    static void dExSurface() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : ExtendedResponse.class.getMethods()) names.add(m.getName());
        check(names.contains("getID"), "declares getID");
        check(names.contains("getEncodedValue"), "declares getEncodedValue");
        check(ExtendedResponse.class.getMethods().length == 2, "exactly two methods");
        check(BasicControl.class.isAssignableFrom(ExtendedResponse.class) == false,
                "independent of BasicControl");
    }

    static class DeepResp implements ExtendedResponse {
        final String id; final byte[] val;
        DeepResp(String id, byte[] val) { this.id = id; this.val = val; }
        public String getID() { return id; }
        public byte[] getEncodedValue() { return val; }
    }

    static void dExUse() throws Exception {
        DeepResp r = new DeepResp("1.2.3", new byte[]{5});
        check("1.2.3".equals(r.getID()), "getID");
        check(r.getEncodedValue()[0] == 5, "getEncodedValue");
        // null value is a legal response payload (valueless extended ops)
        DeepResp n = new DeepResp("x", null);
        check(n.getEncodedValue() == null, "null payload legal");
        // responses flow through ExtendedRequest.createExtendedResponse contracts
        javax.naming.ldap.ExtendedRequest req = new javax.naming.ldap.ExtendedRequest() {
            public String getID() { return "req"; }
            public byte[] getEncodedValue() { return null; }
            public ExtendedResponse createExtendedResponse(String id, byte[] berValue,
                    int offset, int length) { return new DeepResp(id, berValue); }
        };
        ExtendedResponse back = req.createExtendedResponse("resp", new byte[]{7}, 0, 1);
        check("resp".equals(back.getID()) && back.getEncodedValue()[0] == 7,
                "request-to-response handoff");
    }

}
