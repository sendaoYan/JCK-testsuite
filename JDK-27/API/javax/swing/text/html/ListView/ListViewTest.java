/*
 * @test
 * @summary javax.swing.text.html.ListView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ListViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ListView;

public class ListViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.html.ListView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.BlockView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getConstructor(Element.class) != null, "ctor(Element) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><ul><li>one</li><li>two</li></ul></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element ul = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.UL);
            check(ul != null, "UL element found");

            ListView lv = new ListView(ul);
            check(lv.getElement() == ul, "getElement round trip");
            check(lv.getStartOffset() == ul.getStartOffset(), "start offset");
            check(lv.getEndOffset() == ul.getEndOffset(), "end offset");

            // ListView overrides alignment: exactly 0.5 on both axes
            check(lv.getAlignment(View.X_AXIS) == 0.5f, "alignment X == 0.5");
            check(lv.getAlignment(View.Y_AXIS) == 0.5f, "alignment Y == 0.5");
            check(throwsIAE(() -> lv.getAlignment(7)), "alignment(7) -> IAE");

            // inherited BlockView resize weight still applies
            check(lv.getResizeWeight(View.X_AXIS) == 1, "resizeWeight X == 1");
            check(lv.getResizeWeight(View.Y_AXIS) == 0, "resizeWeight Y == 0");

            AttributeSet a1 = lv.getAttributes();
            check(a1 != null, "getAttributes non-null");
            check(lv.getAttributes() == a1, "getAttributes stable");

            for (int axis : new int[]{View.X_AXIS, View.Y_AXIS}) {
                float min = lv.getMinimumSpan(axis);
                float pref = lv.getPreferredSpan(axis);
                float max = lv.getMaximumSpan(axis);
                check(min >= 0f && pref >= 0f && max >= 0f, "spans >= 0 on axis " + axis);
                check(min <= pref && pref <= max, "min <= pref <= max on axis " + axis);
            }
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ListViewTest::testWhiteBox);
        runOnEDT(ListViewTest::testBlackBox);
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
