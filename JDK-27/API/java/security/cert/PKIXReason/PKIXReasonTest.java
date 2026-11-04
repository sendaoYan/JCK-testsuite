/*
 * @test
 * @summary java.security.cert.PKIXReason conformance: white-box structure + black-box behavior.
 * @run main PKIXReasonTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class PKIXReasonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.PKIXReason");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkInterfaces(c, new String[]{"java.security.cert.CertPathValidatorException$Reason"});
        checkEnumConstants(c, new String[]{"NAME_CHAINING", "INVALID_KEY_USAGE", "INVALID_POLICY", "NO_TRUST_ANCHOR", "UNRECOGNIZED_CRIT_EXT", "NOT_CA_CERT", "PATH_TOO_LONG", "INVALID_NAME"});
        checkMethods(c, new String[]{"valueOf", "values"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        List<String> names = new ArrayList<>();
        for (PKIXReason r : PKIXReason.values()) names.add(r.name());
        Collections.sort(names);
        check(names.equals(List.of("INVALID_KEY_USAGE", "INVALID_NAME", "INVALID_POLICY", "NAME_CHAINING",
            "NOT_CA_CERT", "NO_TRUST_ANCHOR", "PATH_TOO_LONG", "UNRECOGNIZED_CRIT_EXT")), "8 reasons");
        check(PKIXReason.valueOf("NO_TRUST_ANCHOR") == PKIXReason.NO_TRUST_ANCHOR, "valueOf");
        // usable as a validation Reason
        CertPathValidatorException e = new CertPathValidatorException("m", null, null, -1, PKIXReason.PATH_TOO_LONG);
        check(e.getReason() == PKIXReason.PATH_TOO_LONG, "reason retained");
        check(PKIXReason.NO_TRUST_ANCHOR instanceof CertPathValidatorException.Reason, "implements Reason");

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
