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
        // KEY_TYPED requires a defined keyChar
        boolean iaeTyped = false;
        try { new KeyEvent(src, KeyEvent.KEY_TYPED, when, 0, KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED); }
        catch (IllegalArgumentException x) { iaeTyped = true; }
        check(iaeTyped, "KEY_TYPED with CHAR_UNDEFINED throws IAE");
        // VK constant block values
        check(KeyEvent.VK_ENTER == 10, "VK_ENTER == 10");
        check(KeyEvent.VK_BACK_SPACE == 8, "VK_BACK_SPACE == 8");
        check(KeyEvent.VK_TAB == 9, "VK_TAB == 9");
        check(KeyEvent.VK_ESCAPE == 27, "VK_ESCAPE == 27");
        check(KeyEvent.VK_SPACE == 32, "VK_SPACE == 32");
        check(KeyEvent.VK_SHIFT == 16, "VK_SHIFT == 16");
        check(KeyEvent.VK_CONTROL == 17, "VK_CONTROL == 17");
        check(KeyEvent.VK_ALT == 18, "VK_ALT == 18");
        check(KeyEvent.VK_META == 157, "VK_META == 157");
        check(KeyEvent.VK_UNDEFINED == 0, "VK_UNDEFINED == 0");
        check(KeyEvent.VK_F1 == 112 && KeyEvent.VK_F12 == 123, "VK_F1..VK_F12 range");
        check(KeyEvent.VK_Z == 90 && KeyEvent.VK_A == 65, "VK_A/VK_Z values");
        check(KeyEvent.VK_0 == 48 && KeyEvent.VK_9 == 57, "VK_0..VK_9 values");
        check(KeyEvent.VK_NUMPAD0 == 96, "VK_NUMPAD0 == 96");
        // key locations
        check(KeyEvent.KEY_LOCATION_STANDARD == 1, "KEY_LOCATION_STANDARD == 1");
        check(KeyEvent.KEY_LOCATION_LEFT == 2, "KEY_LOCATION_LEFT == 2");
        check(KeyEvent.KEY_LOCATION_RIGHT == 3, "KEY_LOCATION_RIGHT == 3");
        // KEY_PRESSED events have undefined keyChar
        KeyEvent pressed = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, 0, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_STANDARD);
        check(pressed.getKeyChar() == KeyEvent.CHAR_UNDEFINED, "KEY_PRESSED keyChar undefined");
        check(!pressed.isActionKey(), "letter keys are not action keys");
        // getKeyText for well-known keys
        check(KeyEvent.getKeyText(KeyEvent.VK_ENTER).equals("Enter"), "getKeyText(VK_ENTER)");
        check(KeyEvent.getKeyText(KeyEvent.VK_SPACE).equals("Space"), "getKeyText(VK_SPACE)");
        check(KeyEvent.getKeyText(KeyEvent.VK_F1).equals("F1"), "getKeyText(VK_F1)");
        check(KeyEvent.getKeyText(0x3000).startsWith("Unknown"), "getKeyText(unknown) Unknown");
        // getKeyModifiersText joins with + and orders Meta first
        check(KeyEvent.getKeyModifiersText(InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK).equals("Ctrl+Shift"), "getKeyModifiersText Ctrl+Shift");
        check(KeyEvent.getKeyModifiersText(InputEvent.ALT_MASK | InputEvent.META_MASK).equals("Meta+Alt"), "getKeyModifiersText Meta+Alt");
        // getExtendedKeyCodeForChar returns 0 for non-ASCII characters (no real extended key on this platform)
        check(KeyEvent.getExtendedKeyCodeForChar('\u4E2D') == 0, "getExtendedKeyCodeForChar(non-ASCII) == 0");
        // paramString of KEY_TYPED shows keyCode=0 and the key char
        KeyEvent tk = new KeyEvent(src, KeyEvent.KEY_TYPED, when, 0, KeyEvent.VK_UNDEFINED, 'z');
        check(tk.paramString().contains("keyCode=0"), "KEY_TYPED paramString keyCode 0");
        check(tk.paramString().contains("keyChar='z'"), "KEY_TYPED paramString keyChar");
        // extended modifiers text in paramString
        KeyEvent md = new KeyEvent(src, KeyEvent.KEY_PRESSED, when, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED);
        check(md.paramString().contains("modifiers=Ctrl+Shift"), "paramString old modifiers");
        check(md.paramString().contains("extModifiers=Ctrl+Shift"), "paramString extended modifiers");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        Component src = new Component() { };
        KeyEvent e = new KeyEvent(src, KeyEvent.KEY_PRESSED, 321L, InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_Z, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_RIGHT);
        KeyEvent back = roundTrip(e);
        check(back.getID() == KeyEvent.KEY_PRESSED, "serialization preserves id");
        check(back.getKeyCode() == KeyEvent.VK_Z, "serialization preserves key code");
        check(back.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT, "serialization preserves key location");
        check(back.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK, "serialization preserves modifiers");
        check(back.isShiftDown(), "serialization keeps shift down");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
