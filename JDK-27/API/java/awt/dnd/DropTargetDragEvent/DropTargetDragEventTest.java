/*
 * @test
 * @summary java.awt.dnd.DropTargetDragEvent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DropTargetDragEventTest
 */

import java.lang.reflect.*;

public class DropTargetDragEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTargetDragEvent", false, DropTargetDragEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DropTargetDragEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.awt.dnd.DropTargetEvent"), "extends DropTargetEvent");
        check(hasMethod(c, "getSourceActions"), "declares getSourceActions");
        check(hasMethod(c, "getLocation"), "declares getLocation");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
        java.awt.dnd.DropTargetContext ctx = dt.getDropTargetContext();
        java.awt.dnd.DropTargetDragEvent e = new java.awt.dnd.DropTargetDragEvent(ctx, new java.awt.Point(1,2), java.awt.dnd.DnDConstants.ACTION_COPY, java.awt.dnd.DnDConstants.ACTION_COPY);
        check(e.getDropTargetContext() == ctx, "getDropTargetContext");
        check(e.getLocation() != null && e.getLocation().x == 1 && e.getLocation().y == 2, "getLocation");
        check(e.getSourceActions() == java.awt.dnd.DnDConstants.ACTION_COPY, "getSourceActions");
        check(e.getDropAction() == java.awt.dnd.DnDConstants.ACTION_COPY, "getDropAction");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
