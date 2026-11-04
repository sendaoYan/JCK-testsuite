/*
 * @test
 * @summary sun.security.krb5.internal.LoginOptions conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main LoginOptionsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class LoginOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.LoginOptions", false, LoginOptionsTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.LoginOptions"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.KDCOptions"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALLOW_POSTDATE"), "field:ALLOW_POSTDATE");
        check(fields.contains("ENC_TKT_IN_SKEY"), "field:ENC_TKT_IN_SKEY");
        check(fields.contains("FORWARDABLE"), "field:FORWARDABLE");
        check(fields.contains("MAX"), "field:MAX");
        check(fields.contains("PROXIABLE"), "field:PROXIABLE");
        check(fields.contains("RENEW"), "field:RENEW");
        check(fields.contains("RENEWABLE"), "field:RENEWABLE");
        check(fields.contains("RENEWABLE_OK"), "field:RENEWABLE_OK");
        check(fields.contains("RESERVED"), "field:RESERVED");
        check(fields.contains("VALIDATE"), "field:VALIDATE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
