/*
 * @test
 * @summary java.awt.dnd.MouseDragGestureRecognizer conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MouseDragGestureRecognizerTest
 */

import java.lang.reflect.*;

public class MouseDragGestureRecognizerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.MouseDragGestureRecognizer", false, MouseDragGestureRecognizerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.MouseDragGestureRecognizer");
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
        check(c.getSuperclass().getName().equals("java.awt.dnd.DragGestureRecognizer"), "extends DragGestureRecognizer");
        check(hasInterface(c, "java.awt.event.MouseListener"), "implements MouseListener");
        check(hasInterface(c, "java.awt.event.MouseMotionListener"), "implements MouseMotionListener");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DragSource ds = new java.awt.dnd.DragSource();
        java.awt.dnd.MouseDragGestureRecognizer r = new java.awt.dnd.MouseDragGestureRecognizer(ds, null, java.awt.dnd.DnDConstants.ACTION_COPY) {};
        check(r.getDragSource() == ds, "getDragSource");
        check(r.getSourceActions() == java.awt.dnd.DnDConstants.ACTION_COPY, "getSourceActions");
        r.mouseClicked(null);
        r.mousePressed(null);
        r.mouseReleased(null);
        r.mouseEntered(null);
        r.mouseExited(null);
        r.mouseDragged(null);
        r.mouseMoved(null);
        check(true, "mouse listener methods callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
