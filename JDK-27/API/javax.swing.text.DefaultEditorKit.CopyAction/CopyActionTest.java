/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.CopyAction conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CopyActionTest
 */

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

public class CopyActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$CopyAction");
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
        DefaultEditorKit.CopyAction action = new DefaultEditorKit.CopyAction();
        check(action instanceof Action, "instanceof Action");
        check(DefaultEditorKit.copyAction.equals(action.getValue(Action.NAME)), "action name is copy-to-clipboard");

        JFrame frame = new JFrame();
        try {
            JTextArea src = new JTextArea("CopyThis");
            src.select(0, 4); // "Copy"
            frame.add(src);
            frame.pack();
            frame.setVisible(true);

            ActionEvent evt = new ActionEvent(src, ActionEvent.ACTION_PERFORMED, DefaultEditorKit.copyAction);
            action.actionPerformed(evt);

            // copy leaves source text and selection intact
            check("CopyThis".equals(src.getText()), "copy leaves source text");
            check(src.getSelectionStart() == 0 && src.getSelectionEnd() == 4, "copy keeps selection");

            JTextArea dst = new JTextArea();
            dst.paste();
            check("Copy".equals(dst.getText()), "copy->paste round trip");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(CopyActionTest::testWhiteBox);
        runOnEDT(CopyActionTest::testBlackBox);
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
