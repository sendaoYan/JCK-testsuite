/*
 * @test
 * @summary javax.swing.JApplet conformance: type shape via reflection (headless-safe).
 * @run main JAppletTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JAppletTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JApplet", false, JAppletTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.applet.Applet"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        check(hasInterface(c, "javax.swing.RootPaneContainer"), "implements javax.swing.RootPaneContainer");
        check(hasInterface(c, "javax.swing.TransferHandler$HasGetTransferHandler"), "implements javax.swing.TransferHandler$HasGetTransferHandler");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setTransferHandler"), "method setTransferHandler");
        check(names.contains("getTransferHandler"), "method getTransferHandler");
        check(names.contains("update"), "method update");
        check(names.contains("setJMenuBar"), "method setJMenuBar");
        check(names.contains("getJMenuBar"), "method getJMenuBar");
        check(names.contains("remove"), "method remove");
        check(names.contains("setLayout"), "method setLayout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
