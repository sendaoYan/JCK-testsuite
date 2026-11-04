/*
 * @test
 * @summary sun.security.krb5.internal.ccache.CCacheInputStream conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ccache:+open
 * @run main CCacheInputStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CCacheInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ccache.CCacheInputStream", false, CCacheInputStreamTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ccache.CCacheInputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.util.KrbDataInputStream"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.krb5.internal.ccache.FileCCacheConstants"), "iface:sun.security.krb5.internal.ccache.FileCCacheConstants");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("readPrincipal"), "method:readPrincipal");
        check(methods.contains("readTag"), "method:readTag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
