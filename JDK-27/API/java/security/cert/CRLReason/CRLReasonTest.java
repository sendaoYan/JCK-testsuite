/*
 * @test
 * @summary java.security.cert.CRLReason conformance: white-box structure + black-box behavior.
 * @run main CRLReasonTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class CRLReasonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.CRLReason");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkEnumConstants(c, new String[]{"UNSPECIFIED", "KEY_COMPROMISE", "CA_COMPROMISE", "AFFILIATION_CHANGED", "SUPERSEDED", "CESSATION_OF_OPERATION", "CERTIFICATE_HOLD", "UNUSED", "REMOVE_FROM_CRL", "PRIVILEGE_WITHDRAWN", "AA_COMPROMISE"});
        checkMethods(c, new String[]{"valueOf", "values"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Enum with the 10 RFC 5280 reasons
        List<String> names = new ArrayList<>();
        for (CRLReason r : CRLReason.values()) names.add(r.name());
        Collections.sort(names);
        check(names.equals(List.of("AA_COMPROMISE", "AFFILIATION_CHANGED", "CA_COMPROMISE", "CERTIFICATE_HOLD",
            "CESSATION_OF_OPERATION", "KEY_COMPROMISE", "PRIVILEGE_WITHDRAWN", "REMOVE_FROM_CRL",
            "SUPERSEDED", "UNSPECIFIED", "UNUSED")), "11 reasons");
        check(CRLReason.valueOf("KEY_COMPROMISE") == CRLReason.KEY_COMPROMISE, "valueOf");
        check(CRLReason.UNSPECIFIED.ordinal() == 0, "UNSPECIFIED first");
        // usable as a revocation reason in CertificateRevokedException and readable back
        java.util.Date when = java.util.Date.from(java.time.Instant.parse("2026-06-01T00:00:00Z"));
        javax.security.auth.x500.X500Principal auth = new javax.security.auth.x500.X500Principal("CN=jck");
        CertificateRevokedException cre = new CertificateRevokedException(
            when, CRLReason.SUPERSEDED, auth, java.util.Map.of());
        check(cre.getRevocationReason() == CRLReason.SUPERSEDED, "reason round trip");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
