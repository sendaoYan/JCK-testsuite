/*
 * @test
 * @summary java.awt.dnd.DropTargetDropEvent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DropTargetDropEventTest
 */

import java.lang.reflect.*;

public class DropTargetDropEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTargetDropEvent", false, DropTargetDropEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DropTargetDropEvent");
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
        check(hasMethod(c, "acceptDrop"), "declares acceptDrop");
        check(hasMethod(c, "isLocalTransfer"), "declares isLocalTransfer");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
        java.awt.dnd.DropTargetContext ctx = dt.getDropTargetContext();
        java.awt.dnd.DropTargetDropEvent e = new java.awt.dnd.DropTargetDropEvent(ctx, new java.awt.Point(3,4), java.awt.dnd.DnDConstants.ACTION_MOVE, java.awt.dnd.DnDConstants.ACTION_COPY);
        check(e.getDropTargetContext() == ctx, "getDropTargetContext");
        check(e.getLocation() != null && e.getLocation().x == 3, "getLocation");
        check(e.getSourceActions() == java.awt.dnd.DnDConstants.ACTION_COPY, "getSourceActions");
        check(e.getDropAction() == java.awt.dnd.DnDConstants.ACTION_MOVE, "getDropAction");
        check(e.isLocalTransfer() == false, "isLocalTransfer false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
