/*
 * @test
 * @summary javax.swing.KeyStroke conformance: keystroke factories (headless-safe).
 * @run main KeyStrokeTest
 */

import javax.swing.KeyStroke;

public class KeyStrokeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        KeyStroke a = KeyStroke.getKeyStroke('a');
        check(a != null && a.getKeyChar() == 'a', "getKeyStroke char");
        KeyStroke enter = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
        check(enter != null && enter.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER, "getKeyStroke code");
        check(!a.equals(enter), "distinct");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
