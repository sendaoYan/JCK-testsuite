/*
 * @test
 * @summary javax.swing.text.BoxView conformance: type shape via reflection (headless-safe).
 * @run main BoxViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BoxViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.BoxView", false, BoxViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.CompositeView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAxis"), "method getAxis");
        check(names.contains("setAxis"), "method setAxis");
        check(names.contains("layoutChanged"), "method layoutChanged");
        check(names.contains("replace"), "method replace");
        check(names.contains("preferenceChanged"), "method preferenceChanged");
        check(names.contains("getResizeWeight"), "method getResizeWeight");
        check(names.contains("setSize"), "method setSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
