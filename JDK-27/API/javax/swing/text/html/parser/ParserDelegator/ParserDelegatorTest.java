/*
 * @test
 * @summary javax.swing.text.html.parser.ParserDelegator conformance: white-box structure + black-box behavior.
 * @run main ParserDelegatorTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class ParserDelegatorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.ParserDelegator");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLEditorKit$Parser"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
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
        ParserDelegator pd = new ParserDelegator();

        CB cb = new CB();
        pd.parse(new StringReader("<p>Hello</p>"), cb, false);
        check(cb.starts.contains("html") && cb.starts.contains("head") && cb.starts.contains("body") && cb.starts.contains("p"), "start tags html/head/body/p");
        check(cb.ends.contains("p") && cb.ends.contains("body") && cb.ends.contains("html"), "end tags");
        check(cb.texts.contains("Hello"), "text Hello");
        check("\n".equals(cb.eol), "end-of-line string newline");

        CB cb2 = new CB();
        pd.parse(new StringReader("<br>foo"), cb2, false);
        check(cb2.simples.contains("br"), "simple tag br");
        check(cb2.texts.contains("foo"), "text foo");

        CB cb3 = new CB();
        pd.parse(new StringReader("<!-- note -->"), cb3, false);
        check(cb3.comments.size() == 1 && cb3.comments.get(0).contains("note"), "comment note");

        CB cb4 = new CB();
        pd.parse(new StringReader("<p>Hi</p>"), cb4, true);
        check(cb4.starts.contains("p") && cb4.texts.contains("Hi"), "ignoreCharSet=true still parses");

        // each parse() call starts a fresh DocumentParser; results are independent
        CB cb5 = new CB();
        pd.parse(new StringReader("<i>em</i>"), cb5, false);
        check(cb5.starts.contains("i") && cb5.texts.contains("em"), "fresh parse independent");
    }

    // ---- Part 3: deep contract coverage ----

    static class SeqCB extends HTMLEditorKit.ParserCallback {
        final List<String> log = new ArrayList<String>();
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            log.add("S:" + t + "@" + pos + (Boolean.TRUE.equals(a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED)) ? "*" : ""));
        }
        public void handleEndTag(HTML.Tag t, int pos) { log.add("E:" + t + "@" + pos); }
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) { log.add("P:" + t + "@" + pos); }
        public void handleText(char[] data, int pos) { log.add("T:" + new String(data) + "@" + pos); }
        public void handleComment(char[] data, int pos) { log.add("C:" + new String(data) + "@" + pos); }
        public void handleError(String errorMsg, int pos) { log.add("!:" + errorMsg + "@" + pos); }
        public void handleEndOfLineString(String eol) { log.add("$:" + eol.replace("\n", "\\n")); }
    }

    static void testDeepSequenceTable() throws Exception {
        ParserDelegator pd = new ParserDelegator();
        // whole-table callback contract with positions; * marks implied tags
        SeqCB cb = new SeqCB();
        pd.parse(new StringReader("<p>zz</p>"), cb, false);
        List<String> expected = java.util.Arrays.asList(
            "S:html@0*", "S:head@0*", "E:head@0", "S:body@0*",
            "S:p@0", "T:zz@3", "E:p@5", "E:body@8", "E:html@8", "$:\\n");
        check(cb.log.equals(expected), "full sequence table, got " + cb.log);
        // empty input produces only the default end-of-line string event
        SeqCB empty = new SeqCB();
        pd.parse(new StringReader(""), empty, false);
        check(empty.log.equals(java.util.Arrays.asList("$:\\n")), "empty input -> only eol, got " + empty.log);
        // doctype is skipped silently; skeleton still implied
        SeqCB doc = new SeqCB();
        pd.parse(new StringReader("<!DOCTYPE html><p>x"), doc, false);
        check(doc.log.size() == 10 && doc.log.get(0).equals("S:html@15*") && doc.log.get(4).equals("S:p@15")
              && doc.log.contains("T:x@18") && doc.log.get(9).equals("$:\\n"),
              "doctype skipped, skeleton kept, got " + doc.log);
        // the default DTD bootstrap made html32 available through DTD.getDTD
        check(javax.swing.text.html.parser.DTD.getDTD("html32").getName().equals("html32"),
              "html32 registered by the delegator");
    }

    static void testDeepExceptionContracts() throws Exception {
        final ParserDelegator pd = new ParserDelegator();
        // null reader: no propagation; the internal NPE is reported via handleError
        SeqCB nullReader = new SeqCB();
        pd.parse(null, nullReader, false);
        boolean nullReaderError = false;
        for (String s : nullReader.log) if (s.startsWith("!:exception java.lang.NullPointerException")) nullReaderError = true;
        check(nullReaderError, "null reader reported via handleError, got " + nullReader.log);
        // null callback
        try {
            pd.parse(new StringReader("x"), null, false);
            check(false, "expected NPE for null callback");
        } catch (NullPointerException expected) { passed++; }
        // a RuntimeException thrown by a callback is caught by the parse loop:
        // it does NOT propagate; it is reported through handleError
        final RuntimeException mine = new RuntimeException("delegator-marker");
        SeqCB rt = new SeqCB();
        pd.parse(new StringReader("<p>x"), new HTMLEditorKit.ParserCallback() {
            public void handleText(char[] d, int p) { throw mine; }
            public void handleError(String m, int p) { rt.handleError(m, p); }
        }, false);
        boolean rtReported = false;
        for (String s : rt.log) if (s.startsWith("!:exception java.lang.RuntimeException")) rtReported = true;
        check(rtReported, "callback RuntimeException reported via handleError, got " + rt.log);
        // IOException from the reader surfaces unchanged
        java.io.Reader bad = new StringReader("<p>x") {
            public int read(char[] cbuf) throws java.io.IOException { throw new java.io.IOException("pd-io"); }
        };
        try {
            pd.parse(bad, new SeqCB(), false);
            check(false, "expected IOException passthrough");
        } catch (java.io.IOException e) {
            check("pd-io".equals(e.getMessage()), "reader IOException propagated");
        }
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ParserDelegator.class).getSerialVersionUID()
              == 1832828210669556215L, "serialVersionUID");
        ParserDelegator pd = new ParserDelegator();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(pd);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray()));
        ParserDelegator back = (ParserDelegator) ois.readObject();
        ois.close();
        check(back != pd, "deserialized instance is distinct");
        SeqCB cb = new SeqCB();
        back.parse(new StringReader("<p>after"), cb, false);
        check(cb.log.contains("T:after@3"), "deserialized delegator still parses, got " + cb.log);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepSequenceTable();
        testDeepExceptionContracts();
        testDeepSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
