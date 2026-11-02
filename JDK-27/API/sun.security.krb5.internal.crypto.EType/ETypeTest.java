/*
 * @test
 * @summary sun.security.krb5.internal.crypto.EType conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto:+open
 * @run main ETypeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ETypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.EType", false, ETypeTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.EType"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("blockSize"), "method:blockSize");
        check(methods.contains("checksumSize"), "method:checksumSize");
        check(methods.contains("checksumType"), "method:checksumType");
        check(methods.contains("confounderSize"), "method:confounderSize");
        check(methods.contains("dataSize"), "method:dataSize");
        check(methods.contains("decrypt"), "method:decrypt");
        check(methods.contains("decryptedData"), "method:decryptedData");
        check(methods.contains("eType"), "method:eType");
        check(methods.contains("encrypt"), "method:encrypt");
        check(methods.contains("getBuiltInDefaults"), "method:getBuiltInDefaults");
        check(methods.contains("getDefaults"), "method:getDefaults");
        check(methods.contains("getInstance"), "method:getInstance");
        check(methods.contains("initStatic"), "method:initStatic");
        check(methods.contains("isNewer"), "method:isNewer");
        check(methods.contains("isOneOf"), "method:isOneOf");
        check(methods.contains("isSupported"), "method:isSupported");
        check(methods.contains("keySize"), "method:keySize");
        check(methods.contains("keyType"), "method:keyType");
        check(methods.contains("minimumPadSize"), "method:minimumPadSize");
        check(methods.contains("padSize"), "method:padSize");
        check(methods.contains("startOfChecksum"), "method:startOfChecksum");
        check(methods.contains("startOfData"), "method:startOfData");
        check(methods.contains("startOfPad"), "method:startOfPad");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
