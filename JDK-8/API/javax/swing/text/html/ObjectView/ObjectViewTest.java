/*
 * @test
 * @summary javax.swing.text.html.ObjectView conformance: component creation
 *          for valid, non-Component, missing and absent classid values,
 *          param propagation and component availability (headless-safe).
 * @run main ObjectViewTest
 */

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ObjectView;

public class ObjectViewTest {
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

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.ObjectView", false, ObjectViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.ComponentView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getComponent"), "method getComponent");
    }

    // ---- Part 2: component creation matrix ----
    static void testCreateComponent() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        HTMLDocument doc = load("<html><body>"
                + "<object classid='javax.swing.JLabel' id='ok'><param name='text' value='P'></object>"
                + "<object classid='java.lang.String' id='bad'></object>"
                + "<object classid='no.such.Clazz' id='miss'></object>"
                + "<object id='none'></object>"
                + "</body></html>");
        Method create = Class.forName("javax.swing.text.ComponentView")
                .getDeclaredMethod("createComponent");
        create.setAccessible(true);

        // valid Component classid + param: label built from bean properties
        ObjectView ok = new ObjectView(doc.getElement("ok"));
        Object c1 = create.invoke(ok);
        check(c1 instanceof JLabel, "valid classid yields a JLabel");
        if (c1 instanceof JLabel) {
            JLabel lb = (JLabel) c1;
            check("P".equals(lb.getText()), "param name/text propagated to the bean");
            check(!lb.getForeground().equals(java.awt.Color.red), "valid component not flagged red");
        }

        // classid that is not a Component: red question label
        ObjectView bad = new ObjectView(doc.getElement("bad"));
        Object c2 = create.invoke(bad);
        check(c2 instanceof JLabel, "non-Component classid falls back to a JLabel");
        if (c2 instanceof JLabel) {
            JLabel lb = (JLabel) c2;
            check("??".equals(lb.getText()), "fallback label text is ??");
            check(lb.getForeground().equals(java.awt.Color.red), "fallback label is red");
        }

        // missing class: same red question fallback
        ObjectView miss = new ObjectView(doc.getElement("miss"));
        Object c3 = create.invoke(miss);
        check(c3 instanceof JLabel && "??".equals(((JLabel) c3).getText())
              && ((JLabel) c3).getForeground().equals(java.awt.Color.red),
              "unresolvable classid gets the red ? fallback");

        // no classid attribute at all: same fallback
        ObjectView none = new ObjectView(doc.getElement("none"));
        Object c4 = create.invoke(none);
        check(c4 instanceof JLabel && "??".equals(((JLabel) c4).getText())
              && ((JLabel) c4).getForeground().equals(java.awt.Color.red),
              "missing classid gets the red ? fallback");

        // component becomes available only once the view is parented
        ObjectView fresh = new ObjectView(doc.getElement("ok"));
        check(fresh.getComponent() == null, "getComponent null before parenting");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testCreateComponent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
