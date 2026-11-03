/*
 * @test
 * @summary java.awt.dnd.DropTarget conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DropTargetTest
 */

import java.lang.reflect.*;

public class DropTargetTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTarget", false, DropTargetTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DropTarget");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.awt.dnd.DropTargetListener"), "implements DropTargetListener");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasMethod(c, "getDropTargetContext"), "declares getDropTargetContext");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
        check(dt.getComponent() == null, "no component");
        check(dt.isActive() == true, "default active true");
        dt.setActive(false);
        check(dt.isActive() == false, "setActive false");
        dt.setActive(true);
        check(dt.isActive() == true, "setActive true");
        dt.setDefaultActions(java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE);
        check(dt.getDefaultActions() == java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE, "set/getDefaultActions");
        java.awt.dnd.DropTargetListener l = new java.awt.dnd.DropTargetAdapter() { public void drop(java.awt.dnd.DropTargetDropEvent e) {} };
        dt.addDropTargetListener(l);
        dt.removeDropTargetListener(l);
        check(true, "listener add/remove callable");
        java.awt.datatransfer.FlavorMap fm = dt.getFlavorMap();
        check(fm != null, "getFlavorMap non-null");
        dt.setFlavorMap(fm);
        check(dt.getFlavorMap() == fm, "setFlavorMap same");
        check(dt.getDropTargetContext() != null, "getDropTargetContext non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
