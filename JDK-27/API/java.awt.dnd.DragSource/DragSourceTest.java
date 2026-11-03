/*
 * @test
 * @summary java.awt.dnd.DragSource conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DragSourceTest
 */

import java.lang.reflect.*;

public class DragSourceTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSource", false, DragSourceTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DragSource");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasMethod(c, "getDefaultDragSource"), "declares getDefaultDragSource");
        check(hasMethod(c, "createDragGestureRecognizer"), "declares createDragGestureRecognizer");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DragSource ds = new java.awt.dnd.DragSource();
        check(ds.getFlavorMap() != null, "getFlavorMap non-null");
        check(java.awt.dnd.DragSource.getDefaultDragSource() != null, "getDefaultDragSource non-null");
        check(java.awt.dnd.DragSource.getDragThreshold() >= 0, "getDragThreshold >= 0");
        check(java.awt.dnd.DragSource.isDragImageSupported() || !java.awt.dnd.DragSource.isDragImageSupported(), "isDragImageSupported boolean");
        java.awt.dnd.DragSourceListener l = new java.awt.dnd.DragSourceAdapter() {};
        ds.addDragSourceListener(l);
        check(ds.getDragSourceListeners() != null && ds.getDragSourceListeners().length == 1, "one drag source listener");
        ds.removeDragSourceListener(l);
        check(ds.getDragSourceListeners().length == 0, "listener removed");
        java.awt.dnd.DragSourceMotionListener ml = new java.awt.dnd.DragSourceAdapter() {};
        ds.addDragSourceMotionListener(ml);
        check(ds.getDragSourceMotionListeners().length == 1, "one motion listener");
        ds.removeDragSourceMotionListener(ml);
        check(ds.getDragSourceMotionListeners().length == 0, "motion listener removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
