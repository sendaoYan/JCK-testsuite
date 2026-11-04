/*
 * @test
 * @summary java.awt.event.InputEvent conformance: white-box structure + black-box behavior.
 * @run main InputEventTest
 */

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;

public class InputEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.InputEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "InputEvent is public");
        check(Modifier.isAbstract(m), "InputEvent is abstract");
        check(c.getSuperclass() == ComponentEvent.class, "InputEvent extends ComponentEvent");
        check(Modifier.isPublic(c.getField("SHIFT_DOWN_MASK").getModifiers()), "SHIFT_DOWN_MASK public");
        check(c.getField("ALT_GRAPH_DOWN_MASK").getType() == int.class, "ALT_GRAPH_DOWN_MASK int");
        check(Modifier.isPublic(c.getMethod("isShiftDown").getModifiers()), "isShiftDown public");
        check(Modifier.isPublic(c.getMethod("getModifiersEx").getModifiers()), "getModifiersEx public");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        long when = 500L;

        // Use a concrete KeyEvent to exercise abstract InputEvent behavior.
        KeyEvent none = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, 0, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);
        check(none.getWhen() == when, "getWhen round-trips");
        check(none.getModifiers() == 0, "getModifiers 0");
        check(none.getModifiersEx() == 0, "getModifiersEx 0");
        check(!none.isShiftDown(), "isShiftDown false");
        check(!none.isControlDown(), "isControlDown false");
        check(!none.isMetaDown(), "isMetaDown false");
        check(!none.isAltDown(), "isAltDown false");
        check(!none.isAltGraphDown(), "isAltGraphDown false");
        check(!none.isConsumed(), "fresh InputEvent not consumed");
        none.consume();
        check(none.isConsumed(), "consume() marks consumed");

        KeyEvent shift = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, InputEvent.SHIFT_DOWN_MASK,
                                      KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);
        check(shift.isShiftDown(), "isShiftDown true for SHIFT_DOWN_MASK");
        check(!shift.isControlDown(), "isControlDown false");
        check(shift.getModifiers() == InputEvent.SHIFT_MASK, "getModifiers returns old SHIFT_MASK");
        check(shift.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK, "getModifiersEx returns SHIFT_DOWN_MASK");

        KeyEvent combo = new KeyEvent(src, KeyEvent.KEY_PRESSED, when,
                                      InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK,
                                      KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);
        check(combo.isControlDown() && combo.isAltDown(), "ctrl+alt both down");
        check(!combo.isShiftDown(), "shift not down");

        // documented mask constants
        check(InputEvent.SHIFT_MASK == 1, "SHIFT_MASK == 1");
        check(InputEvent.CTRL_MASK == 2, "CTRL_MASK == 2");
        check(InputEvent.META_MASK == 4, "META_MASK == 4");
        check(InputEvent.ALT_MASK == 8, "ALT_MASK == 8");
        check(InputEvent.BUTTON1_MASK == 16, "BUTTON1_MASK == 16");
        check(InputEvent.ALT_GRAPH_MASK == 32, "ALT_GRAPH_MASK == 32");
        check(InputEvent.SHIFT_DOWN_MASK == 0x40, "SHIFT_DOWN_MASK == 0x40");
        check(InputEvent.CTRL_DOWN_MASK == 0x80, "CTRL_DOWN_MASK == 0x80");
        check(InputEvent.ALT_GRAPH_DOWN_MASK == 0x2000, "ALT_GRAPH_DOWN_MASK == 0x2000");

        // getMaskForButton
        check(InputEvent.getMaskForButton(1) == InputEvent.BUTTON1_DOWN_MASK, "mask button1");
        check(InputEvent.getMaskForButton(2) == InputEvent.BUTTON2_DOWN_MASK, "mask button2");
        check(InputEvent.getMaskForButton(3) == InputEvent.BUTTON3_DOWN_MASK, "mask button3");
        boolean iae0 = false, iaeBig = false;
        try { InputEvent.getMaskForButton(0); } catch (IllegalArgumentException x) { iae0 = true; }
        try { InputEvent.getMaskForButton(1000); } catch (IllegalArgumentException x) { iaeBig = true; }
        check(iae0, "getMaskForButton(0) throws IAE");
        check(iaeBig, "getMaskForButton(1000) throws IAE");

        // getModifiersExText
        check(InputEvent.getModifiersExText(0).equals(""), "getModifiersExText(0) empty");
        check(InputEvent.getModifiersExText(InputEvent.SHIFT_DOWN_MASK).length() > 0, "getModifiersExText non-empty");

        check(none instanceof ComponentEvent, "InputEvent instanceof ComponentEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
