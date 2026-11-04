/*
 * @test
 * @summary sun.security.krb5.internal.rcache.AuthList conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.rcache:+open
 * @run main AuthListTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AuthListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.rcache.AuthList", false, AuthListTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.rcache.AuthList"), "name");
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
        check(methods.contains("isEmpty"), "method:isEmpty");
        check(methods.contains("put"), "method:put");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
