/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.ElementSpec conformance: type shape via reflection (headless-safe).
 * @run main ElementSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ElementSpecTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$ElementSpec", false, ElementSpecTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setType"), "method setType");
        check(names.contains("getType"), "method getType");
        check(names.contains("setDirection"), "method setDirection");
        check(names.contains("getDirection"), "method getDirection");
        check(names.contains("getAttributes"), "method getAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
