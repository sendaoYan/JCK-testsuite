/*
 * @test
 * @summary sun.security.jgss.krb5.InitialToken.OverloadedChecksum conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.krb5:+open
 * @run main OverloadedChecksumTest
 */
import java.lang.reflect.*;
import java.util.*;
public class OverloadedChecksumTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.krb5.InitialToken$OverloadedChecksum", false, OverloadedChecksumTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.krb5.InitialToken$OverloadedChecksum"), "name");
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
        check(methods.contains("getChecksum"), "method:getChecksum");
        check(methods.contains("getDelegatedCreds"), "method:getDelegatedCreds");
        check(methods.contains("setContextFlags"), "method:setContextFlags");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
