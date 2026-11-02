/*
 * @test
 * @summary javax.swing.text.Caret conformance: type shape via reflection (headless-safe).
 * @run main CaretTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CaretTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Caret", false, CaretTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("install"), "method install");
        check(names.contains("deinstall"), "method deinstall");
        check(names.contains("paint"), "method paint");
        check(names.contains("addChangeListener"), "method addChangeListener");
        check(names.contains("removeChangeListener"), "method removeChangeListener");
        check(names.contains("isVisible"), "method isVisible");
        check(names.contains("setVisible"), "method setVisible");
        check(names.contains("isSelectionVisible"), "method isSelectionVisible");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
