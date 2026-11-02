/*
 * @test
 * @summary java.awt.peer.FramePeer conformance: interface reflection.
 * @run main FramePeerTest
 */

public class FramePeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.FramePeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.peer.WindowPeer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setTitle"), "method setTitle");
        check(hasMethod(c, "setMenuBar"), "method setMenuBar");
        check(hasMethod(c, "setResizable"), "method setResizable");
        check(hasMethod(c, "setState"), "method setState");
        check(hasMethod(c, "getState"), "method getState");
        check(hasMethod(c, "setMaximizedBounds"), "method setMaximizedBounds");
        check(hasMethod(c, "setBoundsPrivate"), "method setBoundsPrivate");
        check(hasMethod(c, "getBoundsPrivate"), "method getBoundsPrivate");
        check(hasMethod(c, "emulateActivation"), "method emulateActivation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
