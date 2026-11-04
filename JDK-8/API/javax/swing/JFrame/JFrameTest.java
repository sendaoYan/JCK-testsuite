/*
 * @test
 * @summary javax.swing.JFrame conformance: type shape via reflection (headless-safe).
 * @run main JFrameTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JFrameTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JFrame", false, JFrameTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Frame"), "superclass");
        check(hasInterface(c, "javax.swing.WindowConstants"), "implements javax.swing.WindowConstants");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        check(hasInterface(c, "javax.swing.RootPaneContainer"), "implements javax.swing.RootPaneContainer");
        check(hasInterface(c, "javax.swing.TransferHandler$HasGetTransferHandler"), "implements javax.swing.TransferHandler$HasGetTransferHandler");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setDefaultCloseOperation"), "method setDefaultCloseOperation");
        check(names.contains("getDefaultCloseOperation"), "method getDefaultCloseOperation");
        check(names.contains("setTransferHandler"), "method setTransferHandler");
        check(names.contains("getTransferHandler"), "method getTransferHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
