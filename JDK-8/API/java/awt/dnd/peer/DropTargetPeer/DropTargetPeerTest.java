/*
 * @test
 * @summary java.awt.dnd.peer.DropTargetPeer conformance: interface reflection.
 * @run main DropTargetPeerTest
 */

public class DropTargetPeerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.peer.DropTargetPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addDropTarget"), "method addDropTarget");
        check(hasMethod(c, "removeDropTarget"), "method removeDropTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
