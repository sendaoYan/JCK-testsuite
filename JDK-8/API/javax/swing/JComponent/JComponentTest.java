/*
 * @test
 * @summary javax.swing.JComponent conformance: type shape via reflection (headless-safe).
 * @run main JComponentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JComponentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JComponent", false, JComponentTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Container"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasInterface(c, "javax.swing.TransferHandler$HasGetTransferHandler"), "implements javax.swing.TransferHandler$HasGetTransferHandler");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setInheritsPopupMenu"), "method setInheritsPopupMenu");
        check(names.contains("getInheritsPopupMenu"), "method getInheritsPopupMenu");
        check(names.contains("setComponentPopupMenu"), "method setComponentPopupMenu");
        check(names.contains("getComponentPopupMenu"), "method getComponentPopupMenu");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("update"), "method update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
