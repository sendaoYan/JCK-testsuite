/*
 * @test
 * @summary sun.security.krb5.internal.crypto.Crc32CksumType conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto:+open
 * @run main Crc32CksumTypeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Crc32CksumTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.Crc32CksumType", false, Crc32CksumTypeTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.Crc32CksumType"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.crypto.CksumType"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("bytes2long"), "method:bytes2long");
        check(methods.contains("calculateChecksum"), "method:calculateChecksum");
        check(methods.contains("cksumSize"), "method:cksumSize");
        check(methods.contains("cksumType"), "method:cksumType");
        check(methods.contains("confounderSize"), "method:confounderSize");
        check(methods.contains("int2quad"), "method:int2quad");
        check(methods.contains("isKeyed"), "method:isKeyed");
        check(methods.contains("keySize"), "method:keySize");
        check(methods.contains("keyType"), "method:keyType");
        check(methods.contains("verifyChecksum"), "method:verifyChecksum");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
