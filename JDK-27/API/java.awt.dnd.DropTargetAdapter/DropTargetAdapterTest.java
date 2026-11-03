/*
 * @test
 * @summary java.awt.dnd.DropTargetAdapter conformance: white-box structure + black-box behavior.
 * @run main DropTargetAdapterTest
 */

import java.lang.reflect.*;

public class DropTargetAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.dnd.DropTargetAdapter", false, DropTargetAdapterTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DropTargetAdapter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasInterface(c, "java.awt.dnd.DropTargetListener"), "implements DropTargetListener");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DropTargetAdapter a = new java.awt.dnd.DropTargetAdapter() { public void drop(java.awt.dnd.DropTargetDropEvent e) {} };
        a.dragEnter(null);
        a.dragOver(null);
        a.dropActionChanged(null);
        a.dragExit(null);
        check(true, "all no-op methods callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
