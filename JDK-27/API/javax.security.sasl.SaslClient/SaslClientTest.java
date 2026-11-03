/*
 * @test
 * @summary javax.security.sasl.SaslClient conformance: white-box structure + black-box behavior.
 * @run main SaslClientTest
 */

import java.lang.reflect.*;
import javax.security.sasl.*;
import javax.security.auth.callback.*;

public class SaslClientTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.SaslClient");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getMechanismName","hasInitialResponse","evaluateChallenge","isComplete",
                                     "unwrap","wrap","getNegotiatedProperty","dispose"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CallbackHandler h = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof NameCallback) ((NameCallback) cb).setName("user");
                else if (cb instanceof PasswordCallback) ((PasswordCallback) cb).setPassword("pass".toCharArray());
            }
        };
        SaslClient sc = Sasl.createSaslClient(new String[]{"PLAIN"}, null, "PLAIN", "localhost", null, h);
        check(sc != null, "PLAIN client via factory");
        check("PLAIN".equals(sc.getMechanismName()), "getMechanismName");
        check(sc.hasInitialResponse(), "hasInitialResponse");
        check(!sc.isComplete(), "isComplete before evaluate");

        try { sc.wrap(new byte[]{1, 2}, 0, 2); check(false, "wrap before complete should throw"); }
        catch (IllegalStateException e) { check(true, "wrap before complete throws IllegalStateException"); }
        try { sc.unwrap(new byte[]{1, 2}, 0, 2); check(false, "unwrap before complete should throw"); }
        catch (IllegalStateException e) { check(true, "unwrap before complete throws IllegalStateException"); }

        byte[] r = sc.evaluateChallenge(new byte[0]);
        check(r != null && r.length > 0, "evaluateChallenge returns initial response");
        check(sc.isComplete(), "isComplete after evaluate");

        try { sc.wrap(new byte[]{1, 2}, 0, 2); check(false, "wrap after complete should throw"); }
        catch (SaslException e) { check(true, "wrap after complete throws SaslException (no integrity)"); }
        try { sc.unwrap(new byte[]{1, 2}, 0, 2); check(false, "unwrap after complete should throw"); }
        catch (SaslException e) { check(true, "unwrap after complete throws SaslException"); }

        check("auth".equals(sc.getNegotiatedProperty(Sasl.QOP)), "getNegotiatedProperty(qop)");
        check(sc.getNegotiatedProperty("no.such.property") == null, "getNegotiatedProperty unknown null");
        sc.dispose();
        check(true, "dispose callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
