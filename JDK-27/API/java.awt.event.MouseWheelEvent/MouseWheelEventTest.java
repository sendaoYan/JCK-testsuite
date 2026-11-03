/*
 * @test
 * @summary java.awt.event.MouseWheelEvent conformance: white-box structure + black-box behavior.
 * @run main MouseWheelEventTest
 */

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Modifier;

public class MouseWheelEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.MouseWheelEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "MouseWheelEvent is public");
        check(!Modifier.isAbstract(m), "MouseWheelEvent is concrete");
        check(c.getSuperclass() == MouseEvent.class, "MouseWheelEvent extends MouseEvent");
        check(c.getField("WHEEL_UNIT_SCROLL").getType() == int.class, "WHEEL_UNIT_SCROLL int");
        check(c.getField("WHEEL_BLOCK_SCROLL").getType() == int.class, "WHEEL_BLOCK_SCROLL int");
        check(c.getConstructor(Component.class, int.class, long.class, int.class, int.class, int.class, int.class, boolean.class, int.class, int.class, int.class) != null, "has 11-arg ctor");
        check(c.getConstructor(Component.class, int.class, long.class, int.class, int.class, int.class, int.class, int.class, int.class, boolean.class, int.class, int.class, int.class, double.class) != null, "has 14-arg ctor");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        long when = 900L;
        MouseWheelEvent e = new MouseWheelEvent(src, MouseEvent.MOUSE_WHEEL, when, 0,
                                                5, 6, 3, 3, 1, false,
                                                MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, -2);

        check(e.getID() == MouseEvent.MOUSE_WHEEL, "getID MOUSE_WHEEL");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getWhen() == when, "getWhen round-trips");
        check(e.getX() == 5 && e.getY() == 6, "mouse coords round-trip");
        check(e.getClickCount() == 1, "clickCount");
        check(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL, "getScrollType");
        check(e.getScrollAmount() == 3, "getScrollAmount");
        check(e.getWheelRotation() == -2, "getWheelRotation");
        check(e.getPreciseWheelRotation() == -2.0, "getPreciseWheelRotation defaults to wheelRotation");
        check(e.getUnitsToScroll() == -6, "getUnitsToScroll == scrollAmount * wheelRotation");
        check(e.getButton() == MouseEvent.NOBUTTON, "button defaults NOBUTTON");

        // 14-arg constructor with explicit precise rotation
        MouseWheelEvent precise = new MouseWheelEvent(src, MouseEvent.MOUSE_WHEEL, when, 0,
                                                     0, 0, 0, 0, 0, false,
                                                     MouseWheelEvent.WHEEL_BLOCK_SCROLL, 1, 2, 2.5);
        check(precise.getPreciseWheelRotation() == 2.5, "explicit preciseWheelRotation");
        check(precise.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL, "block scroll type");
        check(precise.getUnitsToScroll() == 2, "block unitsToScroll == scrollAmount * wheelRotation");

        // documented constants
        check(MouseWheelEvent.WHEEL_UNIT_SCROLL == 0, "WHEEL_UNIT_SCROLL == 0");
        check(MouseWheelEvent.WHEEL_BLOCK_SCROLL == 1, "WHEEL_BLOCK_SCROLL == 1");

        // paramString
        String ps = e.paramString();
        check(ps.contains("MOUSE_WHEEL"), "paramString id");
        check(ps.contains("scrollType=WHEEL_UNIT_SCROLL"), "paramString scrollType");
        check(ps.contains("scrollAmount=3"), "paramString scrollAmount");
        check(ps.contains("wheelRotation=-2"), "paramString wheelRotation");

        check(e instanceof MouseEvent, "MouseWheelEvent instanceof MouseEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
