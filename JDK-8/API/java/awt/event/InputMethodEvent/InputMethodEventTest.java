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
        // committedCharacterCount == text length boundary is legal
        InputMethodEvent exact = new InputMethodEvent(src, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, when, text, 2, caret, vis);
        check(exact.getCommittedCharacterCount() == 2, "count == text length legal");
        // count cannot exceed the iterator length
        boolean over = false;
        try { new InputMethodEvent(src, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, when, text, 3, caret, vis); }
        catch (IllegalArgumentException x) { over = true; }
        check(over, "count > text length throws IAE");
        // negative count rejected
        boolean neg = false;
        try { new InputMethodEvent(src, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, when, text, -1, caret, vis); }
        catch (IllegalArgumentException x) { neg = true; }
        check(neg, "negative count throws IAE");
        // 4-arg caret-only ctor also validates id range
        boolean bad4 = false;
        try { new InputMethodEvent(src, 9999, caret, vis); }
        catch (IllegalArgumentException x) { bad4 = true; }
        check(bad4, "4-arg ctor validates id range");
        // consume semantics
        check(!caretEv.isConsumed(), "caret event fresh not consumed");
        caretEv.consume();
        check(caretEv.isConsumed(), "caret event consume marks consumed");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        Component src = new Component() { };
        java.text.AttributedCharacterIterator text = new java.text.AttributedString("ab").getIterator();
        TextHitInfo caret = TextHitInfo.leading(1);
        TextHitInfo vis = TextHitInfo.leading(0);
        InputMethodEvent e = new InputMethodEvent(src, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, 111L, text, 2, caret, vis);
        InputMethodEvent back = roundTrip(e);
        check(back.getID() == InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, "serialization preserves id");
        check(back.getWhen() == 111L, "serialization preserves when");
        // caret/visiblePosition/count/text are transient: only id and when survive the round-trip
        check(back.getCommittedCharacterCount() == 0, "serialization resets transient committed count");
        check(back.getCaret() == null && back.getVisiblePosition() == null, "serialization resets transient caret/visiblePosition");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
