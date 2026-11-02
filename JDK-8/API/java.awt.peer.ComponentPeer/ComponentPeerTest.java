/*
 * @test
 * @summary java.awt.peer.ComponentPeer conformance: interface reflection.
 * @run main ComponentPeerTest
 */

public class ComponentPeerTest {
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
        Class<?> c = Class.forName("java.awt.peer.ComponentPeer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "isObscured"), "method isObscured");
        check(hasMethod(c, "canDetermineObscurity"), "method canDetermineObscurity");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "setEnabled"), "method setEnabled");
        check(hasMethod(c, "paint"), "method paint");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "setBounds"), "method setBounds");
        check(hasMethod(c, "handleEvent"), "method handleEvent");
        check(hasMethod(c, "coalescePaintEvent"), "method coalescePaintEvent");
        check(hasMethod(c, "getLocationOnScreen"), "method getLocationOnScreen");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getGraphics"), "method getGraphics");
        check(hasMethod(c, "getFontMetrics"), "method getFontMetrics");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "setForeground"), "method setForeground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "updateCursorImmediately"), "method updateCursorImmediately");
        check(hasMethod(c, "requestFocus"), "method requestFocus");
        check(hasMethod(c, "isFocusable"), "method isFocusable");
        check(hasMethod(c, "createImage"), "method createImage");
        check(hasMethod(c, "createVolatileImage"), "method createVolatileImage");
        check(hasMethod(c, "prepareImage"), "method prepareImage");
        check(hasMethod(c, "checkImage"), "method checkImage");
        check(hasMethod(c, "getGraphicsConfiguration"), "method getGraphicsConfiguration");
        check(hasMethod(c, "handlesWheelScrolling"), "method handlesWheelScrolling");
        check(hasMethod(c, "createBuffers"), "method createBuffers");
        check(hasMethod(c, "getBackBuffer"), "method getBackBuffer");
        check(hasMethod(c, "flip"), "method flip");
        check(hasMethod(c, "destroyBuffers"), "method destroyBuffers");
        check(hasMethod(c, "reparent"), "method reparent");
        check(hasMethod(c, "isReparentSupported"), "method isReparentSupported");
        check(hasMethod(c, "layout"), "method layout");
        check(hasMethod(c, "applyShape"), "method applyShape");
        check(hasMethod(c, "setZOrder"), "method setZOrder");
        check(hasMethod(c, "updateGraphicsData"), "method updateGraphicsData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
