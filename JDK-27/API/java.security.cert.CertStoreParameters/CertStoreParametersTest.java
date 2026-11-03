/*
 * @test
 * @summary java.security.cert.CertStoreParameters conformance: white-box structure + black-box behavior.
 * @run main CertStoreParametersTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class CertStoreParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.cert.CertStoreParameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.lang.Cloneable"});
        checkMethods(c, new String[]{"clone"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Cloneable marker implemented by the three concrete parameter types
        CollectionCertStoreParameters c = new CollectionCertStoreParameters(List.of());
        check(c instanceof CertStoreParameters, "Collection implements");
        check(c.clone() != null && c.clone() instanceof CollectionCertStoreParameters, "collection clone");
        LDAPCertStoreParameters l = new LDAPCertStoreParameters("ldap.jck", 389);
        check(l instanceof CertStoreParameters, "LDAP implements");
        check(l.clone() != l, "ldap clone distinct");
        URICertStoreParameters u = new URICertStoreParameters(java.net.URI.create("file:///tmp/certs"));
        check(u instanceof CertStoreParameters, "URI implements");
        check(u.clone() != u, "uri clone distinct");
        // a custom implementation carrying a fixed payload
        class FixedParams implements CertStoreParameters {
            public Object clone() { return new FixedParams(); }
        }
        check(new FixedParams().clone() instanceof FixedParams, "custom clone");

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
