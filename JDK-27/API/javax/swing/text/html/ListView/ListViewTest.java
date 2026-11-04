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

    static javax.swing.text.html.HTMLEditorKit deepKit = new javax.swing.text.html.HTMLEditorKit();

    static javax.swing.text.html.HTMLDocument deepLoad(String html) throws Exception {
        javax.swing.text.html.HTMLDocument d = (javax.swing.text.html.HTMLDocument) deepKit.createDefaultDocument();
        deepKit.read(new java.io.StringReader(html), d, 0);
        return d;
    }

    static boolean deepHasAttrName(javax.swing.text.AttributeSet s, String name) {
        for (java.util.Enumeration<?> e = s.getAttributeNames(); e.hasMoreElements(); ) {
            if (name.equals(String.valueOf(e.nextElement()))) return true;
        }
        return false;
    }

    // ---- model layer over a fixed document (headless-safe) ----
    static void testModelLayer() throws Exception {
                javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        javax.swing.text.html.HTMLDocument doc = deepLoad("<html><body><ul id='ul'><li>item one</li><li>item two</li></ul></body></html>");
        javax.swing.text.Element ul = doc.getElement("ul");
        check(ul != null, "model: ul element found");
        javax.swing.text.html.ListView lv = new javax.swing.text.html.ListView(ul);
        check(lv.getElement() == ul, "model: element identity");
        javax.swing.text.AttributeSet attrs = lv.getAttributes();
        check(attrs.getAttributeCount() == 6, "model: ul view carries six attributes");
        check(attrs.isDefined(javax.swing.text.html.CSS.Attribute.LIST_STYLE_TYPE), "model: list-style-type defined");
        check(String.valueOf(attrs.getAttribute(javax.swing.text.html.CSS.Attribute.LIST_STYLE_TYPE)).contains("disc"),
              "model: default marker is disc");
        check(attrs.isDefined(javax.swing.text.html.CSS.Attribute.MARGIN_TOP), "model: margin-top defined");
        check(attrs.isDefined(javax.swing.text.html.CSS.Attribute.MARGIN_BOTTOM), "model: margin-bottom defined");
        check(deepHasAttrName(attrs, "margin-left-ltr"), "model: margin-left-ltr defined");
        check(deepHasAttrName(attrs, "margin-right-rtl"), "model: margin-right-rtl defined");
        check(lv.getAlignment(javax.swing.text.View.Y_AXIS) == 0.5f, "model: alignment Y 0.5");
        check(lv.getResizeWeight(javax.swing.text.View.X_AXIS) == 1, "model: resizeWeight X == 1");
        check(lv.getResizeWeight(javax.swing.text.View.Y_AXIS) == 0, "model: resizeWeight Y == 0");
        javax.swing.text.html.HTMLDocument sdoc = deepLoad("<html><body><ul type='square'><li>one</li></ul></body></html>");
        javax.swing.text.Element sul = sdoc.getDefaultRootElement().getElement(1).getElement(0);
        javax.swing.text.html.ListView slv = new javax.swing.text.html.ListView(sul);
        check(String.valueOf(slv.getAttributes().getAttribute(javax.swing.text.html.CSS.Attribute.LIST_STYLE_TYPE)).contains("square"),
              "model: type=square maps to list-style-type square");
        javax.swing.text.html.HTMLDocument odoc = deepLoad("<html><body><ol><li>one</li></ol></body></html>");
        javax.swing.text.Element ol = odoc.getDefaultRootElement().getElement(1).getElement(0);
        javax.swing.text.html.ListView olv = new javax.swing.text.html.ListView(ol);
        check(String.valueOf(olv.getAttributes().getAttribute(javax.swing.text.html.CSS.Attribute.LIST_STYLE_TYPE)).contains("decimal"),
              "model: ol default marker is decimal");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ListViewTest::testWhiteBox);
        runOnEDT(ListViewTest::testBlackBox);
        runOnEDT(ListViewTest::testModelLayer);
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
