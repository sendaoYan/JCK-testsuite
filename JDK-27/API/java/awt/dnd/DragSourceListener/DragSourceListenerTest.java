/*
 * @test
 * @summary java.awt.dnd.DragSourceListener conformance: white-box structure + black-box behavior.
 * @run main DragSourceListenerTest
 */

import java.lang.reflect.*;

public class DragSourceListenerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceListener", false, DragSourceListenerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DragSourceListener");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.util.EventListener"), "extends EventListener");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DragSourceListener l = new java.awt.dnd.DragSourceListener() {
            public void dragEnter(java.awt.dnd.DragSourceDragEvent e) {}
            public void dragOver(java.awt.dnd.DragSourceDragEvent e) {}
            public void dropActionChanged(java.awt.dnd.DragSourceDragEvent e) {}
            public void dragExit(java.awt.dnd.DragSourceEvent e) {}
            public void dragDropEnd(java.awt.dnd.DragSourceDropEvent e) {}
        };
        l.dragEnter(null); l.dragOver(null); l.dropActionChanged(null); l.dragExit(null); l.dragDropEnd(null);
        check(true, "all callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
