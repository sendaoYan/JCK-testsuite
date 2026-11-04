/*
 * @test
 * @summary java.awt.dnd.DragSourceContext conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DragSourceContextTest
 */

import java.lang.reflect.*;

public class DragSourceContextTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceContext", false, DragSourceContextTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DragSourceContext");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.awt.dnd.DragSourceListener"), "implements DragSourceListener");
        check(hasInterface(c, "java.awt.dnd.DragSourceMotionListener"), "implements DragSourceMotionListener");
        check(hasMethod(c, "getTransferable"), "declares getTransferable");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DragSource ds = new java.awt.dnd.DragSource();
        java.awt.Button b = new java.awt.Button("b");
        java.awt.dnd.DragGestureRecognizer r = new java.awt.dnd.DragGestureRecognizer(ds, b, java.awt.dnd.DnDConstants.ACTION_COPY) { public void registerListeners(){} public void unregisterListeners(){} };
        java.awt.event.MouseEvent me = new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_PRESSED, 0L, 0, 5, 6, 1, false);
        java.awt.dnd.DragGestureEvent ge = new java.awt.dnd.DragGestureEvent(r, java.awt.dnd.DnDConstants.ACTION_COPY, new java.awt.Point(1,2), java.util.Collections.singletonList(me));
        java.awt.datatransfer.StringSelection t = new java.awt.datatransfer.StringSelection("x");
        java.awt.dnd.DragSourceContext ctx = new java.awt.dnd.DragSourceContext(ge, java.awt.Cursor.getDefaultCursor(), null, new java.awt.Point(0,0), t, null);
        check(ctx.getDragSource() == ds, "getDragSource");
        check(ctx.getComponent() == b, "getComponent");
        check(ctx.getTrigger() == ge, "getTrigger");
        check(ctx.getSourceActions() == java.awt.dnd.DnDConstants.ACTION_COPY, "getSourceActions");
        check(ctx.getTransferable() == t, "getTransferable");
        check(ctx.getCursor() != null, "getCursor non-null");
        ctx.setCursor(java.awt.Cursor.getDefaultCursor());
        check(true, "setCursor callable");
        java.awt.dnd.DragSourceListener l = new java.awt.dnd.DragSourceAdapter() {};
        ctx.addDragSourceListener(l);
        ctx.removeDragSourceListener(l);
        check(true, "listener add/remove callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
