/*
 * @test
 * @summary java.awt.event.InputMethodEvent conformance: white-box structure + black-box behavior.
 * @run main InputMethodEventTest
 */

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.InputMethodEvent;
import java.awt.font.TextHitInfo;
import java.lang.reflect.Modifier;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class InputMethodEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.InputMethodEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "InputMethodEvent is public");
        check(!Modifier.isAbstract(m), "InputMethodEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "InputMethodEvent extends AWTEvent");
        check(c.getField("INPUT_METHOD_TEXT_CHANGED").getType() == int.class, "INPUT_METHOD_TEXT_CHANGED int");
        check(c.getField("CARET_POSITION_CHANGED").getType() == int.class, "CARET_POSITION_CHANGED int");
        check(c.getConstructor(Component.class, int.class, long.class, AttributedCharacterIterator.class, int.class, TextHitInfo.class, TextHitInfo.class) != null, "has 7-arg ctor");
        check(c.getConstructor(Component.class, int.class, TextHitInfo.class, TextHitInfo.class) != null, "has 4-arg ctor");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        long when = 555L;
        AttributedCharacterIterator text = new AttributedString("ab").getIterator();
        TextHitInfo caret = TextHitInfo.leading(1);
        TextHitInfo vis = TextHitInfo.leading(0);

        InputMethodEvent e = new InputMethodEvent(src, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                                                 when, text, 1, caret, vis);
        check(e.getID() == InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getWhen() == when, "getWhen round-trips");
        check(e.getText() == text, "getText round-trips");
        check(e.getCommittedCharacterCount() == 1, "getCommittedCharacterCount");
        check(e.getCaret() == caret, "getCaret round-trips");
        check(e.getVisiblePosition() == vis, "getVisiblePosition round-trips");
        check(!e.isConsumed(), "fresh event not consumed");
        e.consume();
        check(e.isConsumed(), "consume() marks consumed");

        // CARET_POSITION_CHANGED must have null text
        InputMethodEvent caretEv = new InputMethodEvent(src, InputMethodEvent.CARET_POSITION_CHANGED,
                                                       when, null, 0, caret, vis);
        check(caretEv.getText() == null, "caret event text null");
        check(caretEv.getCommittedCharacterCount() == 0, "caret event count 0");

        // 4-arg constructor: null text, 0 count
        InputMethodEvent shortEv = new InputMethodEvent(src, InputMethodEvent.CARET_POSITION_CHANGED, caret, vis);
        check(shortEv.getText() == null, "4-arg ctor text null");
        check(shortEv.getWhen() >= 0, "4-arg ctor when non-negative");

        // documented constants
        check(InputMethodEvent.INPUT_METHOD_FIRST == 1100, "INPUT_METHOD_FIRST == 1100");
        check(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED == 1100, "INPUT_METHOD_TEXT_CHANGED == 1100");
        check(InputMethodEvent.CARET_POSITION_CHANGED == 1101, "CARET_POSITION_CHANGED == 1101");
        check(InputMethodEvent.INPUT_METHOD_LAST == 1101, "INPUT_METHOD_LAST == 1101");

        // validation
        boolean idOut = false, caretText = false, badCount = false;
        try { new InputMethodEvent(src, 9999, when, text, 1, caret, vis); } catch (IllegalArgumentException x) { idOut = true; }
        try { new InputMethodEvent(src, InputMethodEvent.CARET_POSITION_CHANGED, when, text, 1, caret, vis); } catch (IllegalArgumentException x) { caretText = true; }
        try { new InputMethodEvent(src, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, when, text, 99, caret, vis); } catch (IllegalArgumentException x) { badCount = true; }
        check(idOut, "out-of-range id throws IAE");
        check(caretText, "CARET_POSITION_CHANGED with text throws IAE");
        check(badCount, "out-of-range committedCharacterCount throws IAE");

        // paramString
        String ps = e.paramString();
        check(ps.contains("INPUT_METHOD_TEXT_CHANGED"), "paramString id");
        check(ps.contains("1 characters committed"), "paramString count");
        check(ps.contains("caret: "), "paramString caret");

        check(e instanceof AWTEvent, "InputMethodEvent instanceof AWTEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
