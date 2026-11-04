/*
 * @test
 * @summary javax.swing.text.html.parser.DocumentParser conformance: white-box structure + black-box behavior.
 * @run main DocumentParserTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.ParserDelegator;

public class DocumentParserTest {
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
        Class<?> c = Class.forName("javax.swing.text.html.parser.DocumentParser", false, DocumentParserTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.parser.Parser"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("parse"), "method parse");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.DocumentParser");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.parser.Parser"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class CB extends HTMLEditorKit.ParserCallback {
        final List<String> starts = new ArrayList<String>();
        final List<String> ends = new ArrayList<String>();
        final List<String> simples = new ArrayList<String>();
        final List<String> texts = new ArrayList<String>();
        final List<String> comments = new ArrayList<String>();
        final List<Boolean> implied = new ArrayList<Boolean>();
        String eol;

        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            starts.add(t.toString());
            implied.add(Boolean.TRUE.equals(a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED)));
        }
        public void handleEndTag(HTML.Tag t, int pos) { ends.add(t.toString()); }
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) { simples.add(t.toString()); }
        public void handleText(char[] data, int pos) { texts.add(new String(data)); }
        public void handleComment(char[] data, int pos) { comments.add(new String(data)); }
        public void handleEndOfLineString(String e) { eol = e; }
    }

    static void testBlackBox() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        DocumentParser dp = new DocumentParser(dtd);

        CB cb = new CB();
        dp.parse(new StringReader("<p>Hello <b>world</b></p>"), cb, false);
        check(cb.starts.size() == 5, "start tags count == 5, got " + cb.starts);
        check("html".equals(cb.starts.get(0)), "start html");
        check("head".equals(cb.starts.get(1)), "start head");
        check("body".equals(cb.starts.get(2)), "start body");
        check("p".equals(cb.starts.get(3)), "start p");
        check("b".equals(cb.starts.get(4)), "start b");
        check(cb.implied.get(0) && cb.implied.get(1) && cb.implied.get(2), "html/head/body implied");
        check(!cb.implied.get(3), "explicit p not implied");
        check(cb.ends.contains("b") && cb.ends.contains("p") && cb.ends.contains("body") && cb.ends.contains("html"), "end tags present");
        check(cb.texts.size() == 2, "two text chunks");
        check("Hello ".equals(cb.texts.get(0)), "text Hello ");
        check("world".equals(cb.texts.get(1)), "text world");
        check("\n".equals(cb.eol), "end-of-line string newline");

        CB cb2 = new CB();
        dp.parse(new StringReader("<br>foo"), cb2, false);
        check(cb2.simples.contains("br"), "simple tag br");
        check(cb2.texts.contains("foo"), "text foo");

        CB cb3 = new CB();
        dp.parse(new StringReader("<!-- note -->"), cb3, false);
        check(cb3.comments.size() == 1, "one comment");
        check(cb3.comments.get(0).contains("note"), "comment text note");

        CB cb4 = new CB();
        dp.parse(new StringReader("<p>Hi</p>"), cb4, true);
        check(cb4.starts.contains("p"), "ignoreCharSet=true still emits p");
        check(cb4.texts.contains("Hi"), "ignoreCharSet=true still emits text");
        check("\n".equals(cb4.eol), "ignoreCharSet eol newline");
    }

    // ---- Part 3: deep contract coverage (full callback-sequence tables) ----

    /** Records every callback as a flat string for whole-table comparison. */
    static class SeqCB extends HTMLEditorKit.ParserCallback {
        final List<String> log = new ArrayList<String>();
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            log.add("S:" + t + "@" + pos + attrs(a));
        }
        public void handleEndTag(HTML.Tag t, int pos) { log.add("E:" + t + "@" + pos); }
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            log.add("P:" + t + "@" + pos + attrs(a));
        }
        public void handleText(char[] data, int pos) { log.add("T:" + new String(data) + "@" + pos); }
        public void handleComment(char[] data, int pos) { log.add("C:" + new String(data) + "@" + pos); }
        public void handleError(String errorMsg, int pos) { log.add("!:" + errorMsg + "@" + pos); }
        public void handleEndOfLineString(String eol) { log.add("$:" + eol.replace("\n", "\\n").replace("\r", "\\r")); }
        String attrs(MutableAttributeSet a) {
            StringBuilder sb = new StringBuilder();
            for (java.util.Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements(); ) {
                Object k = e.nextElement();
                sb.append("[" + k + "=" + a.getAttribute(k) + "]");
            }
            return sb.toString();
        }
    }

    static DocumentParser seqParser() throws Exception {
        new ParserDelegator();
        return new DocumentParser(DTD.getDTD("html32"));
    }

    static List<String> parseSeq(String html) throws Exception {
        // bootstrap the archived html32 DTD, then use a fresh DocumentParser per
        // document: positions accumulate when one instance is reused (asserted
        // separately in testDeepCharSetAndExceptions)
        seqParser();
        SeqCB cb = new SeqCB();
        new DocumentParser(DTD.getDTD("html32")).parse(new StringReader(html), cb, false);
        return cb.log;
    }

    static void testDeepFullSequence() throws Exception {
        // THE anchor table: fixed markup exercising comments, implied structure,
        // entity decoding, attributes, simple tags, nesting, positions and eol
        List<String> got = parseSeq(
            "<!--c--><html><head><title>T&amp;t</title></head>"
            + "<body><p align=CENTER class=\"x&amp;y\">A&amp;B&#65;&unknown;<br><b>i</b></p></body></html>");
        List<String> expected = java.util.Arrays.asList(
            "C:c@0",
            "S:html@8", "S:head@14", "S:title@20", "T:T&t@27", "E:title@34", "E:head@42",
            "S:body@49",
            "!:invalid.tagatt class p ?@87",
            "S:p@55[align=center][class=x&y]",
            "T:A&BA&unknown;@87",
            "P:br@108",
            "S:b@112", "T:i@115", "E:b@116",
            "E:p@120", "E:body@124", "E:html@131",
            "$:\\n");
        check(got.equals(expected), "full sequence table, got:\n" + got);

        // implied document skeleton for bare paragraph content
        List<String> bare = parseSeq("<p>x<p>y");
        List<String> bareExpected = java.util.Arrays.asList(
            "S:html@0[_implied_=true]", "S:head@0[_implied_=true]", "E:head@0",
            "S:body@0[_implied_=true]",
            "S:p@0", "T:x@3", "E:p@4",
            "S:p@4", "T:y@7", "E:p@7",
            "E:body@7", "E:html@7", "$:\\n");
        check(bare.equals(bareExpected), "implied skeleton + implicit </p>, got:\n" + bare);

        // stray end tag: an error is reported, no implied structure is started
        List<String> stray = parseSeq("</p>");
        check(stray.equals(java.util.Arrays.asList("!:end.extra.tag p ? ?@4", "$:\\n")),
              "stray end tag, got " + stray);

        // misnesting recovery: missing </i> then unmatched </i>
        List<String> misnest = parseSeq("<b><i>x</b></i>");
        List<String> misnestExpected = java.util.Arrays.asList(
            "S:html@0[_implied_=true]", "S:head@0[_implied_=true]", "E:head@0",
            "S:body@0[_implied_=true]",
            "S:b@0", "S:i@3", "T:x@6",
            "!:end.missing i ? ?@12", "E:i@7", "E:b@7",
            "!:unmatched.endtag i ? ?@15",
            "E:body@14", "E:html@14", "$:\\n");
        check(misnest.equals(misnestExpected), "misnesting recovery, got:\n" + misnest);

        // unrecognized tag: error, tag ignored as start, delivered as simple tag
        // on the closing side with endtag=true
        List<String> unknown = parseSeq("<foobar>a</foobar>");
        List<String> unknownExpected = java.util.Arrays.asList(
            "!:tag.unrecognized  foobar ? ?@8",
            "S:html@8[_implied_=true]", "S:head@8[_implied_=true]", "E:head@8",
            "S:body@8[_implied_=true]",
            "!:start.missing body ? ?@9",
            "!:end.unrecognized foobar ? ?@18",
            "T:a@8", "P:foobar@9[endtag=true]",
            "E:body@17", "E:html@17", "$:\\n");
        check(unknown.equals(unknownExpected), "unknown tag handling, got:\n" + unknown);

        // doctype declaration is silently skipped; the following content still
        // gets its implied skeleton
        List<String> doctype = parseSeq("<!DOCTYPE html><p>x");
        List<String> doctypeExpected = java.util.Arrays.asList(
            "S:html@15[_implied_=true]", "S:head@15[_implied_=true]", "E:head@15",
            "S:body@15[_implied_=true]", "S:p@15", "T:x@18", "E:p@18",
            "E:body@18", "E:html@18", "$:\\n");
        check(doctype.equals(doctypeExpected), "doctype silent, skeleton kept, got:\n" + doctype);
    }

    static void testDeepAttributes() throws Exception {
        // attribute names are folded to lower case; non-enumerated values keep case
        List<String> a1a = parseSeq("<a name=MiXeD>t</a>");
        check(hasEntry(a1a, "S:a@0[name=MiXeD]"), "attribute name lowercased, value kept, got " + a1a);
        List<String> a1b = parseSeq("<a href=AbC.DEF>t</a>");
        check(hasEntry(a1b, "S:a@0[href=AbC.DEF]"), "unquoted URL value case preserved, got " + a1b);
        // enumerated values (align, input type) are folded to lower case
        List<String> a2 = parseSeq("<p align=LEFT>t");
        check(hasEntry(a2, "S:p@0[align=left]"), "enumerated value lowercased, got " + a2);
        // boolean attribute renders as name=value with the name as value
        List<String> a3 = parseSeq("<input type=checkbox CHECKED NAME=Foo>");
        check(hasEntry(a3, "P:input@0[type=checkbox][checked=checked][name=Foo]"),
              "boolean attribute checked=checked, got " + a3);
        // invalid enumerated value additionally reports an error
        List<String> a4 = parseSeq("<p align=MIXED>t");
        boolean errSeen = false;
        for (String s : a4) if (s.startsWith("!:invalid.tagattval")) errSeen = true;
        check(errSeen, "invalid enumerated value reported");
        // values are entity-decoded inside attributes
        List<String> a5 = parseSeq("<p class=\"x&amp;y\">t");
        check(hasEntry(a5, "S:p@0[class=x&y]"), "attribute entity decoding, got " + a5);
        // the AttributeSet handed to the callback is mutable
        final boolean[] mutated = new boolean[1];
        new DocumentParser(DTD.getDTD("html32")).parse(new StringReader("<p>z"), new HTMLEditorKit.ParserCallback() {
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                int before = a.getAttributeCount();
                a.addAttribute("probe-key", "probe-value");
                mutated[0] = a.getAttributeCount() == before + 1
                    && "probe-value".equals(a.getAttribute("probe-key"));
            }
        }, false);
        check(mutated[0], "callback AttributeSet is mutable");
    }

    static boolean hasEntry(List<String> log, String entry) {
        for (String s : log) if (s.equals(entry)) return true;
        return false;
    }

    static void testDeepTextAndEntities() throws Exception {
        // entities: named, decimal numeric, unknown stays literal
        List<String> t1 = parseSeq( "<p>&amp;&lt;&gt;&#65;&unknown;");
        StringBuilder text = new StringBuilder();
        for (String s : t1) if (s.startsWith("T:")) text.append(s.substring(2, s.indexOf('@', 2)));
        check("&<>A&unknown;".equals(text.toString()), "entity decoding, got " + text);
        // nbsp decodes to U+00A0
        List<String> t2 = parseSeq("<p>a&nbsp;b");
        String nbspText = null;
        for (String s : t2) if (s.startsWith("T:")) nbspText = s.substring(2, s.indexOf('@', 2));
        check(nbspText != null && nbspText.length() == 3 && nbspText.charAt(1) == '\u00A0',
              "nbsp decodes to U+00A0, got " + (nbspText == null ? "null" : "[" + nbspText + "]"));
        // whitespace between tags collapses to a single space delivered as text
        List<String> t3 = parseSeq("<b>  <i>t</i>  </b>");
        check(hasEntry(t3, "T:t@8") && hasEntry(t3, "T: @15"),
              "collapsed inter-tag whitespace delivered, got " + t3);
        // line separators collapse inside running text; eol reports the first one
        List<String> t4 = parseSeq("<p>a\r\nb\rc</p>");
        check(hasEntry(t4, "T:a b c@3") && hasEntry(t4, "$:\\r\\n"),
              "CRLF collapsed in text, eol CRLF, got " + t4);
        // pre keeps whitespace and newlines verbatim
        List<String> t5 = parseSeq("<pre> a\n b </pre>");
        boolean preText = false;
        for (String s : t5) if (s.startsWith("T: a\n b ")) preText = true;
        check(preText, "pre whitespace preserved, got " + t5);
        // script content is routed to handleComment, not handleText
        List<String> t6 = parseSeq("<script>if (a < b) { x(); }</script>");
        boolean commentSeen = false;
        boolean scriptTextSeen = false;
        for (String s : t6) {
            if (s.startsWith("C:if (a < b)")) commentSeen = true;
            if (s.startsWith("T:")) scriptTextSeen = true;
        }
        check(commentSeen && !scriptTextSeen, "script content via handleComment, got " + t6);
        // title text is delivered through handleText (title is not body text)
        List<String> t7 = parseSeq("<title>Hello</title>");
        check(hasEntry(t7, "T:Hello@7"), "title text via handleText, got " + t7);
        // two titles: the second (outside the implied head) still delivers text
        // but is flagged with a tag.unexpected error
        List<String> t8 = parseSeq("<title>A</title><title>B</title>");
        check(hasEntry(t8, "T:A@7") && hasEntry(t8, "T:B@23"), "two titles, got " + t8);
        boolean secondTitleError = false;
        for (String s : t8) if (s.startsWith("!:tag.unexpected title")) secondTitleError = true;
        check(secondTitleError, "second title flagged tag.unexpected, got " + t8);
    }

    static void testDeepCharSetAndExceptions() throws Exception {
        // a charset <meta> inside <head> makes parse throw ChangedCharSetException
        String metaHtml = "<html><head><meta http-equiv=\"charset\" content=\"utf-8\"></head><body>x</body></html>";
        try {
            DocumentParser dp0 = new DocumentParser(DTD.getDTD("html32"));
            dp0.parse(new StringReader(metaHtml), new HTMLEditorKit.ParserCallback(), false);
            check(false, "expected ChangedCharSetException for meta charset");
        } catch (javax.swing.text.ChangedCharSetException e) {
            check("utf-8".equals(e.getCharSetSpec()), "ChangedCharSetException spec utf-8, got " + e.getCharSetSpec());
            passed++;
        }
        // same input with ignoreCharSet=true parses through
        SeqCB cb = new SeqCB();
        new DocumentParser(DTD.getDTD("html32")).parse(new StringReader(metaHtml), cb, true);
        check(hasText(cb.log, "x"), "ignoreCharSet=true no exception, got " + cb.log);
        // plain text/html content-type does not throw
        new DocumentParser(DTD.getDTD("html32")).parse(
            new StringReader("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\"></head><body>y</body></html>"),
            new SeqCB(), false);
        passed++;
        // a RuntimeException thrown by a callback is caught by the parse loop:
        // it does NOT propagate; it is reported through handleError
        final RuntimeException mine = new RuntimeException("document-parser-marker");
        SeqCB rt = new SeqCB();
        new DocumentParser(DTD.getDTD("html32")).parse(new StringReader("<p>x"), new HTMLEditorKit.ParserCallback() {
            public void handleText(char[] d, int p) { throw mine; }
            public void handleError(String m, int p) { rt.handleError(m, p); }
        }, false);
        boolean rtReported = false;
        for (String s : rt.log) if (s.startsWith("!:exception java.lang.RuntimeException")) rtReported = true;
        check(rtReported, "callback RuntimeException reported via handleError, got " + rt.log);
        // IOException from the reader propagates
        java.io.Reader bad = new StringReader("<p>x") {
            public int read(char[] cbuf) throws java.io.IOException { throw new java.io.IOException("io-marker"); }
        };
        try {
            new DocumentParser(DTD.getDTD("html32")).parse(bad, new HTMLEditorKit.ParserCallback(), false);
            check(false, "expected IOException passthrough");
        } catch (java.io.IOException e) {
            check("io-marker".equals(e.getMessage()), "reader IOException propagated");
        }
        // null reader: the base parser catches the internal NPE and reports it
        // through handleError instead of propagating it
        SeqCB nullReader = new SeqCB();
        new DocumentParser(DTD.getDTD("html32")).parse(null, nullReader, false);
        boolean nullReaderError = false;
        for (String s : nullReader.log) if (s.startsWith("!:exception java.lang.NullPointerException")) nullReaderError = true;
        check(nullReaderError, "null reader reported via handleError, got " + nullReader.log);
        check(nullReader.log.contains("$:\\n"), "null reader still ends with eol event");
        // null callback: the NPE from the callback field propagates
        try {
            new DocumentParser(DTD.getDTD("html32")).parse(new StringReader("x"), null, false);
            check(false, "expected NPE for null callback");
        } catch (NullPointerException expected) { passed++; }
        // the same DocumentParser instance is reusable: content state resets,
        // but the position counter keeps accumulating across parses
        DocumentParser reused = new DocumentParser(DTD.getDTD("html32"));
        SeqCB r1 = new SeqCB();
        reused.parse(new StringReader("<p>one"), r1, false);
        SeqCB r2 = new SeqCB();
        reused.parse(new StringReader("<p>two"), r2, false);
        boolean one = false, two = false;
        for (String s : r1.log) if (s.startsWith("T:one")) one = true;
        for (String s : r2.log) if (s.startsWith("T:two")) two = true;
        check(one && two, "DocumentParser reusable across parses");
        // positions are NOT reset: the second parse continues past the first
        int minSecond = Integer.MAX_VALUE, maxFirst = Integer.MIN_VALUE;
        for (String s : r2.log) {
            int at = s.lastIndexOf('@');
            if (at > 0) {
                int end = at + 1;
                while (end < s.length() && Character.isDigit(s.charAt(end))) end++;
                if (end > at + 1) minSecond = Math.min(minSecond, Integer.parseInt(s.substring(at + 1, end)));
            }
        }
        for (String s : r1.log) {
            int at = s.lastIndexOf('@');
            if (at > 0) {
                int end = at + 1;
                while (end < s.length() && Character.isDigit(s.charAt(end))) end++;
                if (end > at + 1) maxFirst = Math.max(maxFirst, Integer.parseInt(s.substring(at + 1, end)));
            }
        }
        check(minSecond > maxFirst, "reused parser continues its position counter (" + minSecond + " > " + maxFirst + ")");
    }

    static boolean hasText(List<String> log, String text) {
        for (String s : log) if (s.startsWith("T:" + text + "@")) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testLegacyStructure();
        testWhiteBox();
        testBlackBox();
        testDeepFullSequence();
        testDeepAttributes();
        testDeepTextAndEntities();
        testDeepCharSetAndExceptions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
