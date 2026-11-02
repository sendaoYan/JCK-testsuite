/*
 * @test
 * @summary sun.security.krb5.internal.ccache.FileCredentialsCache conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ccache:+open
 * @run main FileCredentialsCacheTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FileCredentialsCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ccache.FileCredentialsCache", false, FileCredentialsCacheTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ccache.FileCredentialsCache"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.ccache.CredentialsCache"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.krb5.internal.ccache.FileCCacheConstants"), "iface:sun.security.krb5.internal.ccache.FileCCacheConstants");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("acquireInstance"), "method:acquireInstance");
        check(methods.contains("addConfigEntry"), "method:addConfigEntry");
        check(methods.contains("cacheName"), "method:cacheName");
        check(methods.contains("checkValidation"), "method:checkValidation");
        check(methods.contains("getConfigEntries"), "method:getConfigEntries");
        check(methods.contains("getCreds"), "method:getCreds");
        check(methods.contains("getCredsList"), "method:getCredsList");
        check(methods.contains("getDefaultCacheName"), "method:getDefaultCacheName");
        check(methods.contains("getDefaultCreds"), "method:getDefaultCreds");
        check(methods.contains("getInitialCreds"), "method:getInitialCreds");
        check(methods.contains("getPrimaryPrincipal"), "method:getPrimaryPrincipal");
        check(methods.contains("save"), "method:save");
        check(methods.contains("update"), "method:update");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("primaryPrincipal"), "field:primaryPrincipal");
        check(fields.contains("tag"), "field:tag");
        check(fields.contains("version"), "field:version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
