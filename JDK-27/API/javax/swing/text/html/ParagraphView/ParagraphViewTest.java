/*
 * @test
 * @summary javax.swing.text.html.ParagraphView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ParagraphViewTest
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
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class ParagraphViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.ParagraphView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.ParagraphView"), "superclass");
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
            editor.setText("<html><body><p>paragraph body text</p></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element p = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.P);
            check(p != null, "P element found");

            // direct construction for element/attribute accessors
            javax.swing.text.html.ParagraphView pv = new javax.swing.text.html.ParagraphView(p);
            check(pv.getElement() == p, "getElement round trip");
            check(pv.getStartOffset() == p.getStartOffset(), "start offset");
            check(pv.getEndOffset() == p.getEndOffset(), "end offset");
            AttributeSet a1 = pv.getAttributes();
            check(a1 != null, "getAttributes non-null");
            check(pv.getAttributes() == a1, "getAttributes stable");

            // a live paragraph pulled from the view tree has a layout pool
            View live = findView(editor.getUI().getRootView(editor), "javax.swing.text.html.ParagraphView");
            check(live != null, "live ParagraphView from tree");
            if (live != null) {
                check(live.isVisible(), "paragraph with content is visible");
                for (int axis : new int[]{View.X_AXIS, View.Y_AXIS}) {
                    float min = live.getMinimumSpan(axis);
                    float pref = live.getPreferredSpan(axis);
                    float max = live.getMaximumSpan(axis);
                    check(min >= 0f && pref >= 0f && max >= 0f, "spans >= 0 on axis " + axis);
                    check(min <= pref && pref <= max, "min <= pref <= max on axis " + axis);
                }
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

    static javax.swing.text.html.HTMLEditorKit deepKit = new javax.swing.text.html.HTMLEditorKit();

    static javax.swing.text.html.HTMLDocument deepLoad(String html) throws Exception {
        javax.swing.text.html.HTMLDocument d = (javax.swing.text.html.HTMLDocument) deepKit.createDefaultDocument();
        deepKit.read(new java.io.StringReader(html), d, 0);
        return d;
    }

    // ---- model layer over a fixed document (headless-safe) ----
    static void testModelLayer() throws Exception {
                javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        javax.swing.text.html.HTMLDocument doc = deepLoad("<html><body><div align='center'><p id='p'>Para one</p></div></body></html>");
        javax.swing.text.Element para = doc.getElement("p");
        check(para != null, "model: paragraph element found");
        javax.swing.text.html.ParagraphView pv = new javax.swing.text.html.ParagraphView(para);
        check(pv.getElement() == para, "model: element identity");
        check(pv.getViewCount() == 0, "model: children not loaded before parenting");
        check(pv.getAttributes().getAttributeCount() == 2, "model: two paragraph attributes");
        boolean npe = false;
        try { pv.isVisible(); } catch (NullPointerException e) { npe = true; }
        check(npe, "model: isVisible before parenting NPE");
        check(pv.getAlignment(javax.swing.text.View.X_AXIS) == 0.5f, "model: alignment X 0.5");
        check(pv.getAlignment(javax.swing.text.View.Y_AXIS) == 0.5f, "model: alignment Y 0.5");
        javax.swing.text.html.BlockView host = new javax.swing.text.html.BlockView(para.getParentElement(), javax.swing.text.View.Y_AXIS);
        pv.setParent(host);
        check(pv.getParent() == host, "model: setParent round-trip");
        check(pv.getBreakWeight(javax.swing.text.View.X_AXIS, 0f, 10f) == javax.swing.text.View.GoodBreakWeight,
              "model: break weight good for a wide request");
        check(pv.getBreakWeight(javax.swing.text.View.X_AXIS, 0f, 0f) == javax.swing.text.View.BadBreakWeight,
              "model: break weight bad for a zero-width request");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ParagraphViewTest::testWhiteBox);
        runOnEDT(ParagraphViewTest::testBlackBox);
        runOnEDT(ParagraphViewTest::testModelLayer);
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
