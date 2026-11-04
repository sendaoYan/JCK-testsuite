/*
 * @test
 * @summary java.awt.peer.WindowPeer conformance: interface reflection.
 * @run main WindowPeerTest
 */

public class WindowPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.WindowPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.peer.ContainerPeer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "toFront"), "method toFront");
        check(hasMethod(c, "toBack"), "method toBack");
        check(hasMethod(c, "updateAlwaysOnTopState"), "method updateAlwaysOnTopState");
        check(hasMethod(c, "updateFocusableWindowState"), "method updateFocusableWindowState");
        check(hasMethod(c, "setModalBlocked"), "method setModalBlocked");
        check(hasMethod(c, "updateMinimumSize"), "method updateMinimumSize");
        check(hasMethod(c, "updateIconImages"), "method updateIconImages");
        check(hasMethod(c, "setOpacity"), "method setOpacity");
        check(hasMethod(c, "setOpaque"), "method setOpaque");
        check(hasMethod(c, "updateWindow"), "method updateWindow");
        check(hasMethod(c, "repositionSecurityWarning"), "method repositionSecurityWarning");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
