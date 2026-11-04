/*
 * @test
 * @summary com.sun.security.jgss.GSSUtil conformance: white-box structure + black-box behavior.
 * @run main GSSUtilTest
 */

import java.lang.reflect.*;
import java.util.*;

public class GSSUtilTest {
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
        Class<?> c = load("com.sun.security.jgss.GSSUtil");
        check(!c.isInterface(), "concrete class");
        check(methodNames(c).contains("createSubject"), "createSubject method");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.security.auth.Subject s = com.sun.security.jgss.GSSUtil.createSubject(null, null);
        check(s != null, "createSubject(null, null) non-null");
        check(!s.isReadOnly(), "returned Subject is not read-only");
        check(s.getPrincipals() != null, "getPrincipals() non-null");
        check(s.getPublicCredentials() != null, "getPublicCredentials() non-null");
        check(s.getPrivateCredentials() != null, "getPrivateCredentials() non-null");
        check(s.getPrincipals().isEmpty(), "principals empty for null GSSName");
        check(s.getPublicCredentials().isEmpty(), "public credentials empty");
        check(s.getPrivateCredentials().isEmpty(), "private credentials empty for null GSSCredential");

        javax.security.auth.Subject s2 = com.sun.security.jgss.GSSUtil.createSubject(null, null);
        check(s2 != null && s2 != s, "two calls return independent Subjects");
        check(s2.getPrincipals().isEmpty() && s2.getPrivateCredentials().isEmpty(), "second Subject also empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
