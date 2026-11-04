/*
 * @test
 * @summary javax.swing.event.MouseInputListener conformance: white-box structure + black-box behavior.
 * @run main MouseInputListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;
import java.util.HashSet;
import java.util.Set;

public class MouseInputListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MouseInputListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.MouseListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.awt.event.MouseMotionListener"), "interface 1");
        // MouseListener (5) + MouseMotionListener (2): the 7 declared via superinterfaces
        checkMethod(c, "mouseClicked", MouseEvent.class);
        checkMethod(c, "mousePressed", MouseEvent.class);
        checkMethod(c, "mouseReleased", MouseEvent.class);
        checkMethod(c, "mouseEntered", MouseEvent.class);
        checkMethod(c, "mouseExited", MouseEvent.class);
        checkMethod(c, "mouseDragged", MouseEvent.class);
        checkMethod(c, "mouseMoved", MouseEvent.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final MouseEvent[] last = new MouseEvent[1];
        MouseInputListener l = new MouseInputListener() {
            public void mouseClicked(MouseEvent e) { fired[0] = "clicked"; last[0] = e; }
            public void mousePressed(MouseEvent e) { fired[0] = "pressed"; last[0] = e; }
            public void mouseReleased(MouseEvent e) { fired[0] = "released"; last[0] = e; }
            public void mouseEntered(MouseEvent e) { fired[0] = "entered"; last[0] = e; }
            public void mouseExited(MouseEvent e) { fired[0] = "exited"; last[0] = e; }
            public void mouseDragged(MouseEvent e) { fired[0] = "dragged"; last[0] = e; }
            public void mouseMoved(MouseEvent e) { fired[0] = "moved"; last[0] = e; }
        };

        Component src = new Container();

        MouseEvent clicked = new MouseEvent(src, MouseEvent.MOUSE_CLICKED, 100L, 0, 5, 6, 2, false, MouseEvent.BUTTON1);
        l.mouseClicked(clicked);
        check("clicked".equals(fired[0]) && last[0] == clicked, "mouseClicked fired");
        check(last[0].getX() == 5 && last[0].getY() == 6, "x/y round-trip");
        check(last[0].getClickCount() == 2, "clickCount round-trip");
        check(last[0].getButton() == MouseEvent.BUTTON1, "button round-trip");
        check(last[0].getSource() == src, "source round-trip");

        l.mousePressed(new MouseEvent(src, MouseEvent.MOUSE_PRESSED, 101L, 0, 5, 6, 1, false));
        check("pressed".equals(fired[0]), "mousePressed fired");
        l.mouseReleased(new MouseEvent(src, MouseEvent.MOUSE_RELEASED, 102L, 0, 5, 6, 1, false));
        check("released".equals(fired[0]), "mouseReleased fired");
        l.mouseEntered(new MouseEvent(src, MouseEvent.MOUSE_ENTERED, 103L, 0, 5, 6, 1, false));
        check("entered".equals(fired[0]), "mouseEntered fired");
        l.mouseExited(new MouseEvent(src, MouseEvent.MOUSE_EXITED, 104L, 0, 5, 6, 1, false));
        check("exited".equals(fired[0]), "mouseExited fired");
        l.mouseDragged(new MouseEvent(src, MouseEvent.MOUSE_DRAGGED, 105L, 0, 7, 8, 1, false));
        check("dragged".equals(fired[0]), "mouseDragged fired");
        check(last[0].getX() == 7 && last[0].getY() == 8, "dragged x/y round-trip");
        l.mouseMoved(new MouseEvent(src, MouseEvent.MOUSE_MOVED, 106L, 0, 9, 10, 1, false));
        check("moved".equals(fired[0]), "mouseMoved fired");
        check(last[0].getX() == 9 && last[0].getY() == 10, "moved x/y round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MouseInputListener", false, MouseInputListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.awt.event.MouseListener"), "implements java.awt.event.MouseListener");
        check(hasInterface(c, "java.awt.event.MouseMotionListener"), "implements java.awt.event.MouseMotionListener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
