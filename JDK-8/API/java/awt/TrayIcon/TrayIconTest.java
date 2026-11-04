/*
 * @test
 * @summary java.awt.TrayIcon conformance: class reflection.
 * @run main TrayIconTest
 */

public class TrayIconTest {
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
        Class<?> c = Class.forName("java.awt.TrayIcon");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setImage"), "method setImage");
        check(hasMethod(c, "getImage"), "method getImage");
        check(hasMethod(c, "setPopupMenu"), "method setPopupMenu");
        check(hasMethod(c, "getPopupMenu"), "method getPopupMenu");
        check(hasMethod(c, "setToolTip"), "method setToolTip");
        check(hasMethod(c, "getToolTip"), "method getToolTip");
        check(hasMethod(c, "setImageAutoSize"), "method setImageAutoSize");
        check(hasMethod(c, "isImageAutoSize"), "method isImageAutoSize");
        check(hasMethod(c, "addMouseListener"), "method addMouseListener");
        check(hasMethod(c, "removeMouseListener"), "method removeMouseListener");
        check(hasMethod(c, "getMouseListeners"), "method getMouseListeners");
        check(hasMethod(c, "addMouseMotionListener"), "method addMouseMotionListener");
        check(hasMethod(c, "removeMouseMotionListener"), "method removeMouseMotionListener");
        check(hasMethod(c, "getMouseMotionListeners"), "method getMouseMotionListeners");
        check(hasMethod(c, "getActionCommand"), "method getActionCommand");
        check(hasMethod(c, "setActionCommand"), "method setActionCommand");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "displayMessage"), "method displayMessage");
        check(hasMethod(c, "getSize"), "method getSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
