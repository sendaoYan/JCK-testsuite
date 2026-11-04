/*
 * @test
 * @summary sun.security.krb5.internal.crypto.ArcFourHmac conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto:+open
 * @run main ArcFourHmacTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ArcFourHmacTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.ArcFourHmac", false, ArcFourHmacTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.ArcFourHmac"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("calculateChecksum"), "method:calculateChecksum");
        check(methods.contains("decrypt"), "method:decrypt");
        check(methods.contains("decryptRaw"), "method:decryptRaw");
        check(methods.contains("decryptSeq"), "method:decryptSeq");
        check(methods.contains("encrypt"), "method:encrypt");
        check(methods.contains("encryptRaw"), "method:encryptRaw");
        check(methods.contains("encryptSeq"), "method:encryptSeq");
        check(methods.contains("getChecksumLength"), "method:getChecksumLength");
        check(methods.contains("stringToKey"), "method:stringToKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
