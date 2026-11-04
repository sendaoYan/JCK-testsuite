/*
 * @test
 * @summary sun.security.krb5.internal.ktab.KeyTabEntry conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ktab:+open
 * @run main KeyTabEntryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyTabEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ktab.KeyTabEntry", false, KeyTabEntryTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ktab.KeyTabEntry"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.krb5.internal.ktab.KeyTabConstants"), "iface:sun.security.krb5.internal.ktab.KeyTabConstants");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("entryLength"), "method:entryLength");
        check(methods.contains("getKey"), "method:getKey");
        check(methods.contains("getKeyString"), "method:getKeyString");
        check(methods.contains("getService"), "method:getService");
        check(methods.contains("getTimeStamp"), "method:getTimeStamp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
