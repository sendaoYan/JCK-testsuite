/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSplitPaneUI.BasicHorizontalLayoutManager conformance: type shape via reflection (headless-safe).
 * @run main BasicHorizontalLayoutManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BasicHorizontalLayoutManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneUI$BasicHorizontalLayoutManager", false, BasicHorizontalLayoutManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager2"), "implements java.awt.LayoutManager2");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("layoutContainer"), "method layoutContainer");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("minimumLayoutSize"), "method minimumLayoutSize");
        check(names.contains("preferredLayoutSize"), "method preferredLayoutSize");
        check(names.contains("removeLayoutComponent"), "method removeLayoutComponent");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("getLayoutAlignmentX"), "method getLayoutAlignmentX");
        check(names.contains("getLayoutAlignmentY"), "method getLayoutAlignmentY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
