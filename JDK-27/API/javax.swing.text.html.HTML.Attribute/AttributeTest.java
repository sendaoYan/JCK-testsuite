/*
 * @test
 * @summary javax.swing.text.html.HTML.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.html.HTML;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTML$Attribute");
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
        check(HTML.Attribute.HREF.toString().equals("href"), "HREF toString");
        check(HTML.Attribute.SRC.toString().equals("src"), "SRC toString");
        check(HTML.Attribute.TITLE.toString().equals("title"), "TITLE toString");
        check(HTML.Attribute.ACTION.toString().equals("action"), "ACTION toString");
        check(HTML.Attribute.METHOD.toString().equals("method"), "METHOD toString");

        check(HTML.Attribute.HREF != HTML.Attribute.SRC, "distinct constants");
        check(HTML.getAttributeKey("href") == HTML.Attribute.HREF, "getAttributeKey(href) identity");
        check(HTML.getAttributeKey("src") == HTML.Attribute.SRC, "getAttributeKey(src) identity");
        check(HTML.getAttributeKey("title") == HTML.Attribute.TITLE, "getAttributeKey(title) identity");
        check(HTML.getAttributeKey(HTML.Attribute.HREF.toString()) == HTML.Attribute.HREF, "toString round-trip");

        boolean hrefFound = false, srcFound = false, titleFound = false;
        for (HTML.Attribute a : HTML.getAllAttributeKeys()) {
            if (a == HTML.Attribute.HREF) hrefFound = true;
            if (a == HTML.Attribute.SRC) srcFound = true;
            if (a == HTML.Attribute.TITLE) titleFound = true;
        }
        check(hrefFound && srcFound && titleFound, "getAllAttributeKeys contains HREF/SRC/TITLE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
