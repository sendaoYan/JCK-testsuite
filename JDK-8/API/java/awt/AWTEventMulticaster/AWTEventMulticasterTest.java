/*
 * @test
 * @summary java.awt.AWTEventMulticaster conformance: class reflection.
 * @run main AWTEventMulticasterTest
 */

public class AWTEventMulticasterTest {
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
        Class<?> c = Class.forName("java.awt.AWTEventMulticaster");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.event.ActionListener", "java.awt.event.AdjustmentListener", "java.awt.event.ComponentListener", "java.awt.event.ContainerListener", "java.awt.event.FocusListener", "java.awt.event.HierarchyBoundsListener", "java.awt.event.HierarchyListener", "java.awt.event.InputMethodListener", "java.awt.event.ItemListener", "java.awt.event.KeyListener", "java.awt.event.MouseListener", "java.awt.event.MouseMotionListener", "java.awt.event.MouseWheelListener", "java.awt.event.TextListener", "java.awt.event.WindowFocusListener", "java.awt.event.WindowListener", "java.awt.event.WindowStateListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "componentResized"), "method componentResized");
        check(hasMethod(c, "componentMoved"), "method componentMoved");
        check(hasMethod(c, "componentShown"), "method componentShown");
        check(hasMethod(c, "componentHidden"), "method componentHidden");
        check(hasMethod(c, "componentAdded"), "method componentAdded");
        check(hasMethod(c, "componentRemoved"), "method componentRemoved");
        check(hasMethod(c, "focusGained"), "method focusGained");
        check(hasMethod(c, "focusLost"), "method focusLost");
        check(hasMethod(c, "keyTyped"), "method keyTyped");
        check(hasMethod(c, "keyPressed"), "method keyPressed");
        check(hasMethod(c, "keyReleased"), "method keyReleased");
        check(hasMethod(c, "mouseClicked"), "method mouseClicked");
        check(hasMethod(c, "mousePressed"), "method mousePressed");
        check(hasMethod(c, "mouseReleased"), "method mouseReleased");
        check(hasMethod(c, "mouseEntered"), "method mouseEntered");
        check(hasMethod(c, "mouseExited"), "method mouseExited");
        check(hasMethod(c, "mouseDragged"), "method mouseDragged");
        check(hasMethod(c, "mouseMoved"), "method mouseMoved");
        check(hasMethod(c, "windowOpened"), "method windowOpened");
        check(hasMethod(c, "windowClosing"), "method windowClosing");
        check(hasMethod(c, "windowClosed"), "method windowClosed");
        check(hasMethod(c, "windowIconified"), "method windowIconified");
        check(hasMethod(c, "windowDeiconified"), "method windowDeiconified");
        check(hasMethod(c, "windowActivated"), "method windowActivated");
        check(hasMethod(c, "windowDeactivated"), "method windowDeactivated");
        check(hasMethod(c, "windowStateChanged"), "method windowStateChanged");
        check(hasMethod(c, "windowGainedFocus"), "method windowGainedFocus");
        check(hasMethod(c, "windowLostFocus"), "method windowLostFocus");
        check(hasMethod(c, "actionPerformed"), "method actionPerformed");
        check(hasMethod(c, "itemStateChanged"), "method itemStateChanged");
        check(hasMethod(c, "adjustmentValueChanged"), "method adjustmentValueChanged");
        check(hasMethod(c, "textValueChanged"), "method textValueChanged");
        check(hasMethod(c, "inputMethodTextChanged"), "method inputMethodTextChanged");
        check(hasMethod(c, "caretPositionChanged"), "method caretPositionChanged");
        check(hasMethod(c, "hierarchyChanged"), "method hierarchyChanged");
        check(hasMethod(c, "ancestorMoved"), "method ancestorMoved");
        check(hasMethod(c, "ancestorResized"), "method ancestorResized");
        check(hasMethod(c, "mouseWheelMoved"), "method mouseWheelMoved");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "getListeners"), "method getListeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
