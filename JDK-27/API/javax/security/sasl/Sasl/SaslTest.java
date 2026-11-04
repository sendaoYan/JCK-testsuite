/*
 * @test
 * @summary javax.security.sasl.Sasl conformance: white-box structure + black-box behavior.
 * @run main SaslTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.security.sasl.*;
import javax.security.auth.callback.*;

public class SaslTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasField(Class<?> c, String n) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static void checkFields(Class<?> c, String[] ns) { for (String n : ns) check(hasField(c, n), "field " + n); }

    static final CallbackHandler PLAIN_HANDLER = callbacks -> {
        for (Callback cb : callbacks) {
            if (cb instanceof NameCallback) ((NameCallback) cb).setName("user");
            else if (cb instanceof PasswordCallback) ((PasswordCallback) cb).setPassword("pass".toCharArray());
        }
    };

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.Sasl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "class, not interface");
        checkMethods(c, new String[]{"createSaslClient","createSaslServer","getSaslClientFactories","getSaslServerFactories"});
        checkFields(c, new String[]{"QOP","STRENGTH","SERVER_AUTH","BOUND_SERVER_NAME","MAX_BUFFER","RAW_SEND_SIZE",
                                    "REUSE","POLICY_NOPLAINTEXT","POLICY_NOACTIVE","POLICY_NODICTIONARY",
                                    "POLICY_NOANONYMOUS","POLICY_FORWARD_SECRECY","POLICY_PASS_CREDENTIALS","CREDENTIALS"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check("javax.security.sasl.qop".equals(Sasl.QOP), "QOP");
        check("javax.security.sasl.strength".equals(Sasl.STRENGTH), "STRENGTH");
        check("javax.security.sasl.server.authentication".equals(Sasl.SERVER_AUTH), "SERVER_AUTH");
        check("javax.security.sasl.bound.server.name".equals(Sasl.BOUND_SERVER_NAME), "BOUND_SERVER_NAME");
        check("javax.security.sasl.maxbuffer".equals(Sasl.MAX_BUFFER), "MAX_BUFFER");
        check("javax.security.sasl.rawsendsize".equals(Sasl.RAW_SEND_SIZE), "RAW_SEND_SIZE");
        check("javax.security.sasl.reuse".equals(Sasl.REUSE), "REUSE");
        check("javax.security.sasl.policy.noplaintext".equals(Sasl.POLICY_NOPLAINTEXT), "POLICY_NOPLAINTEXT");
        check("javax.security.sasl.policy.noactive".equals(Sasl.POLICY_NOACTIVE), "POLICY_NOACTIVE");
        check("javax.security.sasl.policy.nodictionary".equals(Sasl.POLICY_NODICTIONARY), "POLICY_NODICTIONARY");
        check("javax.security.sasl.policy.noanonymous".equals(Sasl.POLICY_NOANONYMOUS), "POLICY_NOANONYMOUS");
        check("javax.security.sasl.policy.forward".equals(Sasl.POLICY_FORWARD_SECRECY), "POLICY_FORWARD_SECRECY");
        check("javax.security.sasl.policy.credentials".equals(Sasl.POLICY_PASS_CREDENTIALS), "POLICY_PASS_CREDENTIALS");
        check("javax.security.sasl.credentials".equals(Sasl.CREDENTIALS), "CREDENTIALS");

        Enumeration<SaslClientFactory> cf = Sasl.getSaslClientFactories();
        check(cf != null && cf.hasMoreElements(), "client factories non-empty");
        Enumeration<SaslServerFactory> sf = Sasl.getSaslServerFactories();
        check(sf != null && sf.hasMoreElements(), "server factories non-empty");

        // PLAIN client with callback handler
        SaslClient sc = Sasl.createSaslClient(new String[]{"PLAIN"}, null, "PLAIN", "localhost", null, PLAIN_HANDLER);
        check(sc != null, "PLAIN client non-null");
        check("PLAIN".equals(sc.getMechanismName()), "PLAIN mechanism");
        check(sc.hasInitialResponse(), "PLAIN hasInitialResponse");
        check(!sc.isComplete(), "PLAIN not complete before evaluate");
        byte[] resp = sc.evaluateChallenge(new byte[0]);
        check(resp != null && resp.length > 0, "PLAIN initial response non-empty");
        check(sc.isComplete(), "PLAIN complete after evaluate");
        check("auth".equals(sc.getNegotiatedProperty(Sasl.QOP)), "PLAIN negotiated qop=auth");
        sc.dispose();

        // EXTERNAL client requires no handler
        SaslClient ext = Sasl.createSaslClient(new String[]{"EXTERNAL"}, null, "EXTERNAL", "localhost", null, null);
        check(ext != null, "EXTERNAL client non-null");
        check("EXTERNAL".equals(ext.getMechanismName()), "EXTERNAL mechanism");

        // Unknown mechanism returns null
        check(Sasl.createSaslClient(new String[]{"NO-SUCH-MECH"}, null, null, null, null, null) == null, "unknown client mechanism null");

        // PLAIN client without handler throws
        try { Sasl.createSaslClient(new String[]{"PLAIN"}, null, "PLAIN", "localhost", null, null); check(false, "PLAIN no handler should throw"); }
        catch (SaslException e) { check(true, "PLAIN no handler throws SaslException"); }

        // Server mechanisms: PLAIN server is not provided by default providers
        check(Sasl.createSaslServer("PLAIN", "PLAIN", "localhost", null, PLAIN_HANDLER) == null, "PLAIN server null");
        try { Sasl.createSaslServer("DIGEST-MD5", "DIGEST-MD5", "localhost", null, null); check(false, "DIGEST-MD5 no handler should throw"); }
        catch (SaslException e) { check(true, "DIGEST-MD5 no handler throws SaslException"); }
        try { Sasl.createSaslServer("CRAM-MD5", "CRAM-MD5", "localhost", null, null); check(false, "CRAM-MD5 no handler should throw"); }
        catch (SaslException e) { check(true, "CRAM-MD5 no handler throws SaslException"); }
        check(Sasl.createSaslServer("NO-SUCH-MECH", null, null, null, null) == null, "unknown server mechanism null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
