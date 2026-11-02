/*
 * @test
 * @summary java.awt.dnd.DropTargetDragEvent conformance: class reflection.
 * @run main DropTargetDragEventTest
 */

public class DropTargetDragEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTargetDragEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.dnd.DropTargetEvent"), "superclass java.awt.dnd.DropTargetEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getCurrentDataFlavors"), "method getCurrentDataFlavors");
        check(hasMethod(c, "getCurrentDataFlavorsAsList"), "method getCurrentDataFlavorsAsList");
        check(hasMethod(c, "isDataFlavorSupported"), "method isDataFlavorSupported");
        check(hasMethod(c, "getSourceActions"), "method getSourceActions");
        check(hasMethod(c, "getDropAction"), "method getDropAction");
        check(hasMethod(c, "getTransferable"), "method getTransferable");
        check(hasMethod(c, "acceptDrag"), "method acceptDrag");
        check(hasMethod(c, "rejectDrag"), "method rejectDrag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
