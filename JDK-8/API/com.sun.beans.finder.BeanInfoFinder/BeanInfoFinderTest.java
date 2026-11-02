/*
 * @test
 * @summary com.sun.beans.finder.BeanInfoFinder conformance: type shape via reflection (headless-safe).
 * @run main BeanInfoFinderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BeanInfoFinderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.beans.finder.BeanInfoFinder", false, BeanInfoFinderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.beans.finder.BeanInfoFinder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.beans.finder.InstanceFinder"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("instantiate"), "method instantiate");
        check(names.contains("setPackages"), "method setPackages");
        check(names.contains("getPackages"), "method getPackages");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
