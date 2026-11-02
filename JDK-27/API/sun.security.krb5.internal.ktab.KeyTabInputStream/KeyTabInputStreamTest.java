/*
 * @test
 * @summary sun.security.krb5.internal.ktab.KeyTabInputStream conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ktab:+open
 * @run main KeyTabInputStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyTabInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ktab.KeyTabInputStream", false, KeyTabInputStreamTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ktab.KeyTabInputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.util.KrbDataInputStream"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.krb5.internal.ktab.KeyTabConstants"), "iface:sun.security.krb5.internal.ktab.KeyTabConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
