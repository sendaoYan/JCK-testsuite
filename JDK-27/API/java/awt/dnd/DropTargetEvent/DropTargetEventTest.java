/*
 * @test
 * @summary java.awt.dnd.DropTargetEvent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DropTargetEventTest
 */

import java.lang.reflect.*;

public class DropTargetEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTargetEvent", false, DropTargetEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DropTargetEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "extends EventObject");
        check(hasMethod(c, "getDropTargetContext"), "declares getDropTargetContext");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
        java.awt.dnd.DropTargetContext ctx = dt.getDropTargetContext();
        java.awt.dnd.DropTargetEvent e = new java.awt.dnd.DropTargetEvent(ctx);
        check(e.getDropTargetContext() == ctx, "getDropTargetContext");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
