/*
 * @test
 * @summary javax.swing.plaf.basic.BasicButtonListener conformance: white-box structure + black-box behavior.
 * @run main BasicButtonListenerTest
 */

import java.lang.reflect.Modifier;

public class BasicButtonListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicButtonListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 5, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.MouseListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.awt.event.MouseMotionListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.awt.event.FocusListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.event.ChangeListener"), "interface 3");
        check(c.getInterfaces()[4].getName().equals("java.beans.PropertyChangeListener"), "interface 4");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JButton b = new javax.swing.JButton("X");
        javax.swing.plaf.basic.BasicButtonListener l = new javax.swing.plaf.basic.BasicButtonListener(b);
        check(l != null, "constructs");
        l.propertyChange(new java.beans.PropertyChangeEvent(b, "text", "a", "b"));
        l.stateChanged(new javax.swing.event.ChangeEvent(b));
        l.focusGained(new java.awt.event.FocusEvent(b, java.awt.event.FocusEvent.FOCUS_GAINED));
        l.focusLost(new java.awt.event.FocusEvent(b, java.awt.event.FocusEvent.FOCUS_LOST));
        l.mouseClicked(new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_CLICKED, 0L, 0, 0, 0, 1, false));
        l.mousePressed(new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_PRESSED, 0L, 0, 0, 0, 1, false));
        l.mouseReleased(new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_RELEASED, 0L, 0, 0, 0, 1, false));
        l.mouseEntered(new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_ENTERED, 0L, 0, 0, 0, 0, false));
        l.mouseExited(new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_EXITED, 0L, 0, 0, 0, 0, false));
        l.mouseMoved(new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_MOVED, 0L, 0, 0, 0, 0, false));
        l.mouseDragged(new java.awt.event.MouseEvent(b, java.awt.event.MouseEvent.MOUSE_DRAGGED, 0L, 0, 0, 0, 0, false));
        l.installKeyboardActions(b);
        l.uninstallKeyboardActions(b);
        check(true, "listener methods callable");
    }
}
