/*
 * @test
 * @summary java.awt.peer.MenuComponentPeer conformance: interface reflection.
 * @run main MenuComponentPeerTest
 */

public class MenuComponentPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.MenuComponentPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "setFont"), "method setFont");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
