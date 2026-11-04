/*
 * @test
 * @summary javax.swing.text.html.parser.Parser conformance: white-box structure + black-box behavior.
 * @run main ParserTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.ChangedCharSetException;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.Parser;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.parser.TagElement;

public class ParserTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.Parser");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.html.parser.DTDConstants"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class Capture extends Parser {
        final List<String> starts = new ArrayList<String>();
        final List<String> ends = new ArrayList<String>();
        final List<String> empties = new ArrayList<String>();
        final List<String> texts = new ArrayList<String>();
        final List<String> comments = new ArrayList<String>();
        final List<Boolean> fictional = new ArrayList<Boolean>();

        Capture(DTD dtd) { super(dtd); }

        protected void handleStartTag(TagElement tag) {
            starts.add(tag.getElement().getName());
            fictional.add(tag.fictional());
        }
        protected void handleEndTag(TagElement tag) { ends.add(tag.getElement().getName()); }
        protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException { empties.add(tag.getElement().getName()); }
        protected void handleText(char[] text) { texts.add(new String(text)); }
        protected void handleComment(char[] text) { comments.add(new String(text)); }

        int line() { return getCurrentLine(); }
        int pos() { return getCurrentPos(); }
    }

    static void testBlackBox() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");

        Capture c = new Capture(dtd);
        c.parse(new StringReader("<p>Hello</p>"));
        check(c.starts.size() == 4, "starts count == 4, got " + c.starts);
        check("html".equals(c.starts.get(0)), "start html");
        check("head".equals(c.starts.get(1)), "start head");
        check("body".equals(c.starts.get(2)), "start body");
        check("p".equals(c.starts.get(3)), "start p");
        check(c.fictional.get(0) && c.fictional.get(1) && c.fictional.get(2), "html/head/body fictional");
        check(!c.fictional.get(3), "explicit p not fictional");
        check(c.texts.contains("Hello"), "text Hello");
        check(c.ends.contains("p") && c.ends.contains("body") && c.ends.contains("html"), "end tags present");
        check(c.line() == 1, "single line input line == 1");

        Capture c2 = new Capture(dtd);
        c2.parse(new StringReader("<br>foo"));
        check(c2.empties.contains("br"), "empty tag br");
        check(c2.texts.contains("foo"), "text foo");

        Capture c3 = new Capture(dtd);
        c3.parse(new StringReader("<!-- note -->"));
        check(c3.comments.size() == 1, "one comment");
        check(c3.comments.get(0).contains("note"), "comment text note");
        check(c3.pos() >= 0, "current position non-negative");
    }

    // ---- Part 3: deep contract coverage ----

    /** Parser-level full-event capture as one ordered table. */
    static class Seq extends Parser {
        final List<String> log = new ArrayList<String>();
        Seq(DTD d) { super(d); }
        protected void handleStartTag(TagElement tag) {
            log.add("S:" + tag.getElement().getName() + (tag.fictional() ? "*" : ""));
        }
        protected void handleEndTag(TagElement tag) { log.add("E:" + tag.getElement().getName()); }
        protected void handleEmptyTag(TagElement tag) { log.add("P:" + tag.getElement().getName()); }
        protected void handleText(char[] text) { log.add("T:" + new String(text)); }
        protected void handleComment(char[] text) { log.add("C:" + new String(text)); }
        protected void handleError(int ln, String msg) { log.add("!:" + ln + ":" + msg); }
        int line() { return getCurrentLine(); }
        int pos() { return getCurrentPos(); }
    }

    static DTD html32() throws Exception {
        new ParserDelegator();
        return DTD.getDTD("html32");
    }

    static void testDeepSequenceTable() throws Exception {
        DTD dtd = html32();
        Seq p = new Seq(dtd);
        p.parse(new StringReader("<!--c--><p>Hi <b>there</b><br></p>"));
        List<String> expected = java.util.Arrays.asList(
            "C:c",
            "S:html*", "S:head*", "E:head", "S:body*",
            "S:p", "T:Hi ", "S:b", "T:there", "E:b", "P:br",
            "E:p", "E:body", "E:html");
        check(p.log.equals(expected), "parser-level event table, got " + p.log);
        // text entities are decoded before handleText
        Seq p2 = new Seq(dtd);
        p2.parse(new StringReader("<p>&amp;&#65;"));
        boolean found = false;
        for (String s : p2.log) if (s.equals("T:&A")) found = true;
        check(found, "parser entity decoding, got " + p2.log);
        // the same Parser instance parses a second document with fresh state
        p2.parse(new StringReader("<p>second"));
        boolean second = false;
        for (String s : p2.log) if (s.equals("T:second")) second = true;
        check(second, "parser reusable for a second parse");
        // stray end tag reports an error through handleError
        Seq p3 = new Seq(dtd);
        p3.parse(new StringReader("</p>"));
        check(p3.log.get(0).startsWith("!:") && p3.log.get(0).contains("end.extra.tag"),
              "stray end tag error, got " + p3.log);
    }

    static void testDeepLineAndPos() throws Exception {
        DTD dtd = html32();
        Seq fresh = new Seq(dtd);
        check(fresh.line() == 0, "getCurrentLine starts at 0 on a fresh parser: " + fresh.line());
        check(fresh.pos() == 0, "getCurrentPos starts at 0 on a fresh parser");
        Seq p = new Seq(dtd);
        p.parse(new StringReader("<p>a\nb\nc"));
        check(p.line() == 3, "getCurrentLine keeps the last seen line after parse: " + p.line());
        // after parsing three lines the line counter sits at 3
        final class L extends Parser {
            List<Integer> lines = new ArrayList<Integer>();
            L(DTD d) { super(d); }
            int pos() { return getCurrentPos(); }
            protected void handleText(char[] t) { lines.add(getCurrentLine()); }
        }
        L l = new L(dtd);
        l.parse(new StringReader("<p>a\nb\nc"));
        check(l.lines.size() == 1 && l.lines.get(0) == 3, "line counter after 2 newlines is 3, got " + l.lines);
        check(l.pos() >= 0, "getCurrentPos non-negative");
    }

    static void testDeepProtectedSurface() throws Exception {
        DTD dtd = html32();
        // protected dtd field carries the constructor argument
        final class D extends Parser {
            D(DTD x) { super(x); }
            boolean same(DTD x) { return dtd == x; }
            boolean strictDefault() { return strict; }
        }
        D d = new D(dtd);
        check(d.same(dtd), "protected dtd field is the constructor DTD");
        check(!d.strictDefault(), "strict defaults to false");
        // makeTag: fictional flag honored, no instance caching, canonical HTML.Tag
        final class M extends Parser {
            M(DTD x) { super(x); }
            TagElement plain(Element e) { return makeTag(e); }
            TagElement fict(Element e) { return makeTag(e, true); }
        }
        M m = new M(dtd);
        javax.swing.text.html.parser.Element p = dtd.getElement("p");
        TagElement a = m.plain(p);
        TagElement b = m.plain(p);
        TagElement f = m.fict(p);
        check(!a.fictional() && !b.fictional(), "makeTag(elem) not fictional");
        check(f.fictional(), "makeTag(elem,true) fictional");
        check(a != b, "makeTag does not cache tag instances");
        check(a.getHTMLTag() == javax.swing.text.html.HTML.Tag.P, "makeTag tag maps to HTML.Tag.P");
        // getAttributes/flushAttributes from inside a callback
        final List<String> attrLog = new ArrayList<String>();
        final class A extends Parser {
            A(DTD x) { super(x); }
            protected void handleStartTag(TagElement t) {
                javax.swing.text.AttributeSet as = getAttributes();
                attrLog.add(t.getElement().getName() + ":" + (as == null ? -1 : as.getAttributeCount()));
                flushAttributes();
                attrLog.add("flushed:" + getAttributes().getAttributeCount());
            }
        }
        new A(dtd).parse(new StringReader("<p align=center>tt"));
        check(attrLog.contains("flushed:0"), "flushAttributes clears the attribute set, got " + attrLog);
    }

    static void testDeepNegatives() throws Exception {
        DTD dtd = html32();
        // parse(null) does NOT throw: the internal NPE is caught by parse() and
        // reported through handleError as "exception java.lang.NullPointerException ..."
        Seq np = new Seq(dtd);
        np.parse(null); // must simply return
        boolean nullReaderError = false;
        for (String s : np.log) if (s.contains("exception java.lang.NullPointerException")) nullReaderError = true;
        check(nullReaderError, "parse(null) reported via handleError, got " + np.log);
        // parseDTDMarkup without an initialized reader throws NPE
        final class M2 extends Parser {
            M2(DTD x) { super(x); }
            String probe() throws java.io.IOException { return parseDTDMarkup(); }
        }
        try {
            new M2(dtd).probe();
            check(false, "expected NPE for parseDTDMarkup without reader");
        } catch (NullPointerException expected) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepSequenceTable();
        testDeepLineAndPos();
        testDeepProtectedSurface();
        testDeepNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
