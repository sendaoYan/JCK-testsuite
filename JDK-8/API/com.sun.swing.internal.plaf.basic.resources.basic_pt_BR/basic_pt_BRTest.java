/*
 * @test
 * @summary com.sun.swing.internal.plaf.basic.resources.basic_pt_BR conformance: type shape via reflection (headless-safe).
 * @run main basic_pt_BRTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class basic_pt_BRTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.swing.internal.plaf.basic.resources.basic_pt_BR", false, basic_pt_BRTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.swing.internal.plaf.basic.resources.basic_pt_BR"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getContents"), "method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
