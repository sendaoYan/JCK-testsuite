/*
 * @test
 * @summary java.awt.dnd.peer.DropTargetContextPeer conformance: interface reflection.
 * @run main DropTargetContextPeerTest
 */

public class DropTargetContextPeerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.peer.DropTargetContextPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setTargetActions"), "method setTargetActions");
        check(hasMethod(c, "getTargetActions"), "method getTargetActions");
        check(hasMethod(c, "getDropTarget"), "method getDropTarget");
        check(hasMethod(c, "getTransferDataFlavors"), "method getTransferDataFlavors");
        check(hasMethod(c, "getTransferable"), "method getTransferable");
        check(hasMethod(c, "isTransferableJVMLocal"), "method isTransferableJVMLocal");
        check(hasMethod(c, "acceptDrag"), "method acceptDrag");
        check(hasMethod(c, "rejectDrag"), "method rejectDrag");
        check(hasMethod(c, "acceptDrop"), "method acceptDrop");
        check(hasMethod(c, "rejectDrop"), "method rejectDrop");
        check(hasMethod(c, "dropComplete"), "method dropComplete");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
