/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.BeepAction conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BeepActionTest
 */

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.DefaultEditorKit;

public class BeepActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$BeepAction");
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
        DefaultEditorKit.BeepAction action = new DefaultEditorKit.BeepAction();
        check(action instanceof Action, "instanceof Action");
        check(DefaultEditorKit.beepAction.equals(action.getValue(Action.NAME)), "action name is beep");

        JFrame frame = new JFrame();
        try {
            JTextArea area = new JTextArea("beep test");
            frame.add(area);
            frame.pack();
            frame.setVisible(true);

            ActionEvent evt = new ActionEvent(area, ActionEvent.ACTION_PERFORMED, DefaultEditorKit.beepAction);
            action.actionPerformed(evt);
            check("beep test".equals(area.getText()), "beep action leaves text intact");

            // null event falls back to the focused component / default toolkit beep
            action.actionPerformed(null);
            check("beep test".equals(area.getText()), "beep action with null event no-throw");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(BeepActionTest::testWhiteBox);
        runOnEDT(BeepActionTest::testBlackBox);
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
