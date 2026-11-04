/*
 * @test
 * @summary javax.swing.text.html.BlockView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BlockViewTest
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
import javax.swing.text.html.BlockView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class BlockViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.html.BlockView");
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
        check(c.getConstructor(Element.class, int.class) != null, "ctor(Element,int) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><div id='d'>block text</div><p>para</p></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element div = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.DIV);
            check(div != null, "DIV element found");

            // direct construction over a DIV element
            BlockView bv = new BlockView(div, View.Y_AXIS);
            check(bv.getElement() == div, "getElement round trip");
            check(bv.getStartOffset() == div.getStartOffset(), "start offset");
            check(bv.getEndOffset() == div.getEndOffset(), "end offset");
            check(bv.getStartOffset() <= bv.getEndOffset(), "offsets ordered");

            // axis-specific resize weight (exact, documented)
            check(bv.getResizeWeight(View.X_AXIS) == 1, "resizeWeight X == 1");
            check(bv.getResizeWeight(View.Y_AXIS) == 0, "resizeWeight Y == 0");
            check(throwsIAE(() -> bv.getResizeWeight(5)), "resizeWeight(5) -> IAE");
            check(throwsIAE(() -> bv.getResizeWeight(-1)), "resizeWeight(-1) -> IAE");

            // alignment: X is exactly 0, Y within [0,1], invalid axis -> IAE
            check(bv.getAlignment(View.X_AXIS) == 0.0f, "alignment X == 0");
            float ay = bv.getAlignment(View.Y_AXIS);
            check(ay >= 0.0f && ay <= 1.0f, "alignment Y within [0,1]");
            check(throwsIAE(() -> bv.getAlignment(3)), "alignment(3) -> IAE");

            // attributes from the stylesheet are non-null and stable
            AttributeSet a1 = bv.getAttributes();
            check(a1 != null, "getAttributes non-null");
            check(bv.getAttributes() == a1, "getAttributes stable instance");

            // span monotonicity on a live BlockView pulled from the view tree
            View live = findView(editor.getUI().getRootView(editor), "javax.swing.text.html.BlockView");
            check(live != null && live instanceof BlockView, "live BlockView from tree");
            if (live != null) {
                for (int axis : new int[]{View.X_AXIS, View.Y_AXIS}) {
                    float min = live.getMinimumSpan(axis);
                    float pref = live.getPreferredSpan(axis);
                    float max = live.getMaximumSpan(axis);
                    check(min >= 0f && pref >= 0f && max >= 0f, "spans >= 0 on axis " + axis);
                    check(min <= pref && pref <= max, "min <= pref <= max on axis " + axis);
                }
                check(live.getAttributes() != null, "live getAttributes non-null");
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
        javax.swing.text.html.HTMLDocument doc = deepLoad("<html><body><div id='d' align='center'><p>Para one</p></div><p>Para two</p></body></html>");
        javax.swing.text.Element div = doc.getElement("d");
        check(div != null, "model: div element found");
        javax.swing.text.html.BlockView bv = new javax.swing.text.html.BlockView(div, javax.swing.text.View.Y_AXIS);
        check(bv.getElement() == div, "model: element identity");
        check(bv.getAxis() == javax.swing.text.View.Y_AXIS, "model: Y_AXIS stored");
        javax.swing.text.html.BlockView bx = new javax.swing.text.html.BlockView(div, javax.swing.text.View.X_AXIS);
        check(bx.getAxis() == javax.swing.text.View.X_AXIS, "model: X_AXIS stored");
        check(bv.getResizeWeight(javax.swing.text.View.X_AXIS) == 1, "model: resizeWeight X == 1");
        check(bv.getResizeWeight(javax.swing.text.View.Y_AXIS) == 0, "model: resizeWeight Y == 0");
        javax.swing.text.AttributeSet attrs = bv.getAttributes();
        check(attrs.getAttributeCount() == 1, "model: one attribute for align=center");
        check(attrs.isDefined(javax.swing.text.html.CSS.Attribute.TEXT_ALIGN), "model: text-align defined");
        check(String.valueOf(attrs.getAttribute(javax.swing.text.html.CSS.Attribute.TEXT_ALIGN)).contains("center"),
              "model: text-align value center");
        bv.setSize(100f, 100f);
        check(bv.getPreferredSpan(javax.swing.text.View.X_AXIS) == 0.0f, "model: preferred X zero before layout");
        check(bv.getPreferredSpan(javax.swing.text.View.Y_AXIS) == 0.0f, "model: preferred Y zero before layout");
        check(bv.getMinimumSpan(javax.swing.text.View.Y_AXIS) <= bv.getPreferredSpan(javax.swing.text.View.Y_AXIS), "model: min <= pref");
        javax.swing.text.html.HTMLDocument plain = deepLoad("<html><body><div id='p'>x</div></body></html>");
        javax.swing.text.html.BlockView bp = new javax.swing.text.html.BlockView(plain.getElement("p"), javax.swing.text.View.Y_AXIS);
        check(bp.getAttributes().getAttributeCount() == 0, "model: plain div carries no css attributes");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(BlockViewTest::testWhiteBox);
        runOnEDT(BlockViewTest::testBlackBox);
        runOnEDT(BlockViewTest::testModelLayer);
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
