/*
 * @test
 * @summary java.awt.peer.DesktopPeer conformance: interface reflection.
 * @run main DesktopPeerTest
 */

public class DesktopPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.DesktopPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "isSupported"), "method isSupported");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "edit"), "method edit");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "mail"), "method mail");
        check(hasMethod(c, "browse"), "method browse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
