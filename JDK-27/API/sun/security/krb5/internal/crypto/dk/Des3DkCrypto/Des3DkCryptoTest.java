/*
 * @test
 * @summary sun.security.krb5.internal.crypto.dk.Des3DkCrypto conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto.dk:+open
 * @run main Des3DkCryptoTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Des3DkCryptoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.dk.Des3DkCrypto", false, Des3DkCryptoTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.dk.Des3DkCrypto"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.crypto.dk.DkCrypto"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getChecksumLength"), "method:getChecksumLength");
        check(methods.contains("getCipher"), "method:getCipher");
        check(methods.contains("getHmac"), "method:getHmac");
        check(methods.contains("getKeySeedLength"), "method:getKeySeedLength");
        check(methods.contains("parityFix"), "method:parityFix");
        check(methods.contains("randomToKey"), "method:randomToKey");
        check(methods.contains("stringToKey"), "method:stringToKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
