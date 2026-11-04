/*
 * @test
 * @summary java.awt.AWTKeyStroke conformance: white-box structure + black-box behavior.
 * @run main AWTKeyStrokeTest
 */

import java.lang.reflect.*;
import java.awt.AWTKeyStroke;
import java.awt.Canvas;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AWTKeyStrokeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.AWTKeyStroke", false, AWTKeyStrokeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.AWTKeyStroke");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && !Modifier.isAbstract(m), "public concrete class");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        for (String n : new String[] {"getAWTKeyStroke", "getAWTKeyStrokeForEvent", "getKeyChar",
                "getKeyCode", "getModifiers", "isOnKeyRelease", "getKeyEventType", "equals", "hashCode"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // typed-character factory
        AWTKeyStroke typed = AWTKeyStroke.getAWTKeyStroke('a');
        check(typed.getKeyChar() == 'a', "typed keyChar == 'a'");
        check(typed.getKeyCode() == KeyEvent.VK_UNDEFINED, "typed keyCode == VK_UNDEFINED");
        check(typed.getModifiers() == 0, "typed modifiers == 0");
        check(!typed.isOnKeyRelease(), "typed not on key release");
        check(typed.getKeyEventType() == KeyEvent.KEY_TYPED, "typed getKeyEventType == KEY_TYPED");

        // Character factory
        check(AWTKeyStroke.getAWTKeyStroke(Character.valueOf('b'), 0).getKeyChar() == 'b',
              "Character factory keyChar 'b'");
        try { AWTKeyStroke.getAWTKeyStroke((Character) null, 0); check(false, "null Character should throw"); }
        catch (IllegalArgumentException expected) { check(true, "null Character throws IllegalArgumentException"); }

        // keyCode factory (press by default)
        AWTKeyStroke press = AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_A, 0);
        check(press.getKeyCode() == KeyEvent.VK_A, "press keyCode == VK_A");
        check(press.getKeyChar() == KeyEvent.CHAR_UNDEFINED, "press keyChar == CHAR_UNDEFINED");
        check(!press.isOnKeyRelease(), "press not on key release");
        check(press.getKeyEventType() == KeyEvent.KEY_PRESSED, "press getKeyEventType == KEY_PRESSED");

        // keyCode factory with release flag
        AWTKeyStroke release = AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_A, 0, true);
        check(release.isOnKeyRelease(), "release on key release");
        check(release.getKeyEventType() == KeyEvent.KEY_RELEASED, "release getKeyEventType == KEY_RELEASED");

        // modifier bits survive (new-style modifier)
        AWTKeyStroke shifted = AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_DOWN_MASK);
        check((shifted.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0, "shift modifier preserved");
        AWTKeyStroke ctrl = AWTKeyStroke.getAWTKeyStroke("ctrl A");
        check(ctrl.getKeyCode() == KeyEvent.VK_A, "parse 'ctrl A' keyCode");
        check((ctrl.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0, "parse 'ctrl A' ctrl modifier");

        // string parser
        check(AWTKeyStroke.getAWTKeyStroke("typed a").getKeyChar() == 'a', "parse 'typed a'");
        check(AWTKeyStroke.getAWTKeyStroke("released A").isOnKeyRelease(), "parse 'released A'");
        check(AWTKeyStroke.getAWTKeyStroke("INSERT").getKeyCode() == KeyEvent.VK_INSERT, "parse 'INSERT'");
        try { AWTKeyStroke.getAWTKeyStroke((String) null); check(false, "null string should throw"); }
        catch (IllegalArgumentException expected) { check(true, "null string throws IllegalArgumentException"); }
        try { AWTKeyStroke.getAWTKeyStroke("typed ab"); check(false, "multi-char typed should throw"); }
        catch (IllegalArgumentException expected) { check(true, "multi-char typed throws IllegalArgumentException"); }
        try { AWTKeyStroke.getAWTKeyStroke("bogus nonsense"); check(false, "unknown token should throw"); }
        catch (IllegalArgumentException expected) { check(true, "unknown token throws IllegalArgumentException"); }

        // getAWTKeyStrokeForEvent
        Canvas canvas = new Canvas();
        AWTKeyStroke fromPress = AWTKeyStroke.getAWTKeyStrokeForEvent(
                new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_X, KeyEvent.CHAR_UNDEFINED));
        check(fromPress != null && fromPress.getKeyCode() == KeyEvent.VK_X, "forEvent KEY_PRESSED keyCode");
        check(!fromPress.isOnKeyRelease(), "forEvent KEY_PRESSED not release");
        AWTKeyStroke fromTyped = AWTKeyStroke.getAWTKeyStrokeForEvent(
                new KeyEvent(canvas, KeyEvent.KEY_TYPED, 0L, 0, KeyEvent.VK_UNDEFINED, 'y'));
        check(fromTyped != null && fromTyped.getKeyChar() == 'y', "forEvent KEY_TYPED keyChar");

        // equality / hashCode contract
        check(AWTKeyStroke.getAWTKeyStroke('a').equals(AWTKeyStroke.getAWTKeyStroke('a')), "equal strokes equal");
        check(AWTKeyStroke.getAWTKeyStroke('a').hashCode() == AWTKeyStroke.getAWTKeyStroke('a').hashCode(),
              "equal strokes equal hashCode");
        check(!AWTKeyStroke.getAWTKeyStroke('a').equals(AWTKeyStroke.getAWTKeyStroke('b')), "different char not equal");
        check(!press.equals(release), "press != release");
        check(!press.equals(null), "not equal null");
        check(!press.equals("not a keystroke"), "not equal unrelated type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.awt.AWTKeyStroke serKs(java.awt.AWTKeyStroke k) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(k);
        return (java.awt.AWTKeyStroke) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void testContract() throws Exception {
        // cache identity: same params -> same instance (registry)
        check(java.awt.AWTKeyStroke.getAWTKeyStroke('a')
              == java.awt.AWTKeyStroke.getAWTKeyStroke('a'), "char factory cached identity");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke('a') != java.awt.AWTKeyStroke.getAWTKeyStroke('b'),
              "different chars different instances");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_F1, 0)
              == java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_F1, 0),
              "keyCode factory cached");
        // keyChar/keyCode/onKeyRelease accessors
        java.awt.AWTKeyStroke ch = java.awt.AWTKeyStroke.getAWTKeyStroke('x');
        check(ch.getKeyChar() == 'x', "getKeyChar");
        check(ch.getKeyCode() == 0, "char-only stroke keyCode 0");
        check(!ch.isOnKeyRelease(), "char stroke defaults to press");
        java.awt.AWTKeyStroke rel = java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_A,
                java.awt.event.InputEvent.SHIFT_DOWN_MASK, true);
        check(rel.isOnKeyRelease(), "onKeyRelease true");
        check(rel.getKeyCode() == java.awt.event.KeyEvent.VK_A, "keyCode readback");
        check(rel.getModifiers() == 65,
              "getModifiers combines old+extended masks (SHIFT -> 65): " + rel.getModifiers());
        check(!java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_A, 0, false).isOnKeyRelease(),
              "press variant");
        // string parsing
        java.awt.AWTKeyStroke ctrlX = java.awt.AWTKeyStroke.getAWTKeyStroke("ctrl X");
        check(ctrlX.equals(java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
                java.awt.event.InputEvent.CTRL_DOWN_MASK)), "'ctrl X' == VK_X+CTRL");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("control X").equals(ctrlX), "'control X' alias");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("shift X").equals(
              java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
                java.awt.event.InputEvent.SHIFT_DOWN_MASK)), "'shift X'");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("alt X").equals(
              java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
                java.awt.event.InputEvent.ALT_DOWN_MASK)), "'alt X'");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("meta X").equals(
              java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
                java.awt.event.InputEvent.META_DOWN_MASK)), "'meta X'");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("ctrl shift X").equals(
              java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
                java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK)),
              "'ctrl shift X' combined");
        // modifier keywords are matched case-sensitively (lowercase only) - observed
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("alt X").equals(
              java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
                java.awt.event.InputEvent.ALT_DOWN_MASK)), "'alt X'");
        boolean altCaps = false;
        try { java.awt.AWTKeyStroke.getAWTKeyStroke("ctrl ALT X"); }
        catch (java.lang.IllegalArgumentException e) { altCaps = true; }
        check(altCaps, "uppercase 'ALT' rejected (case-sensitive): IAE");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("typed a").equals(
              java.awt.AWTKeyStroke.getAWTKeyStroke('a')), "'typed a'");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke("released X").isOnKeyRelease(), "'released X'");
        // invalid strings -> IAE
        boolean iae = false;
        try { java.awt.AWTKeyStroke.getAWTKeyStroke("bogus ZZZ"); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "unknown token -> IAE");
        iae = false;
        try { java.awt.AWTKeyStroke.getAWTKeyStroke(""); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "empty string -> IAE");
        iae = false;
        try { java.awt.AWTKeyStroke.getAWTKeyStroke((String) null); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "null string -> IAE (documented)");
        // out-of-range keyCode is not validated (maps to UNKNOWN keyCode stroke) - observed
        check(java.awt.AWTKeyStroke.getAWTKeyStroke(-1, 0).getKeyCode() == -1,
              "negative keyCode accepted, readback -1");
        check(java.awt.AWTKeyStroke.getAWTKeyStroke(900, 0).toString().contains("UNKNOWN")
              || java.awt.AWTKeyStroke.getAWTKeyStroke(900, 0).getKeyCode() == 900,
              "undefined keyCode accepted: " + java.awt.AWTKeyStroke.getAWTKeyStroke(900, 0));
        // getAWTKeyStrokeForEvent
        java.awt.event.KeyEvent pressed = new java.awt.event.KeyEvent(new java.awt.Canvas(),
                java.awt.event.KeyEvent.KEY_PRESSED, 0L,
                java.awt.event.InputEvent.CTRL_DOWN_MASK, java.awt.event.KeyEvent.VK_X, 'x');
        java.awt.AWTKeyStroke fromEv = java.awt.AWTKeyStroke.getAWTKeyStrokeForEvent(pressed);
        check(fromEv.equals(ctrlX), "getAWTKeyStrokeForEvent KEY_PRESSED == 'ctrl X'");
        java.awt.event.KeyEvent typed = new java.awt.event.KeyEvent(new java.awt.Canvas(),
                java.awt.event.KeyEvent.KEY_TYPED, 0L, 0, 0, 'q');
        check(java.awt.AWTKeyStroke.getAWTKeyStrokeForEvent(typed).equals(java.awt.AWTKeyStroke.getAWTKeyStroke('q')),
              "getAWTKeyStrokeForEvent KEY_TYPED -> char stroke");
        java.awt.event.KeyEvent released = new java.awt.event.KeyEvent(new java.awt.Canvas(),
                java.awt.event.KeyEvent.KEY_RELEASED, 0L, 0, java.awt.event.KeyEvent.VK_Y, 'y');
        check(java.awt.AWTKeyStroke.getAWTKeyStrokeForEvent(released).isOnKeyRelease(),
              "getAWTKeyStrokeForEvent KEY_RELEASED -> onKeyRelease");
        // equals/hashCode
        check(ctrlX.equals(java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
              java.awt.event.InputEvent.CTRL_DOWN_MASK)), "equals same spec");
        check(ctrlX.hashCode() == java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
              java.awt.event.InputEvent.CTRL_DOWN_MASK).hashCode(), "hashCode consistent");
        check(!ctrlX.equals(java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_X,
              java.awt.event.InputEvent.SHIFT_DOWN_MASK)), "different modifiers not equal");
        check(!ctrlX.equals(java.awt.AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_Y,
              java.awt.event.InputEvent.CTRL_DOWN_MASK)), "different keyCode not equal");
        check(!ctrlX.equals(null), "equals(null)");
        check(!ctrlX.equals("ctrl X"), "equals unrelated");
        // toString human readable
        check(ctrlX.toString().equals("ctrl pressed X"), "toString 'ctrl pressed X': " + ctrlX);
        check(java.awt.AWTKeyStroke.getAWTKeyStroke('z').toString().equals("typed z"), "toString 'typed z'");
        // serialization round-trip preserves all four fields
        java.awt.AWTKeyStroke rt = serKs(rel);
        check(rt.equals(rel), "serialization round-trip equal");
        check(rt.isOnKeyRelease(), "serialization preserves onKeyRelease");
        check(rt.getModifiers() == rel.getModifiers(), "serialization preserves modifiers");
        // protected ctor accessible to subclasses; subclass registers its own prototype
        java.awt.AWTKeyStroke sub = new java.awt.AWTKeyStroke() { };
        check(sub != null, "protected no-arg ctor subclassable");
        check(java.io.Serializable.class.isAssignableFrom(java.awt.AWTKeyStroke.class), "Serializable");
    }

}
