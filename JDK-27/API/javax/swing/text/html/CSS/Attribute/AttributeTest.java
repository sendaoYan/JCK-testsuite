/*
 * @test
 * @summary javax.swing.text.html.CSS.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.html.CSS;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.CSS$Attribute");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(Modifier.isFinal(mods) == true, "final modifier");
    }

    static void testBlackBox() throws Exception {
        check(CSS.Attribute.COLOR.toString().equals("color"), "COLOR toString");
        check(CSS.Attribute.BACKGROUND.toString().equals("background"), "BACKGROUND toString");
        check(CSS.Attribute.FONT_FAMILY.toString().equals("font-family"), "FONT_FAMILY toString");
        check(CSS.Attribute.FONT_SIZE.toString().equals("font-size"), "FONT_SIZE toString");

        check("black".equals(CSS.Attribute.COLOR.getDefaultValue()), "COLOR default black");
        check(CSS.Attribute.COLOR.isInherited(), "COLOR inherited");
        check(CSS.Attribute.BACKGROUND.getDefaultValue() == null, "BACKGROUND default null");
        check(!CSS.Attribute.BACKGROUND.isInherited(), "BACKGROUND not inherited");
        check(CSS.Attribute.FONT_FAMILY.getDefaultValue() == null, "FONT_FAMILY default null");

        check(CSS.Attribute.COLOR != CSS.Attribute.FONT_SIZE, "distinct constants");
        check(CSS.getAttribute("color") == CSS.Attribute.COLOR, "map returns same instance");
        check(CSS.getAttribute("font-size") == CSS.Attribute.FONT_SIZE, "map returns same instance 2");

        boolean found = false;
        for (CSS.Attribute a : CSS.getAllAttributeKeys()) {
            if (a == CSS.Attribute.COLOR) { found = true; break; }
        }
        check(found, "getAllAttributeKeys contains COLOR");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
