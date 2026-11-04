/*
 * @test
 * @summary sun.security.krb5.internal.crypto.DesCbcMd5EType conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto:+open
 * @run main DesCbcMd5ETypeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DesCbcMd5ETypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.DesCbcMd5EType", false, DesCbcMd5ETypeTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.DesCbcMd5EType"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.crypto.DesCbcEType"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("blockSize"), "method:blockSize");
        check(methods.contains("calculateChecksum"), "method:calculateChecksum");
        check(methods.contains("checksumSize"), "method:checksumSize");
        check(methods.contains("checksumType"), "method:checksumType");
        check(methods.contains("confounderSize"), "method:confounderSize");
        check(methods.contains("decrypt"), "method:decrypt");
        check(methods.contains("eType"), "method:eType");
        check(methods.contains("encrypt"), "method:encrypt");
        check(methods.contains("keySize"), "method:keySize");
        check(methods.contains("keyType"), "method:keyType");
        check(methods.contains("minimumPadSize"), "method:minimumPadSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
