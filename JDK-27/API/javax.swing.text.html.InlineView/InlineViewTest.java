/*
 * @test
 * @summary javax.swing.text.html.InlineView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main InlineViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;

public class InlineViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.InlineView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.LabelView"), "superclass");
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
            editor.setText("<html><body><p>Hello <b>boldword</b> world</p></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element content = findContent(doc.getDefaultRootElement(), "boldword");
            check(content != null, "CONTENT element 'boldword' found");

            InlineView iv = new InlineView(content);
            check(iv.getElement() == content, "getElement round trip");
            check(iv.getStartOffset() == content.getStartOffset(), "start offset");
            check(iv.getEndOffset() == content.getEndOffset(), "end offset");

            AttributeSet a1 = iv.getAttributes();
            check(a1 != null, "getAttributes non-null");
            check(iv.getAttributes() == a1, "getAttributes stable");

            // spans are deterministic-ish but >= 0 and monotonic
            for (int axis : new int[]{View.X_AXIS, View.Y_AXIS}) {
                float min = iv.getMinimumSpan(axis);
                float pref = iv.getPreferredSpan(axis);
                float max = iv.getMaximumSpan(axis);
                check(min >= 0f && pref >= 0f && max >= 0f, "spans >= 0 on axis " + axis);
                check(min <= pref && pref <= max, "min <= pref <= max on axis " + axis);
            }

            // break weight is within the documented range
            int bw = iv.getBreakWeight(View.X_AXIS, 0, 8);
            check(bw >= View.BadBreakWeight && bw <= View.ForcedBreakWeight, "breakWeight in range");

            // breakView returns a non-null fragment for a valid request
            View frag = iv.breakView(View.X_AXIS, content.getStartOffset(), 0, 4);
            check(frag != null, "breakView returns non-null fragment");

            // attributes update via changedUpdate (null args tolerated per doc)
            iv.changedUpdate(null, null, null);
            check(iv.getAttributes() != null, "getAttributes still non-null after changedUpdate");
        } finally {
            frame.dispose();
        }
    }

    static Element findContent(Element e, String text) {
        if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.CONTENT) {
            try {
                Document d = e.getDocument();
                String t = d.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset());
                if (text.equals(t)) return e;
            } catch (Exception ex) { /* ignore */ }
        }
        for (int i = 0; i < e.getElementCount(); i++) {
            Element r = findContent(e.getElement(i), text);
            if (r != null) return r;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(InlineViewTest::testWhiteBox);
        runOnEDT(InlineViewTest::testBlackBox);
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
