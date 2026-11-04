/*
 * @test
 * @summary com.sun.java.swing.plaf.windows.WindowsFileChooserUI.DirectoryComboBoxModel conformance: type shape via reflection (headless-safe).
 * @run main DirectoryComboBoxModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DirectoryComboBoxModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.windows.WindowsFileChooserUI$DirectoryComboBoxModel", false, DirectoryComboBoxModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.windows.WindowsFileChooserUI$DirectoryComboBoxModel"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractListModel"), "superclass");
        check(hasInterface(c, "javax.swing.ComboBoxModel"), "implements javax.swing.ComboBoxModel");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getDepth"), "method getDepth");
        check(names.contains("setSelectedItem"), "method setSelectedItem");
        check(names.contains("getSelectedItem"), "method getSelectedItem");
        check(names.contains("getSize"), "method getSize");
        check(names.contains("getElementAt"), "method getElementAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
