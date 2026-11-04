/*
 * @test
 * @summary com.sun.beans.util.Cache conformance: type shape via reflection (headless-safe).
 * @run main CacheTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.beans.util.Cache", false, CacheTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.beans.util.Cache"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("create"), "method create");
        check(names.contains("get"), "method get");
        check(names.contains("remove"), "method remove");
        check(names.contains("clear"), "method clear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
