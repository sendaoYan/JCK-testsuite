/*
 * @test
 * @summary sun.security.krb5.Confounder conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main ConfounderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConfounderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.Confounder", false, ConfounderTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.Confounder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("bytes"), "method:bytes");
        check(methods.contains("intValue"), "method:intValue");
        check(methods.contains("longValue"), "method:longValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
