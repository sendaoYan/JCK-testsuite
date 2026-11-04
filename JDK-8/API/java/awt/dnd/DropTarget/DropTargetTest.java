/*
 * @test
 * @summary java.awt.dnd.DropTarget conformance: class reflection.
 * @run main DropTargetTest
 */

public class DropTargetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.dnd.DropTarget");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.dnd.DropTargetListener", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setComponent"), "method setComponent");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "setDefaultActions"), "method setDefaultActions");
        check(hasMethod(c, "getDefaultActions"), "method getDefaultActions");
        check(hasMethod(c, "setActive"), "method setActive");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "addDropTargetListener"), "method addDropTargetListener");
        check(hasMethod(c, "removeDropTargetListener"), "method removeDropTargetListener");
        check(hasMethod(c, "dragEnter"), "method dragEnter");
        check(hasMethod(c, "dragOver"), "method dragOver");
        check(hasMethod(c, "dropActionChanged"), "method dropActionChanged");
        check(hasMethod(c, "dragExit"), "method dragExit");
        check(hasMethod(c, "drop"), "method drop");
        check(hasMethod(c, "getFlavorMap"), "method getFlavorMap");
        check(hasMethod(c, "setFlavorMap"), "method setFlavorMap");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "getDropTargetContext"), "method getDropTargetContext");
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
