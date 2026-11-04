/*
 * @test
 * @summary javax.swing.text.html.Option conformance: white-box structure + black-box behavior.
 * @run main OptionTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.Option;

public class OptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.Option");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        attr.addAttribute(HTML.Attribute.VALUE, "val1");
        attr.addAttribute(HTML.Attribute.SELECTED, "selected");
        Option o = new Option(attr);
        check(o.isSelected(), "selected when SELECTED present");
        check("val1".equals(o.getValue()), "getValue from VALUE attr");

        o.setLabel("My Label");
        check("My Label".equals(o.getLabel()), "getLabel after setLabel");
        check("My Label".equals(o.toString()), "toString is label");
        check("val1".equals(o.getValue()), "getValue still VALUE attr");

        AttributeSet copy = o.getAttributes();
        check("val1".equals(copy.getAttribute(HTML.Attribute.VALUE)), "getAttributes copied value");
        check(copy.getAttribute(HTML.Attribute.SELECTED) != null, "getAttributes copied selected");

        // attributes are copied at construction: mutating the original does not leak
        attr.addAttribute(HTML.Attribute.VALUE, "changed");
        check("val1".equals(o.getValue()), "attributes copied, immune to later mutation");

        // without VALUE, getValue falls back to label
        Option plain = new Option(new SimpleAttributeSet());
        check(!plain.isSelected(), "not selected by default");
        plain.setLabel("fallback");
        check("fallback".equals(plain.getValue()), "getValue falls back to label");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
