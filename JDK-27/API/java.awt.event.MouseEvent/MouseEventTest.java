/*
 * @test
 * @summary java.awt.event.MouseEvent conformance: white-box structure + black-box behavior.
 * @run main MouseEventTest
 */

import java.awt.Component;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;

public class MouseEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.MouseEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "MouseEvent is public");
        check(!Modifier.isAbstract(m), "MouseEvent is concrete");
        check(c.getSuperclass() == InputEvent.class, "MouseEvent extends InputEvent");
        check(c.getField("MOUSE_CLICKED").getType() == int.class, "MOUSE_CLICKED int");
        check(c.getField("BUTTON1").getType() == int.class, "BUTTON1 int");
        check(c.getField("NOBUTTON").getType() == int.class, "NOBUTTON int");
        check(c.getConstructor(Component.class, int.class, long.class, int.class, int.class, int.class, int.class, boolean.class, int.class) != null, "has 9-arg ctor");
        check(c.getConstructor(Component.class, int.class, long.class, int.class, int.class, int.class, int.class, int.class, int.class, boolean.class, int.class) != null, "has 11-arg ctor");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        long when = 700L;
        MouseEvent e = new MouseEvent(src, MouseEvent.MOUSE_PRESSED, when, 0,
                                      10, 20, 30, 40, 2, true, MouseEvent.BUTTON1);

        check(e.getID() == MouseEvent.MOUSE_PRESSED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getWhen() == when, "getWhen round-trips");
        check(e.getX() == 10, "getX");
        check(e.getY() == 20, "getY");
        check(e.getPoint().equals(new Point(10, 20)), "getPoint");
        check(e.getXOnScreen() == 30, "getXOnScreen");
        check(e.getYOnScreen() == 40, "getYOnScreen");
        check(e.getLocationOnScreen().equals(new Point(30, 40)), "getLocationOnScreen");
        check(e.getClickCount() == 2, "getClickCount");
        check(e.getButton() == MouseEvent.BUTTON1, "getButton");
        check(e.isPopupTrigger(), "isPopupTrigger true");

        // translatePoint shifts relative coords only
        e.translatePoint(5, -3);
        check(e.getX() == 15 && e.getY() == 17, "translatePoint shifts x,y");
        check(e.getXOnScreen() == 30 && e.getYOnScreen() == 40, "translatePoint leaves abs coords");

        // 8-arg ctor defaults button to NOBUTTON
        MouseEvent simple = new MouseEvent(src, MouseEvent.MOUSE_CLICKED, when, 0, 1, 2, 1, false);
        check(simple.getButton() == MouseEvent.NOBUTTON, "8-arg ctor button NOBUTTON");
        check(simple.getClickCount() == 1, "8-arg ctor clickCount");
        check(!simple.isPopupTrigger(), "8-arg ctor popup false");

        // documented constants
        check(MouseEvent.MOUSE_FIRST == 500 && MouseEvent.MOUSE_LAST == 507, "MOUSE_FIRST/LAST");
        check(MouseEvent.MOUSE_CLICKED == 500, "MOUSE_CLICKED == 500");
        check(MouseEvent.MOUSE_PRESSED == 501, "MOUSE_PRESSED == 501");
        check(MouseEvent.MOUSE_RELEASED == 502, "MOUSE_RELEASED == 502");
        check(MouseEvent.MOUSE_MOVED == 503, "MOUSE_MOVED == 503");
        check(MouseEvent.MOUSE_DRAGGED == 506, "MOUSE_DRAGGED == 506");
        check(MouseEvent.MOUSE_WHEEL == 507, "MOUSE_WHEEL == 507");
        check(MouseEvent.NOBUTTON == 0, "NOBUTTON == 0");
        check(MouseEvent.BUTTON1 == 1, "BUTTON1 == 1");
        check(MouseEvent.BUTTON2 == 2, "BUTTON2 == 2");
        check(MouseEvent.BUTTON3 == 3, "BUTTON3 == 3");

        // getModifiersEx strips old-style bits
        MouseEvent withMod = new MouseEvent(src, MouseEvent.MOUSE_PRESSED, when,
                                            InputEvent.BUTTON1_DOWN_MASK, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON1);
        check(withMod.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK, "getModifiersEx BUTTON1_DOWN_MASK");

        // getMouseModifiersText
        check(MouseEvent.getMouseModifiersText(0).equals(""), "getMouseModifiersText(0) empty");
        check(MouseEvent.getMouseModifiersText(InputEvent.SHIFT_MASK).length() > 0, "getMouseModifiersText(SHIFT) non-empty");

        // paramString
        MouseEvent ps = new MouseEvent(src, MouseEvent.MOUSE_PRESSED, when, 0, 10, 20, 30, 40, 1, false, MouseEvent.BUTTON1);
        check(ps.paramString().contains("MOUSE_PRESSED"), "paramString id");
        check(ps.paramString().contains("(10,20)"), "paramString coords");
        check(ps.paramString().contains("absolute(30,40)"), "paramString abs coords");
        check(ps.paramString().contains("button=1"), "paramString button");
        check(ps.paramString().contains("clickCount=1"), "paramString clickCount");

        check(e instanceof InputEvent, "MouseEvent instanceof InputEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
