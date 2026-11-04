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
        // remaining documented old-style masks
        check(InputEvent.BUTTON2_MASK == InputEvent.ALT_MASK, "BUTTON2_MASK aliased to ALT_MASK");
        check(InputEvent.BUTTON3_MASK == InputEvent.META_MASK, "BUTTON3_MASK aliased to META_MASK");
        // remaining documented extended masks
        check(InputEvent.META_DOWN_MASK == 0x100, "META_DOWN_MASK == 0x100");
        check(InputEvent.ALT_DOWN_MASK == 0x200, "ALT_DOWN_MASK == 0x200");
        check(InputEvent.BUTTON1_DOWN_MASK == 0x400, "BUTTON1_DOWN_MASK == 0x400");
        check(InputEvent.BUTTON2_DOWN_MASK == 0x800, "BUTTON2_DOWN_MASK == 0x800");
        check(InputEvent.BUTTON3_DOWN_MASK == 0x1000, "BUTTON3_DOWN_MASK == 0x1000");
        // all extended masks are distinct bits
        int[] ex = { InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK, InputEvent.META_DOWN_MASK, InputEvent.ALT_DOWN_MASK,
                     InputEvent.BUTTON1_DOWN_MASK, InputEvent.BUTTON2_DOWN_MASK, InputEvent.BUTTON3_DOWN_MASK, InputEvent.ALT_GRAPH_DOWN_MASK };
        for (int i = 0; i < ex.length; i++)
            for (int j = i + 1; j < ex.length; j++)
                check((ex[i] & ex[j]) == 0, "extended masks bits distinct");
        // getModifiersExText joins with '+' and orders Meta first
        check(InputEvent.getModifiersExText(InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK).equals("Ctrl+Shift"), "getModifiersExText Ctrl+Shift order");
        check(InputEvent.getModifiersExText(InputEvent.BUTTON1_DOWN_MASK).equals("Button1"), "getModifiersExText Button1");
        // old modifiers exposed via getModifiers for extended input
        check(combo.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.ALT_MASK), "getModifiers maps extended to old masks");
        // negative modifiers text is empty for unknown bits
        check(InputEvent.getModifiersExText(1 << 20).equals("Button10"), "getModifiersExText button bit 20 is Button10");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        Component src = new Component() { };
        KeyEvent e = new KeyEvent(src, KeyEvent.KEY_PRESSED, 77L, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK, KeyEvent.VK_X, KeyEvent.CHAR_UNDEFINED);
        KeyEvent back = roundTrip(e);
        check(back.getWhen() == 77L, "serialization preserves when");
        check(back.getModifiersEx() == (InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK), "serialization preserves extended modifiers");
        check(back.isControlDown() && back.isAltDown(), "serialization keeps isControlDown/isAltDown");
        check(!back.isShiftDown(), "serialization keeps shift up");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
