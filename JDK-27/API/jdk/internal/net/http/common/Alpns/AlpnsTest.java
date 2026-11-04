/*
 * @test
 * @summary jdk.internal.net.http.common.Alpns conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main AlpnsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AlpnsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.Alpns", false, AlpnsTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.Alpns"), "name");
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
        check(methods.contains("isSecureALPNName"), "method:isSecureALPNName");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("H2"), "field:H2");
        check(fields.contains("H2C"), "field:H2C");
        check(fields.contains("H3"), "field:H3");
        check(fields.contains("HTTP_1_1"), "field:HTTP_1_1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
