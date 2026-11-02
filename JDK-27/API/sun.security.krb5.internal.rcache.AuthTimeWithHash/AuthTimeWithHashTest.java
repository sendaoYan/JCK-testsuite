/*
 * @test
 * @summary sun.security.krb5.internal.rcache.AuthTimeWithHash conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.rcache:+open
 * @run main AuthTimeWithHashTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AuthTimeWithHashTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.rcache.AuthTimeWithHash", false, AuthTimeWithHashTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.rcache.AuthTimeWithHash"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.rcache.AuthTime"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.Comparable"), "iface:java.lang.Comparable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("compareTo"), "method:compareTo");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("isSameIgnoresHash"), "method:isSameIgnoresHash");
        check(methods.contains("realAlg"), "method:realAlg");
        check(methods.contains("sameTimeDiffHash"), "method:sameTimeDiffHash");
        check(methods.contains("toString"), "method:toString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("DEFAULT_HASH_ALG"), "field:DEFAULT_HASH_ALG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
