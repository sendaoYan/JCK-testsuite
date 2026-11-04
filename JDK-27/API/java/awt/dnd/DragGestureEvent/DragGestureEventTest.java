/*
 * @test
 * @summary java.awt.dnd.DragGestureEvent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DragGestureEventTest
 */

import java.lang.reflect.*;

public class DragGestureEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragGestureEvent", false, DragGestureEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DragGestureEvent");
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
        check(hasMethod(c, "getDragAction"), "declares getDragAction");
        check(hasMethod(c, "startDrag"), "declares startDrag");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DragSource ds = new java.awt.dnd.DragSource();
        java.awt.Button b = new java.awt.Button("b");
        java.awt.dnd.DragGestureRecognizer r = new java.awt.dnd.DragGestureRecognizer(ds, b, java.awt.dnd.DnDConstants.ACTION_COPY) { public void registerListeners(){} public void unregisterListeners(){} };
        java.awt.event.MouseEvent me = new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_PRESSED, 0L, 0, 5, 6, 1, false);
        java.awt.dnd.DragGestureEvent ge = new java.awt.dnd.DragGestureEvent(r, java.awt.dnd.DnDConstants.ACTION_COPY, new java.awt.Point(1,2), java.util.Collections.singletonList(me));
        check(ge.getDragAction() == java.awt.dnd.DnDConstants.ACTION_COPY, "getDragAction");
        check(ge.getDragOrigin() != null && ge.getDragOrigin().x == 1 && ge.getDragOrigin().y == 2, "getDragOrigin");
        check(ge.getComponent() == b, "getComponent");
        check(ge.getDragSource() == ds, "getDragSource");
        check(ge.getSourceAsDragGestureRecognizer() == r, "getSourceAsDragGestureRecognizer");
        java.util.Iterator<?> it = ge.iterator();
        check(it != null && it.hasNext(), "iterator has event");
        Object[] arr = ge.toArray();
        check(arr != null && arr.length == 1, "toArray length 1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
