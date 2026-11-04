/*
 * @test
 * @summary java.awt.peer.TrayIconPeer conformance: interface reflection.
 * @run main TrayIconPeerTest
 */

public class TrayIconPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.TrayIconPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "setToolTip"), "method setToolTip");
        check(hasMethod(c, "updateImage"), "method updateImage");
        check(hasMethod(c, "displayMessage"), "method displayMessage");
        check(hasMethod(c, "showPopupMenu"), "method showPopupMenu");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
