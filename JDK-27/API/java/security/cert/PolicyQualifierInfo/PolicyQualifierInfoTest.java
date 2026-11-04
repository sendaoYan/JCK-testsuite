/*
 * @test
 * @summary java.security.cert.PolicyQualifierInfo conformance: white-box structure + black-box behavior.
 * @run main PolicyQualifierInfoTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class PolicyQualifierInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.PolicyQualifierInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"getEncoded", "getPolicyQualifier", "getPolicyQualifierId", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Built from DER: policyQualifierId (OID) + qualifier octet string
        // SEQUENCE { OID 1.3.6.1.5.5.7.2.1 (CPS), UTF8String "https://jck.example/cps" }
        byte[] der = java.util.Base64.getDecoder().decode("MB8GCCsGAQUFBwIBBBNodHRwczovL2pjay5leGFtcGxl");
        PolicyQualifierInfo pqi = new PolicyQualifierInfo(der);
        check(pqi.getPolicyQualifierId().equals("1.3.6.1.5.5.7.2.1"), "qualifier id CPS");
        check(pqi.getEncoded() != null && pqi.getEncoded().length > 0, "encoded present");
        check(pqi.getPolicyQualifier() != null && new String(pqi.getPolicyQualifier()).contains("jck.example"), "qualifier value");
        check(pqi.toString().length() > 0, "toString");
        // empty/garbage DER rejected
        try { new PolicyQualifierInfo(new byte[]{1, 2, 3}); check(false, "garbage rejected"); }
        catch (java.io.IOException ioe) { check(true, "garbage IOException"); }
        // two instances with identical bytes are equal in content (identity equals default)
        PolicyQualifierInfo same = new PolicyQualifierInfo(der);
        check(same.getPolicyQualifierId().equals(pqi.getPolicyQualifierId()), "same content id");

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
