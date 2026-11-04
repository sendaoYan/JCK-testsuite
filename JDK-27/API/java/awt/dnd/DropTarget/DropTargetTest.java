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
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            boolean threw = false;
            try { new java.awt.dnd.DropTarget(); } catch (java.awt.HeadlessException e) { threw = true; }
            check(threw, "DropTarget ctor HeadlessException when headless");
            return;
        }
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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // DnDConstants exact values
        check(java.awt.dnd.DnDConstants.ACTION_NONE == 0, "ACTION_NONE == 0");
        check(java.awt.dnd.DnDConstants.ACTION_COPY == 1, "ACTION_COPY == 1");
        check(java.awt.dnd.DnDConstants.ACTION_MOVE == 2, "ACTION_MOVE == 2");
        check(java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE
              == (java.awt.dnd.DnDConstants.ACTION_COPY | java.awt.dnd.DnDConstants.ACTION_MOVE),
              "ACTION_COPY_OR_MOVE == COPY|MOVE == 3");
        check(java.awt.dnd.DnDConstants.ACTION_LINK == 0x40000000, "ACTION_LINK == 0x40000000");
        check(java.awt.dnd.DnDConstants.ACTION_REFERENCE == java.awt.dnd.DnDConstants.ACTION_LINK,
              "ACTION_REFERENCE aliases ACTION_LINK");
        // bit independence of the three actions
        check((java.awt.dnd.DnDConstants.ACTION_COPY & java.awt.dnd.DnDConstants.ACTION_MOVE) == 0,
              "COPY and MOVE bits independent");
        // InvalidDnDOperationException
        java.awt.dnd.InvalidDnDOperationException noArg = new java.awt.dnd.InvalidDnDOperationException();
        check(noArg.getMessage() != null, "no-arg ctor has default message");
        java.awt.dnd.InvalidDnDOperationException withMsg = new java.awt.dnd.InvalidDnDOperationException("why");
        check(withMsg.getMessage().equals("why"), "message ctor");
        check(withMsg.getCause() == null, "no cause");
        check(withMsg instanceof java.lang.IllegalStateException,
              "InvalidDnDOperationException extends IllegalStateException");
        // DragSource headless facts: no DISPLAY -> HeadlessException for the singleton and ctor
        boolean headless = java.awt.GraphicsEnvironment.isHeadless();
        if (headless) {
            boolean threw = false;
            try { java.awt.dnd.DragSource.getDefaultDragSource(); }
            catch (java.awt.HeadlessException e) { threw = true; }
            check(threw, "getDefaultDragSource throws HeadlessException headless");
            threw = false;
            try { new java.awt.dnd.DragSource(); }
            catch (java.awt.HeadlessException e) { threw = true; }
            check(threw, "DragSource ctor throws HeadlessException headless");
            // static cursor fields remain null without a toolkit (observed)
            check(java.awt.dnd.DragSource.DefaultCopyDrop == null, "DefaultCopyDrop null headless (observed)");
            check(java.awt.dnd.DragSource.DefaultLinkNoDrop == null, "DefaultLinkNoDrop null headless (observed)");
        }
        // isDragImageSupported: stable boolean
        boolean dis1 = java.awt.dnd.DragSource.isDragImageSupported();
        check(java.awt.dnd.DragSource.isDragImageSupported() == dis1, "isDragImageSupported stable");
        // getDragThreshold works headless (system property driven)
        int thr = java.awt.dnd.DragSource.getDragThreshold();
        check(thr > 0, "getDragThreshold positive: " + thr);
        // DropTarget headless: all ctors throw HeadlessException
        if (headless) {
            boolean threw2 = false;
            try { new java.awt.dnd.DropTarget(); } catch (java.awt.HeadlessException e) { threw2 = true; }
            check(threw2, "DropTarget() throws HeadlessException headless");
            threw2 = false;
            try { new java.awt.dnd.DropTarget(null, (java.awt.dnd.DropTargetListener) null); }
            catch (java.awt.HeadlessException e) { threw2 = true; }
            check(threw2, "DropTarget(Component,DropTargetListener) HeadlessException headless");
        }
        // DragGestureEvent structural contract: EventObject behaviors verified via class shape
        check(java.util.EventObject.class.isAssignableFrom(java.awt.dnd.DragGestureEvent.class),
              "DragGestureEvent is EventObject");
        check(java.awt.dnd.Autoscroll.class.isInterface(), "Autoscroll is interface");
        check(java.awt.dnd.DragSourceListener.class.isInterface(), "DragSourceListener is interface");
        // adapters: no-op methods callable without effect
        java.awt.dnd.DragSourceAdapter dsa = new java.awt.dnd.DragSourceAdapter() { };
        dsa.dragEnter(null); dsa.dragOver(null); dsa.dropActionChanged(null);
        dsa.dragExit(null); dsa.dragMouseMoved(null);
        check(true, "DragSourceAdapter no-ops callable");
        java.awt.dnd.DropTargetAdapter dta = new java.awt.dnd.DropTargetAdapter() {
            public void drop(java.awt.dnd.DropTargetDropEvent e) { }
        };
        dta.dragEnter(null); dta.dragOver(null); dta.dropActionChanged(null); dta.dragExit(null);
        check(true, "DropTargetAdapter no-ops callable");
    }

}
