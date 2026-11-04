/*
 * @test
 * @summary java.security.cert.CertPathValidatorException.BasicReason conformance: white-box structure + black-box behavior.
 * @run main BasicReasonTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;


public class BasicReasonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.CertPathValidatorException$BasicReason");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkInterfaces(c, new String[]{"java.security.cert.CertPathValidatorException$Reason"});
        checkEnumConstants(c, new String[]{"UNSPECIFIED", "EXPIRED", "NOT_YET_VALID", "REVOKED", "UNDETERMINED_REVOCATION_STATUS", "INVALID_SIGNATURE", "ALGORITHM_CONSTRAINED"});
        checkMethods(c, new String[]{"valueOf", "values"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Enum: all constants and their family membership
        CertPathValidatorException.Reason[] vals = CertPathValidatorException.BasicReason.values();
        List<String> names = new ArrayList<>();
        for (CertPathValidatorException.Reason r : vals) names.add(((CertPathValidatorException.BasicReason) r).name());
        Collections.sort(names);
        check(names.equals(List.of("ALGORITHM_CONSTRAINED", "EXPIRED", "INVALID_SIGNATURE", "NOT_YET_VALID",
            "REVOKED", "UNDETERMINED_REVOCATION_STATUS", "UNSPECIFIED")), "all 7 constants");
        // each constant is a Reason usable in the exception
        for (CertPathValidatorException.Reason r : vals) {
            CertPathValidatorException e = new CertPathValidatorException("m", null, null, -1, r);
            check(e.getReason() == r, "reason retained " + r);
        }
        check(CertPathValidatorException.BasicReason.valueOf("EXPIRED") == CertPathValidatorException.BasicReason.EXPIRED, "valueOf");
        check(CertPathValidatorException.BasicReason.INVALID_SIGNATURE instanceof CertPathValidatorException.Reason, "implements Reason");
        check(CertPathValidatorException.BasicReason.UNSPECIFIED.getDeclaringClass() == CertPathValidatorException.BasicReason.class, "declaring class");

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
