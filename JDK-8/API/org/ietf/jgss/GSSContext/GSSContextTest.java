/*
 * @test
 * @summary org.ietf.jgss.GSSContext conformance: interface face, lifetime sentinels, offline context state.
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
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == null, "no superclass");
        checkConstInt(c, "DEFAULT_LIFETIME");
        checkConstInt(c, "INDEFINITE_LIFETIME");
        String[] names = { "acceptSecContext", "dispose", "export", "getAnonymityState",
                "getConfState", "getCredDelegState", "getDelegCred", "getIntegState", "getLifetime",
                "getMIC", "getMech", "getMutualAuthState", "getReplayDetState", "getSequenceDetState",
                "getSrcName", "getTargName", "getWrapSizeLimit", "initSecContext", "isEstablished",
                "isInitiator", "isProtReady", "isTransferable", "requestAnonymity", "requestConf",
                "requestCredDeleg", "requestInteg", "requestLifetime", "requestMutualAuth",
                "requestReplayDet", "requestSequenceDet", "setChannelBinding", "unwrap", "verifyMIC", "wrap" };
        for (String n : names) check(hasMethod(c, n), "declares " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // lifetime sentinels
        check(GSSContext.DEFAULT_LIFETIME == 0, "DEFAULT_LIFETIME == 0");
        check(GSSContext.INDEFINITE_LIFETIME == Integer.MAX_VALUE, "INDEFINITE_LIFETIME == Integer.MAX_VALUE");
        check(GSSContext.DEFAULT_LIFETIME != GSSContext.INDEFINITE_LIFETIME, "lifetime constants distinct");

        // a real (unestablished) initiator context can be created offline: creation only
        // records the peer/mech/lifetime, no KDC round trip happens until initSecContext
        GSSManager m = GSSManager.getInstance();
        GSSName peer = m.createName("user@EXAMPLE.COM", GSSName.NT_USER_NAME);
        Oid krb5 = new Oid("1.2.840.113554.1.2.2");
        GSSContext ctx = m.createContext(peer, krb5, null, GSSContext.DEFAULT_LIFETIME);
        check(ctx != null, "createContext non-null");
        check(!ctx.isEstablished(), "fresh context not established");
        check(ctx.isInitiator(), "fresh context is initiator");
        check(!ctx.isProtReady(), "fresh context not protReady");
        check(!ctx.isTransferable(), "krb5 context not transferable");
        check(krb5.equals(ctx.getMech()), "context mech is krb5");
        check(ctx.getLifetime() == GSSContext.INDEFINITE_LIFETIME
                || ctx.getLifetime() == GSSContext.DEFAULT_LIFETIME, "fresh context lifetime sentinel");
        check(peer.equals(ctx.getTargName()), "targName is the peer passed in");
        check(ctx.getConfState(), "default conf state true");
        check(ctx.getIntegState(), "default integ state true");
        check(!ctx.getCredDelegState(), "default credDeleg false");
        check(ctx.getMutualAuthState(), "default mutualAuth true");
        check(ctx.getReplayDetState(), "default replayDet true");
        check(ctx.getSequenceDetState(), "default sequenceDet true");
        check(!ctx.getAnonymityState(), "default anonymity false");

        // srcName on an unestablished context dereferences a null mech context (JDK
        // implementation quirk, identical on JDK 8 and JDK 27; documented)
        try {
            ctx.getSrcName();
            check(false, "expected NPE for getSrcName on unestablished context");
        } catch (NullPointerException expected) {
            check(true, "getSrcName unestablished -> NPE (implementation quirk)");
        }

        // request* setters on a fresh context take effect immediately
        ctx.requestConf(false);
        ctx.requestInteg(true);
        ctx.requestMutualAuth(true);
        ctx.requestCredDeleg(false);
        ctx.requestAnonymity(false);
        ctx.requestReplayDet(true);
        ctx.requestSequenceDet(true);
        ctx.requestLifetime(3600);
        check(!ctx.getConfState(), "requestConf(false) visible");
        check(ctx.getIntegState(), "requestInteg(true) visible");
        check(ctx.getMutualAuthState(), "requestMutualAuth(true) visible");
        check(!ctx.getCredDelegState(), "requestCredDeleg(false) visible");
        ctx.requestConf(true);
        check(ctx.getConfState(), "requestConf(true) visible again");

        // channel binding accepted (recorded) before establishment
        ChannelBinding cb = new ChannelBinding(new byte[]{7, 7});
        ctx.setChannelBinding(cb);
        check(true, "setChannelBinding on fresh context does not throw");

        // per-message operations require an established context -> NO_CONTEXT
        MessageProp prop = new MessageProp(false);
        try {
            ctx.wrap(new byte[]{1, 2}, 0, 2, prop);
            check(false, "expected GSSException for wrap on unestablished context");
        } catch (GSSException e) {
            check(e.getMajor() == GSSException.NO_CONTEXT, "wrap unestablished -> NO_CONTEXT");
        }
        try {
            ctx.getMIC(new byte[]{1}, 0, 1, prop);
            check(false, "expected GSSException for getMIC on unestablished context");
        } catch (GSSException e) {
            check(e.getMajor() == GSSException.NO_CONTEXT, "getMIC unestablished -> NO_CONTEXT");
        }
        try {
            ctx.getWrapSizeLimit(0, true, 100);
            check(false, "expected GSSException for getWrapSizeLimit on unestablished context");
        } catch (GSSException e) {
            check(e.getMajor() == GSSException.NO_CONTEXT, "getWrapSizeLimit unestablished -> NO_CONTEXT");
        }

        // export before establishment dereferences the null mech context (same quirk)
        try {
            ctx.export();
            check(false, "expected NPE for export on unestablished context");
        } catch (NullPointerException expected) {
            check(true, "export unestablished -> NPE (implementation quirk)");
        }

        // dispose is safe at any time
        ctx.dispose();
        check(true, "dispose does not throw");
        ctx.dispose();
        check(true, "dispose idempotent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
