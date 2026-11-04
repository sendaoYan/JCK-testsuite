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
        // remaining documented constants
        check(MouseEvent.MOUSE_RELEASED == 502, "MOUSE_RELEASED == 502");
        check(MouseEvent.MOUSE_ENTERED == 504, "MOUSE_ENTERED == 504");
        check(MouseEvent.MOUSE_EXITED == 505, "MOUSE_EXITED == 505");
        check(MouseEvent.MOUSE_LAST == 507, "MOUSE_LAST == 507");
        // negative coordinates and zero click count round-trip (no constructor validation)
        MouseEvent neg = new MouseEvent(src, MouseEvent.MOUSE_MOVED, when, 0, -5, -6, -7, -8, 0, false, MouseEvent.NOBUTTON);
        check(neg.getX() == -5 && neg.getY() == -6, "negative coords round-trip");
        check(neg.getXOnScreen() == -7 && neg.getYOnScreen() == -8, "negative absolute coords round-trip");
        check(neg.getClickCount() == 0, "zero click count round-trips");
        check(neg.getButton() == MouseEvent.NOBUTTON, "NOBUTTON round-trips");
        // translatePoint works in both directions and with zeros
        neg.translatePoint(5, 6);
        check(neg.getX() == 0 && neg.getY() == 0, "translatePoint back to origin");
        neg.translatePoint(0, 0);
        check(neg.getX() == 0 && neg.getY() == 0, "translatePoint(0,0) is no-op");
        // getMouseModifiersText for buttons
        check(MouseEvent.getMouseModifiersText(InputEvent.BUTTON1_MASK).equals("Button1"), "getMouseModifiersText Button1");
        check(MouseEvent.getMouseModifiersText(InputEvent.BUTTON2_DOWN_MASK).equals("Button2"), "getMouseModifiersText Button2");
        check(MouseEvent.getMouseModifiersText(InputEvent.BUTTON3_DOWN_MASK).equals("Button3"), "getMouseModifiersText Button3");
        // old-style BUTTON2_MASK aliases ALT_MASK and renders as Alt
        check(MouseEvent.getMouseModifiersText(InputEvent.BUTTON2_MASK).equals("Alt+Button2"), "BUTTON2_MASK (==ALT_MASK bit) renders as Alt+Button2");
        // consume semantics
        check(!simple.isConsumed(), "fresh not consumed");
        simple.consume();
        check(simple.isConsumed(), "consume marks consumed");
        // toString contains paramString content
        check(e.toString().startsWith("java.awt.event.MouseEvent["), "toString starts with class name");
        check(e.toString().contains("MOUSE_PRESSED"), "toString contains id text");
        // getPoint returns a fresh mutable Point each call
        java.awt.Point p1 = e.getPoint();
        p1.x = 999;
        check(e.getX() != 999, "getPoint returns a defensive copy");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        Component src = new Component() { };
        MouseEvent e = new MouseEvent(src, MouseEvent.MOUSE_RELEASED, 654L, InputEvent.BUTTON1_DOWN_MASK, 7, 8, 9, 10, 2, true, MouseEvent.BUTTON3);
        MouseEvent back = roundTrip(e);
        check(back.getID() == MouseEvent.MOUSE_RELEASED, "serialization preserves id");
        check(back.getX() == 7 && back.getY() == 8, "serialization preserves coordinates");
        check(back.getXOnScreen() == 9 && back.getYOnScreen() == 10, "serialization preserves absolute coordinates");
        check(back.getClickCount() == 2, "serialization preserves click count");
        check(back.getButton() == MouseEvent.BUTTON3, "serialization preserves button");
        check(back.isPopupTrigger(), "serialization preserves popup trigger");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
