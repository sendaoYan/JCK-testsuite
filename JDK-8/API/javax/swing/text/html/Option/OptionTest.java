/*
 * @test
 * @summary javax.swing.text.html.Option conformance: label/value/selection
 *          semantics, attribute copy, serialization and parser integration
 *          (headless-safe).
 * @run main OptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.Option;

public class OptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.Option", false, OptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(Modifier.isPublic(c.getModifiers()) == true, "public modifier");
        check(Modifier.isProtected(c.getModifiers()) == false, "protected modifier");
        check(c.getInterfaces().length == 1, "one interface");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setLabel"), "method setLabel");
        check(names.contains("getLabel"), "method getLabel");
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("toString"), "method toString");
        check(names.contains("isSelected"), "method isSelected");
        check(names.contains("getValue"), "method getValue");
    }

    // ---- Part 2: label / value / selection semantics ----
    static void testSemantics() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(HTML.Attribute.VALUE, "v1");
        attrs.addAttribute(HTML.Attribute.SELECTED, "selected");

        Option op = new Option(attrs);
        check(op.getLabel() == null, "label defaults to null");
        check("v1".equals(op.getValue()), "value from attribute");
        check(op.isSelected(), "selected attribute sets selection");
        check(op.toString() == null, "toString is the null label");

        // attributes are copied, not aliased
        AttributeSet stored = op.getAttributes();
        check(stored != attrs, "attributes copied");
        check("v1".equals(stored.getAttribute(HTML.Attribute.VALUE)), "copied value preserved");
        attrs.addAttribute(HTML.Attribute.VALUE, "changed");
        check("v1".equals(op.getValue()), "copy isolated from later mutation");

        op.setLabel("L1");
        check("L1".equals(op.getLabel()), "setLabel round-trip");
        check("L1".equals(op.toString()), "toString is the label");
        check("v1".equals(op.getValue()), "explicit value still wins over label");

        // no value attribute: getValue falls back to the label
        Option noValue = new Option(new SimpleAttributeSet());
        check(noValue.getLabel() == null, "empty label null");
        check(noValue.getValue() == null, "no value falls back to null label");
        check(!noValue.isSelected(), "empty option not selected");
        noValue.setLabel("X");
        check("X".equals(noValue.getValue()), "getValue falls back to label");
        check("X".equals(noValue.toString()), "toString follows label");

        // selection only depends on the SELECTED attribute
        SimpleAttributeSet unselected = new SimpleAttributeSet();
        unselected.addAttribute(HTML.Attribute.VALUE, "v2");
        Option op2 = new Option(unselected);
        check(!op2.isSelected(), "no SELECTED attribute -> not selected");

        boolean npe = false;
        try { new Option(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "Option(null) NPE");
    }

    // ---- Part 3: serialization ----
    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(Option.class).getSerialVersionUID()
              == -7384465092562270804L, "serialVersionUID stable");

        // HTML.Attribute keys are registered for serialization lazily by the
        // outer HTML class initializer; force it before serializing options
        // whose attribute sets are keyed by HTML.Attribute instances.
        check(HTML.getTag("html") == HTML.Tag.HTML, "force HTML class init");

        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(HTML.Attribute.VALUE, "v1");
        attrs.addAttribute(HTML.Attribute.SELECTED, "selected");
        Option op = new Option(attrs);
        op.setLabel("L1");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(op);
        oos.close();
        Option back = (Option) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check("L1".equals(back.getLabel()), "round-trip label");
        check("v1".equals(back.getValue()), "round-trip value");
        check(back.isSelected(), "round-trip selection");
        check("L1".equals(back.toString()), "round-trip toString");
        check(back.getAttributes().getAttribute(HTML.Attribute.VALUE) != null, "round-trip attributes");
    }

    // ---- Part 4: integration with the HTML reader ----
    static void testParserIntegration() throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(
                "<html><body><select name='s'>"
                + "<option value='1' selected>One</option>"
                + "<option value='2'>Two</option>"
                + "</select></body></html>"), doc, 0);

        Element select = find(doc, HTML.Tag.SELECT);
        check(select != null, "select element found");
        if (select == null) return;
        check(select.isLeaf(), "select is a leaf element");
        check("s".equals(select.getAttributes().getAttribute(HTML.Attribute.NAME)), "select name attribute");

        Object model = select.getAttributes().getAttribute(StyleConstants.ModelAttribute);
        check(model instanceof DefaultComboBoxModel, "select carries combo box model");
        if (model instanceof DefaultComboBoxModel) {
            DefaultComboBoxModel<?> m = (DefaultComboBoxModel<?>) model;
            check(m.getSize() == 2, "two options in model");
            Object i0 = m.getElementAt(0);
            check(i0 instanceof Option, "model items are Options");
            if (i0 instanceof Option) {
                Option o0 = (Option) i0;
                check("One".equals(o0.getLabel()), "parser set label from text");
                check("1".equals(o0.getValue()), "value from attribute");
                check(o0.isSelected(), "selected option flagged");
            }
            Object i1 = m.getElementAt(1);
            if (i1 instanceof Option) {
                Option o1 = (Option) i1;
                check("Two".equals(o1.getLabel()), "second label from text");
                check("2".equals(o1.getValue()), "second value");
                check(!o1.isSelected(), "second not selected");
            }
        }
    }

    static Element find(HTMLDocument doc, HTML.Tag tag) {
        Deque<Element> q = new ArrayDeque<Element>();
        q.add(doc.getDefaultRootElement());
        while (!q.isEmpty()) {
            Element e = q.poll();
            if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == tag) return e;
            for (int i = 0; i < e.getElementCount(); i++) q.add(e.getElement(i));
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testSemantics();
        testSerialization();
        testParserIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
