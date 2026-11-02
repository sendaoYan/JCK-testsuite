/*
 * @test
 * @summary javax.swing.ButtonModel conformance: type shape via reflection (headless-safe).
 * @run main ButtonModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ButtonModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ButtonModel", false, ButtonModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.awt.ItemSelectable"), "implements java.awt.ItemSelectable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isArmed"), "method isArmed");
        check(names.contains("isSelected"), "method isSelected");
        check(names.contains("isEnabled"), "method isEnabled");
        check(names.contains("isPressed"), "method isPressed");
        check(names.contains("isRollover"), "method isRollover");
        check(names.contains("setArmed"), "method setArmed");
        check(names.contains("setSelected"), "method setSelected");
        check(names.contains("setEnabled"), "method setEnabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
