/*
 * @test
 * @summary javax.swing.event.MouseInputAdapter conformance: white-box structure + black-box behavior.
 * @run main MouseInputAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public class MouseInputAdapterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MouseInputAdapter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.MouseInputListener"), "interface 0");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Component src = new Container();
        final String[] fired = new String[1];
        MouseInputAdapter a = new MouseInputAdapter() {
            public void mouseClicked(MouseEvent e) { fired[0] = "clicked"; }
            public void mousePressed(MouseEvent e) { fired[0] = "pressed"; }
            public void mouseReleased(MouseEvent e) { fired[0] = "released"; }
            public void mouseEntered(MouseEvent e) { fired[0] = "entered"; }
            public void mouseExited(MouseEvent e) { fired[0] = "exited"; }
            public void mouseWheelMoved(MouseWheelEvent e) { fired[0] = "wheel"; }
            public void mouseDragged(MouseEvent e) { fired[0] = "dragged"; }
            public void mouseMoved(MouseEvent e) { fired[0] = "moved"; }
        };
        check(a instanceof MouseInputListener, "adapter is a MouseInputListener");

        MouseEvent clicked = new MouseEvent(src, MouseEvent.MOUSE_CLICKED, 0L, 0, 1, 2, 1, false);
        a.mouseClicked(clicked);
        check("clicked".equals(fired[0]), "mouseClicked overridable");

        MouseEvent pressed = new MouseEvent(src, MouseEvent.MOUSE_PRESSED, 0L, 0, 1, 2, 1, false);
        a.mousePressed(pressed);
        check("pressed".equals(fired[0]), "mousePressed overridable");

        MouseEvent released = new MouseEvent(src, MouseEvent.MOUSE_RELEASED, 0L, 0, 1, 2, 1, false);
        a.mouseReleased(released);
        check("released".equals(fired[0]), "mouseReleased overridable");

        MouseEvent entered = new MouseEvent(src, MouseEvent.MOUSE_ENTERED, 0L, 0, 1, 2, 1, false);
        a.mouseEntered(entered);
        check("entered".equals(fired[0]), "mouseEntered overridable");

        MouseEvent exited = new MouseEvent(src, MouseEvent.MOUSE_EXITED, 0L, 0, 1, 2, 1, false);
        a.mouseExited(exited);
        check("exited".equals(fired[0]), "mouseExited overridable");

        MouseWheelEvent wheel = new MouseWheelEvent(src, MouseEvent.MOUSE_WHEEL, 0L, 0, 1, 2, 1, false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 3);
        a.mouseWheelMoved(wheel);
        check("wheel".equals(fired[0]), "mouseWheelMoved overridable");

        MouseEvent dragged = new MouseEvent(src, MouseEvent.MOUSE_DRAGGED, 0L, 0, 1, 2, 1, false);
        a.mouseDragged(dragged);
        check("dragged".equals(fired[0]), "mouseDragged overridable");

        MouseEvent moved = new MouseEvent(src, MouseEvent.MOUSE_MOVED, 0L, 0, 1, 2, 1, false);
        a.mouseMoved(moved);
        check("moved".equals(fired[0]), "mouseMoved overridable");

        // A plain (non-overriding) adapter's no-op methods must be callable without error.
        MouseInputAdapter plain = new MouseInputAdapter() {};
        plain.mouseClicked(clicked);
        plain.mousePressed(pressed);
        plain.mouseReleased(released);
        plain.mouseEntered(entered);
        plain.mouseExited(exited);
        plain.mouseDragged(dragged);
        plain.mouseMoved(moved);
        check(true, "plain adapter no-op mouse methods callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
