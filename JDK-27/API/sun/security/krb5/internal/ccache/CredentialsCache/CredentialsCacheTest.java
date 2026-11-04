/*
 * @test
 * @summary sun.security.krb5.internal.ccache.CredentialsCache conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ccache:+open
 * @run main CredentialsCacheTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CredentialsCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ccache.CredentialsCache", false, CredentialsCacheTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ccache.CredentialsCache"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addConfigEntry"), "method:addConfigEntry");
        check(methods.contains("cacheName"), "method:cacheName");
        check(methods.contains("create"), "method:create");
        check(methods.contains("getConfigEntries"), "method:getConfigEntries");
        check(methods.contains("getConfigEntry"), "method:getConfigEntry");
        check(methods.contains("getCreds"), "method:getCreds");
        check(methods.contains("getCredsList"), "method:getCredsList");
        check(methods.contains("getDefaultCreds"), "method:getDefaultCreds");
        check(methods.contains("getInitialCreds"), "method:getInitialCreds");
        check(methods.contains("getInstance"), "method:getInstance");
        check(methods.contains("getPrimaryPrincipal"), "method:getPrimaryPrincipal");
        check(methods.contains("save"), "method:save");
        check(methods.contains("update"), "method:update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
