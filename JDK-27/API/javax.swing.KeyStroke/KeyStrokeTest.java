/*
 * @test
 * @summary javax.swing.KeyStroke conformance: white-box structure + black-box behavior.
 * @run main KeyStrokeTest
 */

import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class KeyStrokeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.KeyStroke");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.AWTKeyStroke"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        KeyStroke a = KeyStroke.getKeyStroke("A");
        check(a != null, "string form");
        check(a.getKeyCode() == KeyEvent.VK_A, "string form maps to VK_A");
        check(a.getKeyChar() == KeyEvent.CHAR_UNDEFINED, "string form has undefined char");

        KeyStroke charStroke = KeyStroke.getKeyStroke('A');
        check(charStroke.getKeyChar() == 'A', "char form key char");
        check(charStroke.getKeyCode() == 0, "char form key code 0");

        KeyStroke vkA = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);
        check(vkA != null, "vk form");
        check(vkA.getKeyCode() == KeyEvent.VK_A, "vk key code");

        KeyStroke ctrlA = KeyStroke.getKeyStroke("ctrl A");
        check(ctrlA != null, "modifier string form");
        check((ctrlA.getModifiers() & java.awt.event.InputEvent.CTRL_DOWN_MASK) != 0, "ctrl modifier");

        check(KeyStroke.getKeyStroke("A") == a, "cache identity for same string");
        check(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0) == vkA, "cache identity for same vk");

        KeyEvent e = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, KeyEvent.VK_A, 'A');
        KeyStroke fromEvent = KeyStroke.getKeyStrokeForEvent(e);
        check(fromEvent != null, "stroke from event");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
