/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.CutAction conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CutActionTest
 */

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

public class CutActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$CutAction");
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
        DefaultEditorKit.CutAction action = new DefaultEditorKit.CutAction();
        check(action instanceof Action, "instanceof Action");
        check(DefaultEditorKit.cutAction.equals(action.getValue(Action.NAME)), "action name is cut-to-clipboard");

        JFrame frame = new JFrame();
        try {
            JTextArea src = new JTextArea("CutThis");
            src.select(0, 3); // "Cut"
            frame.add(src);
            frame.pack();
            frame.setVisible(true);

            ActionEvent evt = new ActionEvent(src, ActionEvent.ACTION_PERFORMED, DefaultEditorKit.cutAction);
            action.actionPerformed(evt);

            // cut removes the selection from the source
            check("This".equals(src.getText()), "cut removes selection");

            JTextArea dst = new JTextArea();
            dst.paste();
            check("Cut".equals(dst.getText()), "cut->paste round trip");

            // cut on non-editable component is a no-op
            JTextArea ro = new JTextArea("Keep");
            ro.setEditable(false);
            ro.select(0, 1);
            ActionEvent roEvt = new ActionEvent(ro, ActionEvent.ACTION_PERFORMED, DefaultEditorKit.cutAction);
            action.actionPerformed(roEvt);
            check("Keep".equals(ro.getText()), "cut no-op when non-editable");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(CutActionTest::testWhiteBox);
        runOnEDT(CutActionTest::testBlackBox);
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
