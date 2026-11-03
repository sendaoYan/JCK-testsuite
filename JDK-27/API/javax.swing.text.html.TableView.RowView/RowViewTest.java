/*
 * @test
 * @summary javax.swing.text.html.TableView.RowView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main RowViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.AttributeSet;
import javax.swing.text.View;
import javax.swing.text.html.HTMLEditorKit;

public class RowViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.TableView$RowView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><table border='1'><tr><td>a</td><td>b</td></tr><tr><td>c</td><td>d</td></tr></table></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            Class<?> rowViewClass = Class.forName("javax.swing.text.html.TableView$RowView");
            List<View> rows = new ArrayList<View>();
            collectViews(editor.getUI().getRootView(editor), rows);
            List<View> rowViews = new ArrayList<View>();
            for (View v : rows) if (rowViewClass.isInstance(v)) rowViews.add(v);
            check(rowViews.size() == 2, "two RowViews for two <tr> rows");

            View rv = rowViews.get(0);
            check(rv.getStartOffset() <= rv.getEndOffset(), "row offsets ordered");
            check(rv.getResizeWeight(View.X_AXIS) == 1, "resizeWeight X == 1");
            check(rv.getResizeWeight(View.Y_AXIS) == 1, "resizeWeight Y == 1");

            AttributeSet a1 = rv.getAttributes();
            check(a1 != null, "getAttributes non-null");
            check(rv.getAttributes() == a1, "getAttributes stable");

            for (int axis : new int[]{View.X_AXIS, View.Y_AXIS}) {
                float min = rv.getMinimumSpan(axis);
                float pref = rv.getPreferredSpan(axis);
                float max = rv.getMaximumSpan(axis);
                check(min >= 0f && pref >= 0f && max >= 0f, "spans >= 0 on axis " + axis);
                check(min <= pref && pref <= max, "min <= pref <= max on axis " + axis);
            }
            // RowView is infinitely flexible along the major axis (X)
            check(rv.getMaximumSpan(View.X_AXIS) >= rv.getPreferredSpan(View.X_AXIS), "max >= pref on X");
        } finally {
            frame.dispose();
        }
    }

    static void collectViews(View v, List<View> out) {
        out.add(v);
        int n = v.getViewCount();
        for (int i = 0; i < n; i++) collectViews(v.getView(i), out);
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(RowViewTest::testWhiteBox);
        runOnEDT(RowViewTest::testBlackBox);
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
