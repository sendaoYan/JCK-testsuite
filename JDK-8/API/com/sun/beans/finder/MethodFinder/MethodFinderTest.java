/*
 * @test
 * @summary com.sun.beans.finder.MethodFinder conformance: type shape via reflection (headless-safe).
 * @run main MethodFinderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MethodFinderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.beans.finder.MethodFinder", false, MethodFinderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.beans.finder.MethodFinder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.beans.finder.AbstractFinder"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("findMethod"), "method findMethod");
        check(names.contains("findInstanceMethod"), "method findInstanceMethod");
        check(names.contains("findStaticMethod"), "method findStaticMethod");
        check(names.contains("findAccessibleMethod"), "method findAccessibleMethod");
        check(names.contains("isValid"), "method isValid");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
