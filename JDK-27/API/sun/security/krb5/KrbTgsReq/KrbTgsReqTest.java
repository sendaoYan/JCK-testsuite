/*
 * @test
 * @summary sun.security.krb5.KrbTgsReq conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main KrbTgsReqTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KrbTgsReqTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.KrbTgsReq", false, KrbTgsReqTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.KrbTgsReq"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.KrbKdcReq"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("encoding"), "method:encoding");
        check(methods.contains("sendAndGetCreds"), "method:sendAndGetCreds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
