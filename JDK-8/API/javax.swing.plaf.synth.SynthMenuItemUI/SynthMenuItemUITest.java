/*
 * @test
 * @summary javax.swing.plaf.synth.SynthMenuItemUI conformance: type shape via reflection (headless-safe).
 * @run main SynthMenuItemUITest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SynthMenuItemUITest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthMenuItemUI", false, SynthMenuItemUITest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicMenuItemUI"), "superclass");
        check(hasInterface(c, "java.beans.PropertyChangeListener"), "implements java.beans.PropertyChangeListener");
        check(hasInterface(c, "javax.swing.plaf.synth.SynthUI"), "implements javax.swing.plaf.synth.SynthUI");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createUI"), "method createUI");
        check(names.contains("uninstallUI"), "method uninstallUI");
        check(names.contains("getContext"), "method getContext");
        check(names.contains("update"), "method update");
        check(names.contains("paint"), "method paint");
        check(names.contains("paintBorder"), "method paintBorder");
        check(names.contains("propertyChange"), "method propertyChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
