/*
 * @test
 * @summary javax.swing.text.CompositeView conformance: type shape via reflection (headless-safe).
 * @run main CompositeViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CompositeViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.CompositeView", false, CompositeViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setParent"), "method setParent");
        check(names.contains("getViewCount"), "method getViewCount");
        check(names.contains("getView"), "method getView");
        check(names.contains("replace"), "method replace");
        check(names.contains("getChildAllocation"), "method getChildAllocation");
        check(names.contains("modelToView"), "method modelToView");
        check(names.contains("modelToView"), "method modelToView");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
