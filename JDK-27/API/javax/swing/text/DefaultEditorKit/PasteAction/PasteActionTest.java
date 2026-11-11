/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.PasteAction conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm PasteActionTest
 */

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

public class PasteActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$PasteAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.TextAction"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getConstructor() != null, "no-arg ctor present");
        check(c.getMethod("actionPerformed", ActionEvent.class) != null, "actionPerformed present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultEditorKit.PasteAction action = new DefaultEditorKit.PasteAction();
        check(action instanceof Action, "instanceof Action");
        check(DefaultEditorKit.pasteAction.equals(action.getValue(Action.NAME)), "action name is paste-from-clipboard");

        JFrame frame = new JFrame();
        try {
            JTextArea area = new JTextArea("Before");
            frame.add(area);
            frame.pack();
            frame.setVisible(true);

            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection("Pasted"), null);

            area.setCaretPosition(0);
            ActionEvent evt = new ActionEvent(area, ActionEvent.ACTION_PERFORMED, DefaultEditorKit.pasteAction);
            action.actionPerformed(evt);
            check("PastedBefore".equals(area.getText()), "paste inserts at caret");

            // paste replacing a selection
            area.setText("ABCDEF");
            area.select(1, 4); // replace "BCD"
            cb.setContents(new StringSelection("X"), null);
            action.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, DefaultEditorKit.pasteAction));
            check("AXEF".equals(area.getText()), "paste replaces selection");

            // paste is a no-op on non-editable component
            JTextArea ro = new JTextArea("Keep");
            ro.setEditable(false);
            cb.setContents(new StringSelection("Nope"), null);
            action.actionPerformed(new ActionEvent(ro, ActionEvent.ACTION_PERFORMED, DefaultEditorKit.pasteAction));
            check("Keep".equals(ro.getText()), "paste no-op when non-editable");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(PasteActionTest::testWhiteBox);
        runOnEDT(PasteActionTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
