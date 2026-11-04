/*
 * @test
 * @summary java.awt.event.KeyEvent conformance: white-box structure + black-box behavior.
 * @run main KeyEventTest
 */

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;

public class KeyEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.KeyEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "KeyEvent is public");
        check(!Modifier.isAbstract(m), "KeyEvent is concrete");
        check(c.getSuperclass() == InputEvent.class, "KeyEvent extends InputEvent");
        check(c.getField("VK_A").getType() == int.class, "VK_A int");
        check(c.getField("KEY_TYPED").getType() == int.class, "KEY_TYPED int");
        check(c.getField("CHAR_UNDEFINED").getType() == char.class, "CHAR_UNDEFINED char");
        check(c.getConstructor(Component.class, int.class, long.class, int.class, int.class, char.class, int.class) != null, "has 7-arg ctor");
        check(Modifier.isPublic(c.getMethod("getKeyText", int.class).getModifiers()), "getKeyText public");
        check(Modifier.isStatic(c.getMethod("getKeyText", int.class).getModifiers()), "getKeyText static");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        long when = 12345L;
        KeyEvent e = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, 0, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);

        check(e.getID() == KeyEvent.KEY_PRESSED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getWhen() == when, "getWhen round-trips");
        check(e.getKeyCode() == KeyEvent.VK_A, "getKeyCode round-trips");
        check(e.getKeyChar() == KeyEvent.CHAR_UNDEFINED, "getKeyChar CHAR_UNDEFINED");
        check(e.getKeyLocation() == KeyEvent.KEY_LOCATION_UNKNOWN, "default keyLocation UNKNOWN");
        check(!e.isActionKey(), "VK_A is not action key");

        // setters
        e.setKeyCode(KeyEvent.VK_B);
        check(e.getKeyCode() == KeyEvent.VK_B, "setKeyCode changes keyCode");
        e.setKeyChar('x');
        check(e.getKeyChar() == 'x', "setKeyChar changes keyChar");
        e.setModifiers(InputEvent.SHIFT_DOWN_MASK);
        check(e.isShiftDown(), "setModifiers SHIFT_DOWN_MASK sets shift down");

        // action key
        KeyEvent fn = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, 0, KeyEvent.VK_F5, KeyEvent.CHAR_UNDEFINED);
        check(fn.isActionKey(), "VK_F5 is action key");
        KeyEvent tab = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, 0, KeyEvent.VK_HOME, KeyEvent.CHAR_UNDEFINED);
        check(tab.isActionKey(), "VK_HOME is action key");

        // key-typed event: VK_UNDEFINED + real char
        KeyEvent typed = new KeyEvent(src, KeyEvent.KEY_TYPED, when, 0, KeyEvent.VK_UNDEFINED, 'a');
        check(typed.getID() == KeyEvent.KEY_TYPED, "typed id");
        check(typed.getKeyCode() == KeyEvent.VK_UNDEFINED, "typed keyCode VK_UNDEFINED");
        check(typed.getKeyChar() == 'a', "typed keyChar");

        // documented constants
        check(KeyEvent.KEY_FIRST == 400 && KeyEvent.KEY_LAST == 402, "KEY_FIRST/LAST");
        check(KeyEvent.KEY_TYPED == 400, "KEY_TYPED == 400");
        check(KeyEvent.KEY_PRESSED == 401, "KEY_PRESSED == 401");
        check(KeyEvent.KEY_RELEASED == 402, "KEY_RELEASED == 402");
        check(KeyEvent.VK_A == 65, "VK_A == 65");
        check(KeyEvent.VK_0 == 48, "VK_0 == 48");
        check(KeyEvent.CHAR_UNDEFINED == 0xFFFF, "CHAR_UNDEFINED == 0xFFFF");
        check(KeyEvent.KEY_LOCATION_UNKNOWN == 0, "KEY_LOCATION_UNKNOWN == 0");
        check(KeyEvent.KEY_LOCATION_NUMPAD == 4, "KEY_LOCATION_NUMPAD == 4");

        // getKeyText mapping
        check(KeyEvent.getKeyText(KeyEvent.VK_A).equals("A"), "getKeyText(VK_A) == A");
        check(KeyEvent.getKeyText(KeyEvent.VK_0).equals("0"), "getKeyText(VK_0) == 0");
        check(KeyEvent.getKeyText(KeyEvent.VK_UNDEFINED).endsWith("0x0"), "getKeyText(VK_UNDEFINED) ends 0x0");
        check(KeyEvent.getKeyText(KeyEvent.VK_F1).length() > 0, "getKeyText(VK_F1) non-empty");

        // getKeyModifiersText
        check(KeyEvent.getKeyModifiersText(0).equals(""), "getKeyModifiersText(0) empty");
        check(KeyEvent.getKeyModifiersText(InputEvent.SHIFT_MASK).equals("Shift"), "getKeyModifiersText(SHIFT) Shift");

        // extended key code
        check(KeyEvent.getExtendedKeyCodeForChar('A') == KeyEvent.VK_A, "extended keycode for 'A' == VK_A");
        check(KeyEvent.getExtendedKeyCodeForChar('0') == KeyEvent.VK_0, "extended keycode for '0' == VK_0");
        check(e.getExtendedKeyCode() >= 0, "getExtendedKeyCode non-negative");

        // paramString
        String ps = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, 0, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED).paramString();
        check(ps.contains("KEY_PRESSED"), "paramString has id");
        check(ps.contains("keyCode=65"), "paramString keyCode");
        check(ps.contains("keyText=A"), "paramString keyText");
        check(ps.contains("keyLocation=KEY_LOCATION_UNKNOWN"), "paramString keyLocation");

        check(e instanceof InputEvent, "KeyEvent instanceof InputEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
