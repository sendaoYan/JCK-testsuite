/*
 * @test
 * @summary java.security.cert.PKIXRevocationChecker.Option conformance: white-box structure + black-box behavior.
 * @run main OptionTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class OptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.PKIXRevocationChecker$Option");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkEnumConstants(c, new String[]{"ONLY_END_ENTITY", "PREFER_CRLS", "NO_FALLBACK", "SOFT_FAIL"});
        checkMethods(c, new String[]{"valueOf", "values"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        List<String> names = new ArrayList<>();
        for (PKIXRevocationChecker.Option o : PKIXRevocationChecker.Option.values()) names.add(o.name());
        Collections.sort(names);
        check(names.equals(List.of("NO_FALLBACK", "ONLY_END_ENTITY", "PREFER_CRLS", "SOFT_FAIL")), "4 options");
        check(PKIXRevocationChecker.Option.valueOf("SOFT_FAIL") == PKIXRevocationChecker.Option.SOFT_FAIL, "valueOf");
        // options apply to a checker
        PKIXRevocationChecker rev = (PKIXRevocationChecker) CertPathValidator.getInstance("PKIX").getRevocationChecker();
        rev.setOptions(java.util.EnumSet.allOf(PKIXRevocationChecker.Option.class));
        check(rev.getOptions().size() == 4, "all options set");
        rev.setOptions(java.util.EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS));
        check(rev.getOptions().equals(java.util.EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS)), "single option");

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
