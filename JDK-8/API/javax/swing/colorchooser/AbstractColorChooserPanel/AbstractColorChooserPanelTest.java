/*
 * @test
 * @summary javax.swing.colorchooser.AbstractColorChooserPanel conformance: type shape via reflection (headless-safe).
 * @run main AbstractColorChooserPanelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractColorChooserPanelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.AbstractColorChooserPanel", false, AbstractColorChooserPanelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JPanel"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("updateChooser"), "method updateChooser");
        check(names.contains("getDisplayName"), "method getDisplayName");
        check(names.contains("getMnemonic"), "method getMnemonic");
        check(names.contains("getDisplayedMnemonicIndex"), "method getDisplayedMnemonicIndex");
        check(names.contains("getSmallDisplayIcon"), "method getSmallDisplayIcon");
        check(names.contains("getLargeDisplayIcon"), "method getLargeDisplayIcon");
        check(names.contains("installChooserPanel"), "method installChooserPanel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
