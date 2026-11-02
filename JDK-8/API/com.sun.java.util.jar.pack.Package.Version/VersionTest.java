/*
 * @test
 * @summary com.sun.java.util.jar.pack.Package.Version conformance: type shape via reflection (headless-safe).
 * @run main VersionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class VersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.util.jar.pack.Package$Version", false, VersionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.util.jar.pack.Package$Version"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("toString"), "method toString");
        check(names.contains("equals"), "method equals");
        check(names.contains("intValue"), "method intValue");
        check(names.contains("hashCode"), "method hashCode");
        check(names.contains("of"), "method of");
        check(names.contains("makeVersion"), "method makeVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
