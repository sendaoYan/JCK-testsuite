/*
 * @test
 * @summary com.sun.security.jgss.InquireType conformance: white-box structure + black-box behavior.
 * @run main InquireTypeTest
 */

import java.lang.reflect.*;
import java.util.*;

public class InquireTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.jgss.InquireType");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends java.lang.Enum");

        Set<String> fs = fieldNames(c);
        check(fs.contains("KRB5_GET_SESSION_KEY"), "field KRB5_GET_SESSION_KEY");
        check(fs.contains("KRB5_GET_SESSION_KEY_EX"), "field KRB5_GET_SESSION_KEY_EX");
        check(fs.contains("KRB5_GET_TKT_FLAGS"), "field KRB5_GET_TKT_FLAGS");
        check(fs.contains("KRB5_GET_AUTHZ_DATA"), "field KRB5_GET_AUTHZ_DATA");
        check(fs.contains("KRB5_GET_AUTHTIME"), "field KRB5_GET_AUTHTIME");
        check(fs.contains("KRB5_GET_KRB_CRED"), "field KRB5_GET_KRB_CRED");

        com.sun.security.jgss.InquireType[] v = com.sun.security.jgss.InquireType.values();
        check(v.length == 6, "6 constants");
        check(com.sun.security.jgss.InquireType.KRB5_GET_SESSION_KEY.name().equals("KRB5_GET_SESSION_KEY"), "KRB5_GET_SESSION_KEY name");
        check(com.sun.security.jgss.InquireType.valueOf("KRB5_GET_TKT_FLAGS") == com.sun.security.jgss.InquireType.KRB5_GET_TKT_FLAGS, "valueOf identity");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.security.jgss.InquireType[] expected = {
            com.sun.security.jgss.InquireType.KRB5_GET_SESSION_KEY,
            com.sun.security.jgss.InquireType.KRB5_GET_SESSION_KEY_EX,
            com.sun.security.jgss.InquireType.KRB5_GET_TKT_FLAGS,
            com.sun.security.jgss.InquireType.KRB5_GET_AUTHZ_DATA,
            com.sun.security.jgss.InquireType.KRB5_GET_AUTHTIME,
            com.sun.security.jgss.InquireType.KRB5_GET_KRB_CRED,
        };
        check(Arrays.equals(com.sun.security.jgss.InquireType.values(), expected), "values() declaration order");

        for (int i = 0; i < expected.length; i++) {
            check(expected[i].ordinal() == i, expected[i].name() + " ordinal == " + i);
            check(expected[i].name().equals(expected[i].toString()), expected[i].name() + " toString == name");
            check(expected[i].getDeclaringClass() == com.sun.security.jgss.InquireType.class, expected[i].name() + " declaring class");
            check(com.sun.security.jgss.InquireType.valueOf(expected[i].name()) == expected[i], "valueOf(" + expected[i].name() + ") identity");
        }

        check(expected[0].compareTo(expected[1]) < 0, "compareTo ascending");
        check(expected[5].compareTo(expected[0]) > 0, "compareTo descending");

        boolean threw = false;
        try { com.sun.security.jgss.InquireType.valueOf("KRB5_GET_KEYTAB"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf bad name -> IllegalArgumentException");

        threw = false;
        try { com.sun.security.jgss.InquireType.valueOf(null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "valueOf null -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
