/*
 * @test
 * @summary javax.swing.text.html.parser.TagElement conformance: white-box structure + black-box behavior.
 * @run main TagElementTest
 */

import java.lang.reflect.Modifier;

import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.parser.TagElement;

public class TagElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.TagElement");
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

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");

        Element body = dtd.getElement("body");
        TagElement tBody = new TagElement(body);
        check(tBody.getElement() == body, "body getElement");
        check(tBody.getHTMLTag() == HTML.Tag.BODY, "body getHTMLTag BODY");
        check(!tBody.fictional(), "body not fictional");
        check(tBody.breaksFlow(), "body breaksFlow");
        check(!tBody.isPreformatted(), "body not preformatted");

        // fictional flag constructor
        TagElement tFict = new TagElement(body, true);
        check(tFict.fictional(), "fictional constructor true");
        check(tFict.getElement() == body, "fictional getElement");

        // block-level p breaks flow; inline b does not
        check(new TagElement(dtd.getElement("p")).breaksFlow(), "p breaksFlow");
        check(!new TagElement(dtd.getElement("b")).breaksFlow(), "b does not break flow");
        check(!new TagElement(dtd.getElement("img")).breaksFlow(), "img does not break flow");

        // pre is preformatted
        TagElement tPre = new TagElement(dtd.getElement("pre"));
        check(tPre.isPreformatted(), "pre is preformatted");
        check(tPre.breaksFlow(), "pre breaksFlow");

        // unknown element yields an HTML.UnknownTag
        TagElement tUnknown = new TagElement(dtd.getElement("foobar"));
        check(tUnknown.getHTMLTag() instanceof HTML.UnknownTag, "unknown element -> UnknownTag");
        check(!tUnknown.fictional(), "unknown tag not fictional");
        check("foobar".equals(tUnknown.getElement().getName()), "unknown tag element name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
