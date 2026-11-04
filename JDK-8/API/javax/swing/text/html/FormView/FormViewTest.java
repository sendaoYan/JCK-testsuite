/*
 * @test
 * @summary javax.swing.text.html.FormView conformance: submit/reset
 *          constants, span behavior without a parent and activation
 *          behavior without a container (headless-safe, model layer only).
 * @run main FormViewTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class FormViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    static Element findByTag(HTMLDocument doc, HTML.Tag tag) {
        Deque<Element> q = new ArrayDeque<Element>();
        q.add(doc.getDefaultRootElement());
        while (!q.isEmpty()) {
            Element e = q.poll();
            if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == tag) return e;
            for (int i = 0; i < e.getElementCount(); i++) q.add(e.getElement(i));
        }
        return null;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.FormView", false, FormViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.ComponentView"), "superclass");
        check(hasInterface(c, "java.awt.event.ActionListener"), "implements java.awt.event.ActionListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMaximumSpan"), "method getMaximumSpan");
        check(names.contains("actionPerformed"), "method actionPerformed");
    }

    // ---- Part 2: model-layer behavior ----
    static void testModelLayer() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        check("Submit Query".equals(FormView.SUBMIT), "SUBMIT default label");
        check("Reset".equals(FormView.RESET), "RESET default label");

        HTMLDocument doc = load("<html><body><form action='/go'>"
                + "<input type='text' name='t1'>"
                + "<input type='submit' value='Go'>"
                + "<input type='reset'>"
                + "</form></body></html>");
        Element input = findByTag(doc, HTML.Tag.INPUT);
        check(input != null, "input element found");

        FormView fv = new FormView(input);
        check(fv.getElement() == input, "element identity");
        check(fv.getComponent() == null, "no component before parenting");
        check(fv.getMaximumSpan(View.X_AXIS) == 0.0f, "max span X zero without parent");
        check(fv.getMaximumSpan(View.Y_AXIS) == 0.0f, "max span Y zero without parent");

        // activation without an established container fails with NPE
        boolean npe1 = false;
        try { fv.actionPerformed(new java.awt.event.ActionEvent(fv, 0, "x")); }
        catch (NullPointerException e) { npe1 = true; }
        check(npe1, "actionPerformed(plain command) NPE without container");

        boolean npe2 = false;
        try { fv.actionPerformed(new java.awt.event.ActionEvent(fv, 0, FormView.SUBMIT)); }
        catch (NullPointerException e) { npe2 = true; }
        check(npe2, "actionPerformed(SUBMIT) NPE without container");

        boolean npe3 = false;
        try { fv.actionPerformed(new java.awt.event.ActionEvent(fv, 0, FormView.RESET)); }
        catch (NullPointerException e) { npe3 = true; }
        check(npe3, "actionPerformed(RESET) NPE without container");

        // the three input types parsed above are leaves carrying type attributes
        Element submit = findByTag(doc, HTML.Tag.INPUT);
        int types = 0;
        Deque<Element> q = new ArrayDeque<Element>();
        q.add(doc.getDefaultRootElement());
        while (!q.isEmpty()) {
            Element e = q.poll();
            if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.INPUT) {
                check(e.isLeaf(), "input is a leaf element");
                types++;
            }
            for (int i = 0; i < e.getElementCount(); i++) q.add(e.getElement(i));
        }
        check(types == 3, "three input elements in the form");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testModelLayer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
