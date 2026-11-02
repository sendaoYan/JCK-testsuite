/*
 * @test
 * @summary sun.security.krb5.internal.crypto.crc32 conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto:+open
 * @run main crc32Test
 */
import java.lang.reflect.*;
import java.util.*;
public class crc32Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.crc32", false, crc32Test.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.crc32"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.security.MessageDigestSpi"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.Cloneable"), "iface:java.lang.Cloneable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("byte2crc32"), "method:byte2crc32");
        check(methods.contains("byte2crc32sum"), "method:byte2crc32sum");
        check(methods.contains("byte2crc32sum_bytes"), "method:byte2crc32sum_bytes");
        check(methods.contains("clone"), "method:clone");
        check(methods.contains("engineDigest"), "method:engineDigest");
        check(methods.contains("engineGetDigestLength"), "method:engineGetDigestLength");
        check(methods.contains("engineReset"), "method:engineReset");
        check(methods.contains("engineUpdate"), "method:engineUpdate");
        check(methods.contains("init"), "method:init");
        check(methods.contains("int2crc32"), "method:int2crc32");
        check(methods.contains("int2quad"), "method:int2quad");
        check(methods.contains("printcrc32Table"), "method:printcrc32Table");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
