/*
 * @test
 * @summary java.awt.peer.RobotPeer conformance: interface reflection.
 * @run main RobotPeerTest
 */

public class RobotPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.RobotPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "mouseMove"), "method mouseMove");
        check(hasMethod(c, "mousePress"), "method mousePress");
        check(hasMethod(c, "mouseRelease"), "method mouseRelease");
        check(hasMethod(c, "mouseWheel"), "method mouseWheel");
        check(hasMethod(c, "keyPress"), "method keyPress");
        check(hasMethod(c, "keyRelease"), "method keyRelease");
        check(hasMethod(c, "getRGBPixel"), "method getRGBPixel");
        check(hasMethod(c, "getRGBPixels"), "method getRGBPixels");
        check(hasMethod(c, "dispose"), "method dispose");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
