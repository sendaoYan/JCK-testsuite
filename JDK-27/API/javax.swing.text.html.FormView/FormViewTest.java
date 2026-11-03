/*
 * @test
 * @summary javax.swing.text.html.FormView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FormViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class FormViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsIAE(Runnable r) {
        try { r.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.FormView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.ComponentView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.ActionListener"), "interface 0");
        check(c.getConstructor(Element.class) != null, "ctor(Element) present");
        check(c.getField("SUBMIT") != null, "SUBMIT constant");
        check(c.getField("RESET") != null, "RESET constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><form action='x'><input type='text' name='q' value='hi'></form></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element input = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.INPUT);
            check(input != null, "INPUT element found");

            FormView fv = new FormView(input);
            check("Submit Query".equals(FormView.SUBMIT), "SUBMIT constant");
            check("Reset".equals(FormView.RESET), "RESET constant");

            check(fv.getElement() == input, "getElement round trip");
            check(fv.getStartOffset() == input.getStartOffset(), "start offset");
            check(fv.getEndOffset() == input.getEndOffset(), "end offset");

            // standalone (component not yet created) spans are 0, invalid axis -> IAE
            check(fv.getPreferredSpan(View.X_AXIS) == 0f, "standalone preferred X == 0");
            check(fv.getMinimumSpan(View.X_AXIS) == 0f, "standalone minimum X == 0");
            check(fv.getMaximumSpan(View.X_AXIS) == 0f, "standalone maximum X == 0");
            check(throwsIAE(() -> fv.getMaximumSpan(3)), "getMaximumSpan(3) -> IAE");
            check(throwsIAE(() -> fv.getPreferredSpan(-1)), "getPreferredSpan(-1) -> IAE");

            // a live FormView from the view tree creates its input component
            View live = findView(editor.getUI().getRootView(editor), "javax.swing.text.html.FormView");
            check(live != null, "live FormView from tree");
            if (live instanceof FormView) {
                FormView lf = (FormView) live;
                check(lf.getComponent() instanceof JTextField, "text input -> JTextField component");
                float pref = lf.getPreferredSpan(View.X_AXIS);
                float min = lf.getMinimumSpan(View.X_AXIS);
                float max = lf.getMaximumSpan(View.X_AXIS);
                check(pref >= 0f, "live preferred span >= 0");
                check(min <= pref && pref <= max, "live min <= pref <= max");
            }
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
        runOnEDT(FormViewTest::testWhiteBox);
        runOnEDT(FormViewTest::testBlackBox);
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
