/*
 * @test
 * @summary com.sun.corba.se.spi.activation.RepositoryHelper conformance: type shape via reflection (headless-safe).
 * @run main RepositoryHelperTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RepositoryHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.activation.RepositoryHelper", false, RepositoryHelperTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("extract"), "method extract");
        check(mn.contains("id"), "method id");
        check(mn.contains("insert"), "method insert");
        check(mn.contains("narrow"), "method narrow");
        check(mn.contains("read"), "method read");
        check(mn.contains("type"), "method type");
        check(mn.contains("unchecked_narrow"), "method unchecked_narrow");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
