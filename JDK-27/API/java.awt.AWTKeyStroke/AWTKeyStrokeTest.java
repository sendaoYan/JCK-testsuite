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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
