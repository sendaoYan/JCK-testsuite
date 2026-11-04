/*
 * @test
 * @summary java.awt.dnd.DragGestureRecognizer conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DragGestureRecognizerTest
 */

import java.lang.reflect.*;

public class DragGestureRecognizerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragGestureRecognizer", false, DragGestureRecognizerTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.DragGestureRecognizer");
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
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasMethod(c, "getDragSource"), "declares getDragSource");
        check(hasMethod(c, "addDragGestureListener"), "declares addDragGestureListener");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.DragSource ds = new java.awt.dnd.DragSource();
        java.awt.dnd.DragGestureRecognizer r = new java.awt.dnd.DragGestureRecognizer(ds, null, java.awt.dnd.DnDConstants.ACTION_COPY) {
            public void registerListeners() {}
            public void unregisterListeners() {}
        };
        check(r.getDragSource() == ds, "getDragSource");
        check(r.getComponent() == null, "getComponent null");
        r.setComponent(null);
        check(r.getComponent() == null, "setComponent null");
        check(r.getSourceActions() == java.awt.dnd.DnDConstants.ACTION_COPY, "getSourceActions");
        r.setSourceActions(java.awt.dnd.DnDConstants.ACTION_MOVE);
        check(r.getSourceActions() == java.awt.dnd.DnDConstants.ACTION_MOVE, "setSourceActions");
        check(r.getTriggerEvent() == null, "getTriggerEvent null before append");
        java.awt.dnd.DragGestureListener l = e -> {};
        r.addDragGestureListener(l);
        boolean tooMany = false;
        try { r.addDragGestureListener(e -> {}); } catch (java.util.TooManyListenersException e) { tooMany = true; }
        check(tooMany, "second listener TooManyListenersException");
        r.removeDragGestureListener(l);
        r.addDragGestureListener(e -> {});
        check(true, "remove then add ok");
        r.resetRecognizer();
        check(true, "resetRecognizer callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
