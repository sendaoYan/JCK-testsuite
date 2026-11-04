/*
 * @test
 * @summary sun.security.jgss.krb5.Krb5InitCredential conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.krb5:+open
 * @run main Krb5InitCredentialTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Krb5InitCredentialTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.krb5.Krb5InitCredential", false, Krb5InitCredentialTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.krb5.Krb5InitCredential"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("javax.security.auth.kerberos.KerberosTicket"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.jgss.krb5.Krb5CredElement"), "iface:sun.security.jgss.krb5.Krb5CredElement");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("dispose"), "method:dispose");
        check(methods.contains("getAcceptLifetime"), "method:getAcceptLifetime");
        check(methods.contains("getInitLifetime"), "method:getInitLifetime");
        check(methods.contains("getMechanism"), "method:getMechanism");
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("getProvider"), "method:getProvider");
        check(methods.contains("impersonate"), "method:impersonate");
        check(methods.contains("isAcceptorCredential"), "method:isAcceptorCredential");
        check(methods.contains("isInitiatorCredential"), "method:isInitiatorCredential");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("proxyTicket"), "field:proxyTicket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
