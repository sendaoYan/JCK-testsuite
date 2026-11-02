/*
 * @test
 * @summary javax.swing.text.AbstractDocument.DefaultDocumentEvent conformance: type shape via reflection (headless-safe).
 * @run main DefaultDocumentEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultDocumentEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$DefaultDocumentEvent", false, DefaultDocumentEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.CompoundEdit"), "superclass");
        check(hasInterface(c, "javax.swing.event.DocumentEvent"), "implements javax.swing.event.DocumentEvent");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("toString"), "method toString");
        check(names.contains("addEdit"), "method addEdit");
        check(names.contains("redo"), "method redo");
        check(names.contains("undo"), "method undo");
        check(names.contains("isSignificant"), "method isSignificant");
        check(names.contains("getPresentationName"), "method getPresentationName");
        check(names.contains("getUndoPresentationName"), "method getUndoPresentationName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
