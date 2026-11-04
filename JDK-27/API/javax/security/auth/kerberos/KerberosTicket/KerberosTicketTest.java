/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosTicket conformance: white-box structure + black-box behavior.
 * @run main KerberosTicketTest
 */

import javax.security.auth.Destroyable;
import javax.security.auth.Refreshable;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.EncryptionKey;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Date;
import java.util.Arrays;

public class KerberosTicketTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " [expected " + type.getSimpleName() + ", got " + t + "]", t);
        }
        throw new AssertionError(msg + " [expected " + type.getSimpleName() + "]");
    }
    static boolean hasInterface(Class<?> c, Class<?> want) {
        for (Class<?> i : c.getInterfaces()) if (i == want) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.kerberos.KerberosTicket");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "non-final class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasInterface(c, Destroyable.class), "implements Destroyable");
        check(hasInterface(c, Refreshable.class), "implements Refreshable");
        check(hasInterface(c, Serializable.class), "implements Serializable");
        for (String m : new String[]{"getClient", "getServer", "getSessionKey", "getSessionKeyType", "getFlags",
                "getAuthTime", "getStartTime", "getEndTime", "getRenewTill", "getClientAddresses", "getEncoded",
                "isCurrent", "refresh", "destroy", "isDestroyed"}) {
            check(c.getMethod(m).getDeclaringClass() == c, "public method " + m + " declared");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KerberosPrincipal c = new KerberosPrincipal("client@EXAMPLE.COM");
        KerberosPrincipal s = new KerberosPrincipal("krbtgt/EXAMPLE.COM@EXAMPLE.COM");
        byte[] asn1 = new byte[]{1, 2, 3};
        byte[] session = new byte[]{9, 8, 7, 6};
        Date auth = new Date(100_000L);
        Date start = new Date(200_000L);
        Date end = new Date(300_000L);

        KerberosTicket kt = new KerberosTicket(asn1, c, s, session, 17, new boolean[12],
                auth, start, end, null, null);

        // identity accessors
        check("client@EXAMPLE.COM".equals(kt.getClient().getName()), "getClient name");
        check("krbtgt/EXAMPLE.COM@EXAMPLE.COM".equals(kt.getServer().getName()), "getServer name");
        check(kt.getSessionKeyType() == 17, "getSessionKeyType");
        check(kt.getFlags().length == 32, "flags padded to 32");
        check(Arrays.equals(asn1, kt.getEncoded()), "getEncoded contents");
        check(kt.getAuthTime().getTime() == 100_000L, "getAuthTime");
        check(kt.getStartTime().getTime() == 200_000L, "getStartTime");
        check(kt.getEndTime().getTime() == 300_000L, "getEndTime");
        check(kt.getRenewTill() == null, "getRenewTill null for non-renewable");
        check(kt.getClientAddresses() == null, "getClientAddresses null");

        // session key exposed as EncryptionKey
        SecretKey sk = kt.getSessionKey();
        check(sk instanceof EncryptionKey, "getSessionKey is EncryptionKey");
        EncryptionKey esk = (EncryptionKey) sk;
        check(esk.getKeyType() == 17, "session key type");
        check(Arrays.equals(session, esk.getEncoded()), "session key bytes");

        // default all-false flags
        check(!kt.isForwardable() && !kt.isForwarded() && !kt.isProxiable()
                && !kt.isProxy() && !kt.isPostdated() && !kt.isRenewable() && !kt.isInitial(),
                "all flags false by default");

        // getEncoded / getFlags / date getters return copies (immutability)
        kt.getEncoded()[0] = 99;
        check(kt.getEncoded()[0] == 1, "getEncoded returns copy");
        kt.getFlags()[0] = true;
        check(!kt.getFlags()[0], "getFlags returns copy");
        kt.getAuthTime().setTime(0);
        kt.getStartTime().setTime(0);
        kt.getEndTime().setTime(0);
        check(kt.getAuthTime().getTime() == 100_000L, "authTime immutable");
        check(kt.getStartTime().getTime() == 200_000L, "startTime immutable");
        check(kt.getEndTime().getTime() == 300_000L, "endTime immutable");

        // renewable ticket with flags set
        boolean[] flags = new boolean[32];
        flags[1] = true;  // forwardable
        flags[2] = true;  // forwarded
        flags[3] = true;  // proxiable
        flags[4] = true;  // proxy
        flags[6] = true;  // postdated
        flags[8] = true;  // renewable
        flags[9] = true;  // initial
        Date renewTill = new Date(400_000L);
        KerberosTicket rt = new KerberosTicket(asn1, c, s, session, 18, flags,
                auth, start, end, renewTill, null);
        check(rt.isForwardable() && rt.isForwarded() && rt.isProxiable()
                && rt.isProxy() && rt.isPostdated() && rt.isRenewable() && rt.isInitial(),
                "all set flags true");
        check(rt.getRenewTill().getTime() == 400_000L, "getRenewTill renewable");
        check(rt.getSessionKeyType() == 18, "renewable ticket session key type");

        // isCurrent(): deterministic via far future / epoch end times
        KerberosTicket future = new KerberosTicket(asn1, c, s, session, 17, new boolean[1],
                auth, start, new Date(Long.MAX_VALUE), null, null);
        KerberosTicket past = new KerberosTicket(asn1, c, s, session, 17, new boolean[1],
                auth, start, new Date(0L), null, null);
        check(future.isCurrent(), "isCurrent true for far future end");
        check(!past.isCurrent(), "isCurrent false for epoch end");

        // refresh(): non-renewable ticket fails offline before touching KDC
        checkThrows(RefreshFailedException.class, () -> kt.refresh(), "refresh non-renewable -> RefreshFailedException");

        // constructor validation
        checkThrows(IllegalArgumentException.class, () -> new KerberosTicket(null, c, s, session, 17, new boolean[1], auth, start, end, null, null),
                "null asn1Encoding -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new KerberosTicket(asn1, null, s, session, 17, new boolean[1], auth, start, end, null, null),
                "null client -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new KerberosTicket(asn1, c, null, session, 17, new boolean[1], auth, start, end, null, null),
                "null server -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new KerberosTicket(asn1, c, s, null, 17, new boolean[1], auth, start, end, null, null),
                "null sessionKey -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new KerberosTicket(asn1, c, s, session, 17, new boolean[1], auth, start, null, null, null),
                "null endTime -> IAE");

        // destroy lifecycle
        KerberosTicket dk = new KerberosTicket(asn1, c, s, session, 17, new boolean[12], auth, start, end, null, null);
        check(!dk.isDestroyed(), "not destroyed initially");
        dk.destroy();
        check(dk.isDestroyed(), "destroyed flag");
        check("Destroyed KerberosTicket".equals(dk.toString()), "toString after destroy");
        check(dk.getClient() == null, "getClient null after destroy");
        check(dk.getServer() == null, "getServer null after destroy");
        check(dk.getFlags() == null, "getFlags null after destroy");
        check(dk.getAuthTime() == null, "getAuthTime null after destroy");
        check(dk.getStartTime() == null, "getStartTime null after destroy");
        check(dk.getEndTime() == null, "getEndTime null after destroy");
        check(dk.getRenewTill() == null, "getRenewTill null after destroy");
        check(dk.getClientAddresses() == null, "getClientAddresses null after destroy");
        checkThrows(IllegalStateException.class, () -> dk.getSessionKey(), "getSessionKey after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getSessionKeyType(), "getSessionKeyType after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dk.getEncoded(), "getEncoded after destroy -> ISE");
        checkThrows(RefreshFailedException.class, () -> dk.refresh(), "refresh destroyed -> RefreshFailedException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
