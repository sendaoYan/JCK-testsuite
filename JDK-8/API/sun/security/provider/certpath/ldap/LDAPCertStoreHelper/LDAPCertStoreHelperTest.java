/*
 * @test
 * @summary sun.security.provider.certpath.ldap.LDAPCertStoreHelper conformance: type shape via reflection (headless-safe).
 * @run main LDAPCertStoreHelperTest
 */

import java.lang.reflect.Modifier;

public class LDAPCertStoreHelperTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.provider.certpath.ldap.LDAPCertStoreHelper", false, LDAPCertStoreHelperTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.provider.certpath.CertStoreHelper"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
