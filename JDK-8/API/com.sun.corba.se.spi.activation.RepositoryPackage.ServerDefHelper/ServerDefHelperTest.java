/*
 * @test
 * @summary com.sun.corba.se.spi.activation.RepositoryPackage.ServerDefHelper conformance: type shape via reflection (headless-safe).
 * @run main ServerDefHelperTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ServerDefHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.activation.RepositoryPackage.ServerDefHelper", false, ServerDefHelperTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("extract"), "method extract");
        check(mn.contains("id"), "method id");
        check(mn.contains("insert"), "method insert");
        check(mn.contains("read"), "method read");
        check(mn.contains("type"), "method type");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
