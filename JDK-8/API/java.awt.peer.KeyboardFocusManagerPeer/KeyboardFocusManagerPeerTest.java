/*
 * @test
 * @summary java.awt.peer.KeyboardFocusManagerPeer conformance: interface reflection.
 * @run main KeyboardFocusManagerPeerTest
 */

public class KeyboardFocusManagerPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.KeyboardFocusManagerPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setCurrentFocusedWindow"), "method setCurrentFocusedWindow");
        check(hasMethod(c, "getCurrentFocusedWindow"), "method getCurrentFocusedWindow");
        check(hasMethod(c, "setCurrentFocusOwner"), "method setCurrentFocusOwner");
        check(hasMethod(c, "getCurrentFocusOwner"), "method getCurrentFocusOwner");
        check(hasMethod(c, "clearGlobalFocusOwner"), "method clearGlobalFocusOwner");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
