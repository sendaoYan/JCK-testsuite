/*
 * @test
 * @summary javax.swing.JSpinner.DefaultEditor conformance: type shape via reflection (headless-safe).
 * @run main DefaultEditorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultEditorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JSpinner$DefaultEditor", false, DefaultEditorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JPanel"), "superclass");
        check(hasInterface(c, "javax.swing.event.ChangeListener"), "implements javax.swing.event.ChangeListener");
        check(hasInterface(c, "java.beans.PropertyChangeListener"), "implements java.beans.PropertyChangeListener");
        check(hasInterface(c, "java.awt.LayoutManager"), "implements java.awt.LayoutManager");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("dismiss"), "method dismiss");
        check(names.contains("getSpinner"), "method getSpinner");
        check(names.contains("getTextField"), "method getTextField");
        check(names.contains("stateChanged"), "method stateChanged");
        check(names.contains("propertyChange"), "method propertyChange");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("removeLayoutComponent"), "method removeLayoutComponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
