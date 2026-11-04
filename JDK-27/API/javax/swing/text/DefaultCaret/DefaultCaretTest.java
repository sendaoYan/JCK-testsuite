/*
 * @test
 * @summary javax.swing.text.DefaultCaret conformance: white-box structure + black-box behavior.
 * @run main DefaultCaretTest
 */

import java.awt.Point;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Position;

public class DefaultCaretTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultCaret");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Rectangle"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Caret"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.awt.event.FocusListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.awt.event.MouseListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("java.awt.event.MouseMotionListener"), "interface 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // update policy (no component needed)
        DefaultCaret c = new DefaultCaret();
        check(c.getUpdatePolicy() == DefaultCaret.UPDATE_WHEN_ON_EDT, "default update policy EDT");
        c.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        check(c.getUpdatePolicy() == DefaultCaret.NEVER_UPDATE, "set NEVER_UPDATE");
        c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        check(c.getUpdatePolicy() == DefaultCaret.ALWAYS_UPDATE, "set ALWAYS_UPDATE");
        c.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
        check(c.getUpdatePolicy() == DefaultCaret.UPDATE_WHEN_ON_EDT, "set UPDATE_WHEN_ON_EDT");

        // blink rate on an uninstalled caret (JDK-8301989 regression)
        check(c.getBlinkRate() == 0, "blink rate default 0");
        c.setBlinkRate(100);
        check(c.getBlinkRate() == 100, "uninstalled setBlinkRate saved");
        c.setBlinkRate(0);
        check(c.getBlinkRate() == 0, "blink rate reset");
        boolean iae = false;
        try { c.setBlinkRate(-1); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "negative blink rate throws IllegalArgumentException");

        // equals uses identity, not Rectangle equality
        check(c.equals(c), "equals self");
        check(!c.equals(new DefaultCaret()), "not equals other instance");

        // visibility / active on an uninstalled caret (JDK-7083457)
        DefaultCaret un = new DefaultCaret();
        un.setVisible(true);
        check(un.isActive(), "isActive after setVisible(true)");
        un.setVisible(false);
        check(!un.isActive(), "isActive after setVisible(false)");

        JTextArea area = new JTextArea();
        area.setText("abcdefgh");
        c.install(area);
        check(c.getDot() == 0 && c.getMark() == 0, "install resets dot and mark");
        check(c.getDotBias() == Position.Bias.Forward, "dot bias forward after install");
        check(c.getMarkBias() == Position.Bias.Forward, "mark bias forward after install");

        c.setDot(4);
        check(c.getDot() == 4 && c.getMark() == 4, "setDot moves dot and mark");
        c.moveDot(6);
        check(c.getDot() == 6 && c.getMark() == 4, "moveDot leaves mark");

        c.setDot(2, Position.Bias.Backward);
        check(c.getDot() == 2 && c.getDotBias() == Position.Bias.Backward, "setDot with Backward bias");
        check(c.getMarkBias() == Position.Bias.Backward, "mark bias follows dot bias");
        c.setDot(5, Position.Bias.Forward);
        check(c.getDotBias() == Position.Bias.Forward, "setDot with Forward bias");

        c.moveDot(7, Position.Bias.Backward);
        check(c.getDot() == 7 && c.getDotBias() == Position.Bias.Backward, "moveDot with bias");
        check(c.getMark() == 5, "moveDot bias leaves mark");

        boolean nullBias = false;
        try { c.setDot(1, null); } catch (IllegalArgumentException e) { nullBias = true; }
        check(nullBias, "null bias throws IllegalArgumentException");

        c.setDot(-3);
        check(c.getDot() == 0, "setDot clamps negative");
        c.setDot(1000);
        check(c.getDot() == area.getDocument().getLength(), "setDot clamps high");

        check(c.toString().contains("Dot=") && c.toString().contains("Mark="), "toString has Dot/Mark");

        c.setMagicCaretPosition(new Point(3, 4));
        check(new Point(3, 4).equals(c.getMagicCaretPosition()), "magic caret position roundtrip");

        c.setVisible(true);
        check(c.isVisible(), "installed setVisible(true)");
        c.setVisible(false);
        check(!c.isVisible(), "installed setVisible(false)");

        check(!c.isSelectionVisible(), "selection visibility default false");
        c.setSelectionVisible(true);
        check(c.isSelectionVisible(), "selection visibility true");
        c.setSelectionVisible(false);
        check(!c.isSelectionVisible(), "selection visibility false");

        c.setBlinkRate(250);
        check(c.getBlinkRate() == 250, "installed blink rate roundtrip");
        c.setBlinkRate(0);
        check(c.getBlinkRate() == 0, "installed blink rate reset");

        c.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
