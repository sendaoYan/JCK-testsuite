/*
 * @test
 * @summary javax.swing.text.html.FormView.MouseEventListener conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MouseEventListenerTest
 */

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.View;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTMLEditorKit;

public class MouseEventListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.FormView$MouseEventListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethod("mouseReleased", java.awt.event.MouseEvent.class) != null, "mouseReleased(MouseEvent) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            // An <input type=image> FormView attaches a MouseEventListener to its button.
            editor.setText("<html><body><form action='x'><input type='image' src='missing.png' name='go'></form></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            View live = findView(editor.getUI().getRootView(editor), "javax.swing.text.html.FormView");
            check(live instanceof FormView, "live FormView from tree");
            if (live instanceof FormView) {
                FormView fv = (FormView) live;
                check(fv.getComponent() instanceof JButton, "image input -> JButton component");
                JButton button = (JButton) fv.getComponent();
                boolean found = false;
                for (MouseListener ml : button.getMouseListeners()) {
                    if (Class.forName("javax.swing.text.html.FormView$MouseEventListener").isInstance(ml)) {
                        check(ml instanceof MouseAdapter, "listener is a MouseAdapter");
                        found = true;
                    }
                }
                check(found, "MouseEventListener attached to image input button");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    static void collectViews(View v, List<View> out) {
        out.add(v);
        int n = v.getViewCount();
        for (int i = 0; i < n; i++) collectViews(v.getView(i), out);
    }
    static View findView(View root, String className) {
        List<View> all = new ArrayList<View>();
        collectViews(root, all);
        for (View v : all) if (v.getClass().getName().equals(className)) return v;
        return null;
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(MouseEventListenerTest::testWhiteBox);
        runOnEDT(MouseEventListenerTest::testBlackBox);
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
