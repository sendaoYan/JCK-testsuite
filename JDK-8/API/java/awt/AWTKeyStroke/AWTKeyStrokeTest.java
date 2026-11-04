/*
 * @test
 * @summary java.awt.AWTKeyStroke conformance: key char and code.
 * @run main AWTKeyStrokeTest
 */


import java.awt.AWTKeyStroke;
public class AWTKeyStrokeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        AWTKeyStroke k = AWTKeyStroke.getAWTKeyStroke('A');
        check(k.getKeyChar() == 'A', "key char");
        check(k.getKeyCode() == java.awt.event.KeyEvent.VK_UNDEFINED, "char stroke has undefined key code");
        AWTKeyStroke k2 = AWTKeyStroke.getAWTKeyStroke(java.awt.event.KeyEvent.VK_F1, 0);
        check(k2.getKeyCode() == java.awt.event.KeyEvent.VK_F1, "function key code");
        check(k2.getKeyChar() == java.awt.event.KeyEvent.CHAR_UNDEFINED, "function key undefined char");
        check(k.equals(AWTKeyStroke.getAWTKeyStroke('A')), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
