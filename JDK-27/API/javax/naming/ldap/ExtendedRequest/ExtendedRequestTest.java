/*
 * @test
 * @summary javax.naming.ldap.ExtendedRequest conformance: white-box structure + black-box behavior.
 * @run main ExtendedRequestTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import java.util.Arrays;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dErInterface();
        dErResponseCreation();
        System.out.println("deep: ExtendedRequest contracts OK");
    }

    static class DeepExtReq implements ExtendedRequest {
        final java.util.List<String> log;
        DeepExtReq(java.util.List<String> log) { this.log = log; }
        public String getID() { return "1.2.3.4"; }
        public byte[] getEncodedValue() { return new byte[]{0x7f}; }
        public ExtendedResponse createExtendedResponse(String id, byte[] berValue,
                int offset, int length) {
            log.add(id + ":" + offset + ":" + length);
            byte[] slice = new byte[length];
            System.arraycopy(berValue, offset, slice, 0, length);
            return new DeepExtResp(id, slice);
        }
    }

    static class DeepExtResp implements ExtendedResponse {
        final String id; final byte[] val;
        DeepExtResp(String id, byte[] val) { this.id = id; this.val = val; }
        public String getID() { return id; }
        public byte[] getEncodedValue() { return val; }
    }

    static void dErInterface() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : ExtendedRequest.class.getMethods()) names.add(m.getName());
        check(names.contains("getID"), "declares getID");
        check(names.contains("getEncodedValue"), "declares getEncodedValue");
        check(names.contains("createExtendedResponse"), "declares createExtendedResponse");
        check(ExtendedRequest.class.getMethods().length == 3, "exactly three methods");
    }

    static void dErResponseCreation() {
        java.util.List<String> log = new java.util.ArrayList<>();
        DeepExtReq req = new DeepExtReq(log);
        check("1.2.3.4".equals(req.getID()), "getID");
        check(req.getEncodedValue().length == 1 && req.getEncodedValue()[0] == 0x7f,
                "getEncodedValue");
        byte[] ber = {1, 2, 3, 4, 5};
        ExtendedResponse resp = req.createExtendedResponse("resp.oid", ber, 1, 3);
        check("resp.oid".equals(resp.getID()), "response carries id");
        check(java.util.Arrays.equals(resp.getEncodedValue(), new byte[]{2, 3, 4}),
                "offset/length slice honored");
        check(log.get(0).equals("resp.oid:1:3"), "arguments forwarded");
        // zero-length value is legal
        ExtendedResponse empty = req.createExtendedResponse("x", ber, 0, 0);
        check(empty.getEncodedValue().length == 0, "zero-length payload");
    }

}
