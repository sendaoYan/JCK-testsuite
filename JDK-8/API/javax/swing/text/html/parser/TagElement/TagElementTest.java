/*
 * @test
 * @summary javax.swing.text.html.parser.TagElement conformance: white-box structure + black-box behavior.
 * @run main TagElementTest
 */

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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

    // ---- Part 0: legacy structure checks (preserved verbatim from the previous revision) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testLegacyStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.TagElement", false, TagElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("breaksFlow"), "method breaksFlow");
        check(names.contains("isPreformatted"), "method isPreformatted");
        check(names.contains("getElement"), "method getElement");
        check(names.contains("getHTMLTag"), "method getHTMLTag");
        check(names.contains("fictional"), "method fictional");
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

    // ---- Part 3: deep contract coverage ----

    static void expectNPE(String label, Runnable r) {
        try { r.run(); throw new AssertionError(label + ": expected NullPointerException"); }
        catch (NullPointerException expected) { passed++; }
    }

    static void testDeepNulls() {
        expectNPE("new TagElement(null)", new Runnable() { public void run() { new TagElement(null); } });
        expectNPE("new TagElement(null,true)", new Runnable() { public void run() { new TagElement(null, true); } });
    }

    static void testDeepFlagMatrix() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        // whitespace semantics of the html32 tag set: name -> {breaksFlow, isPreformatted}
        String[][] table = {
            {"p", "T", "F"}, {"br", "T", "F"}, {"head", "T", "F"}, {"li", "T", "F"},
            {"dl", "T", "F"}, {"pre", "T", "T"}, {"b", "F", "F"}, {"img", "F", "F"},
            {"td", "T", "F"}, {"tr", "F", "F"}, {"area", "F", "F"}, {"basefont", "F", "F"},
            {"blockquote", "T", "F"}, {"hr", "T", "F"}, {"table", "F", "F"}, {"dd", "T", "F"},
            {"dt", "T", "F"}, {"ul", "T", "F"}, {"ol", "T", "F"}, {"option", "F", "F"},
            {"address", "F", "F"}, {"h1", "T", "F"}, {"center", "T", "F"},
        };
        for (String[] row : table) {
            TagElement t = new TagElement(dtd.getElement(row[0]));
            check(t.breaksFlow() == row[1].equals("T"), row[0] + " breaksFlow " + row[1]);
            check(t.isPreformatted() == row[2].equals("T"), row[0] + " isPreformatted " + row[2]);
        }
        // both constructors expose the same element; the flag only sets fictional()
        Element body = dtd.getElement("body");
        TagElement plain = new TagElement(body);
        TagElement fict = new TagElement(body, true);
        check(plain.getElement() == body && fict.getElement() == body, "both ctors keep element");
        check(!plain.fictional() && fict.fictional(), "fictional flag only from second ctor");
        check(plain.breaksFlow() == fict.breaksFlow(), "fictional does not change breaksFlow");
    }

    static void testDeepHTMLTagMapping() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        // getHTMLTag returns the canonical HTML.Tag singletons (identity, not equals)
        check(new TagElement(dtd.getElement("body")).getHTMLTag() == HTML.Tag.BODY, "body -> Tag.BODY");
        check(new TagElement(dtd.getElement("pre")).getHTMLTag() == HTML.Tag.PRE, "pre -> Tag.PRE");
        check(new TagElement(dtd.getElement("p")).getHTMLTag() == HTML.Tag.P, "p -> Tag.P");
        check(new TagElement(dtd.getElement("br")).getHTMLTag() == HTML.Tag.BR, "br -> Tag.BR");
        check(new TagElement(dtd.getElement("html")).getHTMLTag() == HTML.Tag.HTML, "html -> Tag.HTML");
        // stable across calls on the same instance
        TagElement t = new TagElement(dtd.getElement("body"));
        check(t.getHTMLTag() == t.getHTMLTag(), "getHTMLTag stable");
        check(t.getElement() == t.getElement(), "getElement stable");
        // unknown elements map to a fresh HTML.UnknownTag that stringifies to the name
        TagElement u = new TagElement(dtd.getElement("probe-unknown-tag"));
        check(u.getHTMLTag() instanceof HTML.UnknownTag, "unknown -> UnknownTag instance");
        check("probe-unknown-tag".equals(u.getHTMLTag().toString()), "UnknownTag toString is the name");
        check(!u.getHTMLTag().breaksFlow() && !u.getHTMLTag().isPreformatted(),
              "UnknownTag neither breaks flow nor preformatted");
        // TagElement is not Serializable
        check(!java.io.Serializable.class.isAssignableFrom(TagElement.class), "TagElement not Serializable");
    }

    public static void main(String[] args) throws Exception {
        testLegacyStructure();
        testWhiteBox();
        testBlackBox();
        testDeepNulls();
        testDeepFlagMatrix();
        testDeepHTMLTagMapping();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
