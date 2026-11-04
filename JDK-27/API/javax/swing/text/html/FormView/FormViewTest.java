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

    static javax.swing.text.html.HTMLEditorKit deepKit = new javax.swing.text.html.HTMLEditorKit();

    static javax.swing.text.html.HTMLDocument deepLoad(String html) throws Exception {
        javax.swing.text.html.HTMLDocument d = (javax.swing.text.html.HTMLDocument) deepKit.createDefaultDocument();
        deepKit.read(new java.io.StringReader(html), d, 0);
        return d;
    }

    static javax.swing.text.Element deepFindByTag(javax.swing.text.html.HTMLDocument doc, javax.swing.text.html.HTML.Tag tag) {
        java.util.ArrayDeque<javax.swing.text.Element> q = new java.util.ArrayDeque<javax.swing.text.Element>();
        q.add(doc.getDefaultRootElement());
        while (!q.isEmpty()) {
            javax.swing.text.Element e = q.poll();
            if (e.getAttributes().getAttribute(javax.swing.text.StyleConstants.NameAttribute) == tag) return e;
            for (int i = 0; i < e.getElementCount(); i++) q.add(e.getElement(i));
        }
        return null;
    }

    // ---- model layer over a fixed document (headless-safe) ----
    static void testModelLayer() throws Exception {
                javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        check("Submit Query".equals(javax.swing.text.html.FormView.SUBMIT), "model: SUBMIT default label");
        check("Reset".equals(javax.swing.text.html.FormView.RESET), "model: RESET default label");
        javax.swing.text.html.HTMLDocument doc = deepLoad("<html><body><form action='/go'><input type='text' name='t1'><input type='submit' value='Go'><input type='reset'></form></body></html>");
        javax.swing.text.Element input = deepFindByTag(doc, javax.swing.text.html.HTML.Tag.INPUT);
        check(input != null, "model: input element found");
        javax.swing.text.html.FormView fv = new javax.swing.text.html.FormView(input);
        check(fv.getElement() == input, "model: element identity");
        check(fv.getComponent() == null, "model: no component before parenting");
        check(fv.getMaximumSpan(javax.swing.text.View.X_AXIS) == 0.0f, "model: max span X zero without parent");
        check(fv.getMaximumSpan(javax.swing.text.View.Y_AXIS) == 0.0f, "model: max span Y zero without parent");
        boolean npe1 = false;
        try { fv.actionPerformed(new java.awt.event.ActionEvent(fv, 0, "x")); }
        catch (NullPointerException e) { npe1 = true; }
        check(npe1, "model: actionPerformed(plain command) NPE without container");
        boolean npe2 = false;
        try { fv.actionPerformed(new java.awt.event.ActionEvent(fv, 0, javax.swing.text.html.FormView.SUBMIT)); }
        catch (NullPointerException e) { npe2 = true; }
        check(npe2, "model: actionPerformed(SUBMIT) NPE without container");
        boolean npe3 = false;
        try { fv.actionPerformed(new java.awt.event.ActionEvent(fv, 0, javax.swing.text.html.FormView.RESET)); }
        catch (NullPointerException e) { npe3 = true; }
        check(npe3, "model: actionPerformed(RESET) NPE without container");
        check(input.isLeaf(), "model: input is a leaf element");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(FormViewTest::testWhiteBox);
        runOnEDT(FormViewTest::testBlackBox);
        runOnEDT(FormViewTest::testModelLayer);
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
