/*
 * @test
 * @summary java.awt.peer.MouseInfoPeer conformance: interface reflection.
 * @run main MouseInfoPeerTest
 */

public class MouseInfoPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.MouseInfoPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "fillPointWithCoords"), "method fillPointWithCoords");
        check(hasMethod(c, "isWindowUnderMouse"), "method isWindowUnderMouse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
