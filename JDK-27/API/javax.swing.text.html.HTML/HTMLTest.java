/*
 * @test
 * @summary javax.swing.text.html.HTML conformance: white-box structure + black-box behavior.
 * @run main HTMLTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

public class HTMLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTML");
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
    }

    static void testBlackBox() throws Exception {
        HTML html = new HTML();
        check(html != null, "new HTML()");

        check(HTML.NULL_ATTRIBUTE_VALUE.equals("#DEFAULT"), "NULL_ATTRIBUTE_VALUE");

        HTML.Tag[] tags = HTML.getAllTags();
        check(tags != null && tags.length == 74, "getAllTags length 74");
        boolean hasA = false, hasP = false, hasBody = false;
        for (HTML.Tag t : tags) {
            if (t == HTML.Tag.A) hasA = true;
            if (t == HTML.Tag.P) hasP = true;
            if (t == HTML.Tag.BODY) hasBody = true;
        }
        check(hasA && hasP && hasBody, "getAllTags contains A/P/BODY");

        check(HTML.getTag("a") == HTML.Tag.A, "getTag(a)");
        check(HTML.getTag("p") == HTML.Tag.P, "getTag(p)");
        check(HTML.getTag("body") == HTML.Tag.BODY, "getTag(body)");
        check(HTML.getTag("zzz-no-such") == null, "getTag unknown null");

        HTML.Attribute[] attrs = HTML.getAllAttributeKeys();
        check(attrs != null && attrs.length == 81, "getAllAttributeKeys length 81");
        boolean hasHref = false, hasSrc = false, hasTitle = false;
        for (HTML.Attribute a : attrs) {
            if (a == HTML.Attribute.HREF) hasHref = true;
            if (a == HTML.Attribute.SRC) hasSrc = true;
            if (a == HTML.Attribute.TITLE) hasTitle = true;
        }
        check(hasHref && hasSrc && hasTitle, "getAllAttributeKeys contains HREF/SRC/TITLE");

        check(HTML.getAttributeKey("href") == HTML.Attribute.HREF, "getAttributeKey(href)");
        check(HTML.getAttributeKey("src") == HTML.Attribute.SRC, "getAttributeKey(src)");
        check(HTML.getAttributeKey("title") == HTML.Attribute.TITLE, "getAttributeKey(title)");
        check(HTML.getAttributeKey("zzz-no-such") == null, "getAttributeKey unknown null");

        AttributeSet empty = new SimpleAttributeSet();
        check(HTML.getIntegerAttributeValue(empty, HTML.Attribute.COLSPAN, 5) == 5, "default int value");

        SimpleAttributeSet sized = new SimpleAttributeSet();
        sized.addAttribute(HTML.Attribute.COLSPAN, "3");
        check(HTML.getIntegerAttributeValue(sized, HTML.Attribute.COLSPAN, 5) == 3, "parse int value");

        SimpleAttributeSet bad = new SimpleAttributeSet();
        bad.addAttribute(HTML.Attribute.COLSPAN, "abc");
        check(HTML.getIntegerAttributeValue(bad, HTML.Attribute.COLSPAN, 5) == 5, "bad int falls back to default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
