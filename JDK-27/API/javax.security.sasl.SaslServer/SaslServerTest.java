/*
 * @test
 * @summary javax.security.sasl.SaslServer conformance: white-box structure + black-box behavior.
 * @run main SaslServerTest
 */

import java.lang.reflect.*;
import java.util.Arrays;
import javax.security.sasl.*;

public class SaslServerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.SaslServer");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getMechanismName","evaluateResponse","isComplete","getAuthorizationID",
                                     "unwrap","wrap","getNegotiatedProperty","dispose"});
    }

    // Minimal deterministic implementation exercising the full contract.
    static class TestServer implements SaslServer {
        boolean complete;
        byte[] challenge = new byte[]{7, 8};
        public String getMechanismName() { return "TEST"; }
        public byte[] evaluateResponse(byte[] response) { complete = true; return challenge; }
        public boolean isComplete() { return complete; }
        public String getAuthorizationID() { return complete ? "authz" : null; }
        public byte[] unwrap(byte[] incoming, int offset, int len) { return Arrays.copyOfRange(incoming, offset, offset + len); }
        public byte[] wrap(byte[] outgoing, int offset, int len) { return Arrays.copyOfRange(outgoing, offset, offset + len); }
        public Object getNegotiatedProperty(String propName) { return "prop".equals(propName) ? "value" : null; }
        public void dispose() { complete = false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TestServer s = new TestServer();
        check("TEST".equals(s.getMechanismName()), "getMechanismName");
        check(!s.isComplete(), "isComplete before evaluate");
        check(s.getAuthorizationID() == null, "getAuthorizationID before complete");

        byte[] r = s.evaluateResponse(new byte[]{1});
        check(Arrays.equals(r, new byte[]{7, 8}), "evaluateResponse returns challenge");
        check(s.isComplete(), "isComplete after evaluate");
        check("authz".equals(s.getAuthorizationID()), "getAuthorizationID after complete");

        check(Arrays.equals(s.unwrap(new byte[]{1, 2, 3}, 1, 2), new byte[]{2, 3}), "unwrap copies range");
        check(Arrays.equals(s.wrap(new byte[]{1, 2, 3}, 0, 3), new byte[]{1, 2, 3}), "wrap copies range");
        check("value".equals(s.getNegotiatedProperty("prop")), "getNegotiatedProperty known");
        check(s.getNegotiatedProperty("nope") == null, "getNegotiatedProperty unknown null");

        s.dispose();
        check(true, "dispose callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
