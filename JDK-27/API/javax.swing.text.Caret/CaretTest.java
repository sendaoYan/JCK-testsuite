/*
 * @test
 * @summary javax.swing.text.Caret conformance: white-box structure + black-box behavior.
 * @run main CaretTest
 */

import java.awt.Point;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

public class CaretTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Caret");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JTextArea area = new JTextArea();
        area.setText("abcdefgh");
        Caret caret = new DefaultCaret();
        caret.install(area);

        check(caret.getDot() == 0, "dot starts 0");
        check(caret.getMark() == 0, "mark starts 0");

        caret.setDot(3);
        check(caret.getDot() == 3 && caret.getMark() == 3, "setDot moves dot and mark");
        caret.moveDot(5);
        check(caret.getDot() == 5 && caret.getMark() == 3, "moveDot leaves mark behind");
        caret.setDot(-5);
        check(caret.getDot() == 0, "setDot clamps negative to 0");
        caret.setDot(999);
        check(caret.getDot() == area.getDocument().getLength(), "setDot clamps to document length");

        check(caret.getBlinkRate() == 0, "default blink rate 0");
        caret.setBlinkRate(120);
        check(caret.getBlinkRate() == 120, "setBlinkRate/getBlinkRate roundtrip");
        caret.setBlinkRate(0);
        check(caret.getBlinkRate() == 0, "blink rate reset to 0");

        check(caret.getMagicCaretPosition() == null, "magic caret position default null");
        caret.setMagicCaretPosition(new Point(7, 9));
        Point mp = caret.getMagicCaretPosition();
        check(mp != null && mp.x == 7 && mp.y == 9, "magic caret position roundtrip");

        check(!caret.isVisible(), "caret not visible initially");
        caret.setVisible(true);
        check(caret.isVisible(), "setVisible(true)");
        caret.setVisible(false);
        check(!caret.isVisible(), "setVisible(false)");

        check(!caret.isSelectionVisible(), "selection not visible initially");
        caret.setSelectionVisible(true);
        check(caret.isSelectionVisible(), "setSelectionVisible(true)");
        caret.setSelectionVisible(false);
        check(!caret.isSelectionVisible(), "setSelectionVisible(false)");

        final int[] fired = {0};
        ChangeListener l = e -> fired[0]++;
        caret.addChangeListener(l);
        int before = fired[0];
        caret.setDot(1);
        check(fired[0] > before, "change listener fired on setDot");
        caret.removeChangeListener(l);
        int afterRemove = fired[0];
        caret.setDot(2);
        check(fired[0] == afterRemove, "change listener removed");

        caret.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
