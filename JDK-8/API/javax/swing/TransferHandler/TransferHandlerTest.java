/*
 * @test
 * @summary javax.swing.TransferHandler conformance: type shape via reflection (headless-safe).
 * @run main TransferHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TransferHandlerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.TransferHandler", false, TransferHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getCutAction"), "method getCutAction");
        check(names.contains("getCopyAction"), "method getCopyAction");
        check(names.contains("getPasteAction"), "method getPasteAction");
        check(names.contains("setDragImage"), "method setDragImage");
        check(names.contains("getDragImage"), "method getDragImage");
        check(names.contains("setDragImageOffset"), "method setDragImageOffset");
        check(names.contains("getDragImageOffset"), "method getDragImageOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
