/*
 * @test
 * @summary org.ietf.jgss.GSSContext conformance: white-box structure + black-box behavior.
 * @run main GSSContextTest
 */

import org.ietf.jgss.*;
import java.lang.reflect.*;

public class GSSContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static Field field(Class<?> c, String n) { try { return c.getDeclaredField(n); } catch (NoSuchFieldException e) { return null; } }
    static void checkConstInt(Class<?> c, String n) {
        Field f = field(c, n);
        check(f != null && Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                && Modifier.isFinal(f.getModifiers()) && f.getType() == int.class,
                "public static final int " + n);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkConstInt(c, "DEFAULT_LIFETIME");
        checkConstInt(c, "INDEFINITE_LIFETIME");
        String[] names = { "initSecContext", "acceptSecContext", "isEstablished", "dispose",
                "getWrapSizeLimit", "wrap", "unwrap", "getMIC", "verifyMIC", "export",
                "requestMutualAuth", "requestReplayDet", "requestSequenceDet", "requestCredDeleg",
                "requestAnonymity", "requestConf", "requestInteg", "requestLifetime",
                "setChannelBinding", "getCredDelegState", "getMutualAuthState", "getReplayDetState",
                "getSequenceDetState", "getAnonymityState", "isTransferable", "isProtReady",
                "getConfState", "getIntegState", "getLifetime", "getSrcName", "getTargName",
                "getMech", "getDelegCred", "isInitiator" };
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(GSSContext.DEFAULT_LIFETIME == 0, "DEFAULT_LIFETIME == 0");
        check(GSSContext.INDEFINITE_LIFETIME == Integer.MAX_VALUE, "INDEFINITE_LIFETIME == Integer.MAX_VALUE");
        check(GSSContext.DEFAULT_LIFETIME != GSSContext.INDEFINITE_LIFETIME, "lifetime constants distinct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
