/*
 * @test
 * @summary javax.swing.text.html.CSS conformance: white-box structure + black-box behavior.
 * @run main CSSTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.html.CSS;

public class CSSTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.CSS");
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
        CSS css = new CSS();
        check(css != null, "new CSS()");

        CSS.Attribute[] all = CSS.getAllAttributeKeys();
        check(all != null && all.length == 67, "getAllAttributeKeys length 67");

        CSS.Attribute color = CSS.getAttribute("color");
        check(color != null, "getAttribute(color) non-null");
        check(color.toString().equals("color"), "color toString");
        check("black".equals(color.getDefaultValue()), "color default black");
        check(color.isInherited(), "color inherited");

        CSS.Attribute fontSize = CSS.getAttribute("font-size");
        check(fontSize.toString().equals("font-size"), "font-size toString");
        check("medium".equals(fontSize.getDefaultValue()), "font-size default medium");
        check(fontSize.isInherited(), "font-size inherited");

        CSS.Attribute fontFamily = CSS.getAttribute("font-family");
        check(fontFamily.toString().equals("font-family"), "font-family toString");
        check(fontFamily.getDefaultValue() == null, "font-family default null");
        check(fontFamily.isInherited(), "font-family inherited");

        CSS.Attribute background = CSS.getAttribute("background");
        check(background.getDefaultValue() == null, "background default null");
        check(!background.isInherited(), "background not inherited");

        check(CSS.getAttribute("no-such-attr") == null, "unknown attribute null");

        check(CSS.Attribute.COLOR == color, "COLOR constant identity");
        check(CSS.Attribute.FONT_SIZE == fontSize, "FONT_SIZE constant identity");
        check(CSS.Attribute.FONT_FAMILY == fontFamily, "FONT_FAMILY constant identity");
        check(CSS.Attribute.BACKGROUND == background, "BACKGROUND constant identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
