/*
 * @test
 * @summary javax.swing.text.html.ObjectView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm --add-opens=java.desktop/javax.swing.text=ALL-UNNAMED ObjectViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ObjectView;

public class ObjectViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.html.ObjectView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.ComponentView"), "superclass");
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
            editor.setText("<html><body><object classid='java.lang.String'><param name='text' value='x'></object></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element obj = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.OBJECT);
            check(obj != null, "OBJECT element found");

            ObjectView ov = new ObjectView(obj);
            check(ov.getElement() == obj, "getElement round trip");
            check(ov.getStartOffset() == obj.getStartOffset(), "start offset");
            check(ov.getEndOffset() == obj.getEndOffset(), "end offset");

            // standalone (component not yet created) spans are 0, invalid axis -> IAE
            check(ov.getPreferredSpan(View.X_AXIS) == 0f, "standalone preferred X == 0");
            check(ov.getMinimumSpan(View.Y_AXIS) == 0f, "standalone minimum Y == 0");
            check(throwsIAE(() -> ov.getMaximumSpan(4)), "getMaximumSpan(4) -> IAE");

            // a live ObjectView creates an unloadable-representation JLabel for a
            // non-Component classid
            View live = findView(editor.getUI().getRootView(editor), "javax.swing.text.html.ObjectView");
            check(live instanceof ObjectView, "live ObjectView from tree");
            if (live instanceof ObjectView) {
                ObjectView lo = (ObjectView) live;
                check(lo.getComponent() instanceof JLabel, "unloadable object -> JLabel component");
                float pref = lo.getPreferredSpan(View.X_AXIS);
                float min = lo.getMinimumSpan(View.X_AXIS);
                float max = lo.getMaximumSpan(View.X_AXIS);
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

    static javax.swing.text.html.HTMLEditorKit deepKit = new javax.swing.text.html.HTMLEditorKit();

    static javax.swing.text.html.HTMLDocument deepLoad(String html) throws Exception {
        javax.swing.text.html.HTMLDocument d = (javax.swing.text.html.HTMLDocument) deepKit.createDefaultDocument();
        deepKit.read(new java.io.StringReader(html), d, 0);
        return d;
    }

    // ---- component creation matrix (headless-safe, reflection) ----
    static void testCreateComponent() throws Exception {
                javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        javax.swing.text.html.HTMLDocument doc = deepLoad("<html><body>"
                + "<object classid='javax.swing.JLabel' id='ok'><param name='text' value='P'></object>"
                + "<object classid='java.lang.String' id='bad'></object>"
                + "<object classid='no.such.Clazz' id='miss'></object>"
                + "<object id='none'></object>"
                + "</body></html>");
        java.lang.reflect.Method create = Class.forName("javax.swing.text.ComponentView")
                .getDeclaredMethod("createComponent");
        create.setAccessible(true);
        javax.swing.text.html.ObjectView ok = new javax.swing.text.html.ObjectView(doc.getElement("ok"));
        Object c1 = create.invoke(ok);
        check(c1 instanceof javax.swing.JLabel, "reflect: valid classid yields a JLabel");
        if (c1 instanceof javax.swing.JLabel) {
            javax.swing.JLabel lb = (javax.swing.JLabel) c1;
            check("P".equals(lb.getText()), "reflect: param name/text propagated to the bean");
            check(!lb.getForeground().equals(java.awt.Color.red), "reflect: valid component not flagged red");
        }
        javax.swing.text.html.ObjectView bad = new javax.swing.text.html.ObjectView(doc.getElement("bad"));
        Object c2 = create.invoke(bad);
        check(c2 instanceof javax.swing.JLabel && "??".equals(((javax.swing.JLabel) c2).getText())
              && ((javax.swing.JLabel) c2).getForeground().equals(java.awt.Color.red),
              "reflect: non-Component classid gets the red ?? fallback");
        javax.swing.text.html.ObjectView miss = new javax.swing.text.html.ObjectView(doc.getElement("miss"));
        Object c3 = create.invoke(miss);
        check(c3 instanceof javax.swing.JLabel && "??".equals(((javax.swing.JLabel) c3).getText())
              && ((javax.swing.JLabel) c3).getForeground().equals(java.awt.Color.red),
              "reflect: unresolvable classid gets the red ?? fallback");
        javax.swing.text.html.ObjectView none = new javax.swing.text.html.ObjectView(doc.getElement("none"));
        Object c4 = create.invoke(none);
        check(c4 instanceof javax.swing.JLabel && "??".equals(((javax.swing.JLabel) c4).getText())
              && ((javax.swing.JLabel) c4).getForeground().equals(java.awt.Color.red),
              "reflect: missing classid gets the red ?? fallback");
        javax.swing.text.html.ObjectView fresh = new javax.swing.text.html.ObjectView(doc.getElement("ok"));
        check(fresh.getComponent() == null, "reflect: getComponent null before parenting");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ObjectViewTest::testWhiteBox);
        runOnEDT(ObjectViewTest::testBlackBox);
        runOnEDT(ObjectViewTest::testCreateComponent);
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
