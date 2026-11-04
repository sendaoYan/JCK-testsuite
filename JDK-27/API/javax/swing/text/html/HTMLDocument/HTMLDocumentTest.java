/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument conformance: white-box structure + black-box behavior.
 * @run main HTMLDocumentTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class HTMLDocumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultStyledDocument"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static HTMLDocument parse(String html) throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), doc, 0);
        return doc;
    }

    static void testBlackBox() throws Exception {
        HTMLDocument d1 = new HTMLDocument();
        check(d1 != null, "new HTMLDocument()");
        check(new HTMLDocument(new StyleSheet()) != null, "new HTMLDocument(StyleSheet)");
        check(new HTMLDocument(new javax.swing.text.GapContent(), new StyleSheet()) != null,
                "new HTMLDocument(Content, StyleSheet)");

        HTMLDocument doc = parse("<html><body><p>hello</p></body></html>");
        check(doc.getLength() == 6, "parsed length");
        String text = doc.getText(0, doc.getLength());
        check(text.contains("hello"), "parsed text contains hello");

        Element root = doc.getDefaultRootElement();
        check(root != null && "html".equals(root.getName()), "root name html");
        check(root instanceof HTMLDocument.BlockElement, "root is BlockElement");
        check(root.getElementCount() == 2, "root has head+body children");

        Element para = doc.getParagraphElement(1);
        check("p".equals(para.getName()), "paragraph element name p");
        check(para instanceof HTMLDocument.BlockElement, "paragraph is BlockElement");

        HTMLDocument doc2 = parse("<html><body><p id=\"p1\">hello</p></body></html>");
        Element p1 = doc2.getElement("p1");
        check(p1 != null && "p".equals(p1.getName()), "getElement by id");
        Element found = doc2.getElement(doc2.getDefaultRootElement(), HTML.Attribute.ID, "p1");
        check(found == p1, "getElement(root, ID, value)");
        check(doc2.getElement("no-such-id") == null, "getElement missing id null");
        check(doc2.getElement(null) == null, "getElement(null) null");

        Element bodyByName = doc2.getElement(doc2.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY);
        check(bodyByName != null && "body".equals(bodyByName.getName()), "getElement by name attribute");

        Element bodyElem = doc2.getElement(doc2.getDefaultRootElement(),
                StyleConstants.NameAttribute, HTML.Tag.BODY);
        doc2.insertAfterStart(bodyElem, "<b>B</b>");
        check(doc2.getText(0, doc2.getLength()).contains("Bhello"), "insertAfterStart");
        doc2.insertBeforeEnd(doc2.getElement(doc2.getDefaultRootElement(),
                StyleConstants.NameAttribute, HTML.Tag.BODY), "<p>X</p>");
        check(doc2.getText(0, doc2.getLength()).contains("Bhello"), "insertBeforeEnd keeps prior content");
        check(doc2.getText(0, doc2.getLength()).contains("X"), "insertBeforeEnd appends block");

        check(doc.getTokenThreshold() == 100, "default token threshold");
        doc.setTokenThreshold(10);
        check(doc.getTokenThreshold() == 10, "setTokenThreshold round-trip");

        check(doc.getPreservesUnknownTags() == true, "default preservesUnknownTags true");
        doc.setPreservesUnknownTags(false);
        check(doc.getPreservesUnknownTags() == false, "setPreservesUnknownTags round-trip");

        check(doc.getBase() == null, "default base null");
        URL base = new URL("http://example.com/");
        doc.setBase(base);
        check(base.equals(doc.getBase()), "setBase round-trip");

        check(doc.getStyleSheet() instanceof StyleSheet, "getStyleSheet");
        check(doc.getParser() != null, "getParser non-null");
        check(doc.getReader(1) instanceof HTMLDocument.HTMLReader, "getReader returns HTMLReader");
    }

    static void testTreeShape() throws Exception {
        HTMLDocument doc = parse(DOC_HTML);
        check(doc.getLength() == 21, "parsed length 21");
        String text = doc.getText(0, doc.getLength());
        check(text.equals("  \nHello & bye\nSecond"), "exact parsed text with entity decoded");

        Element root = doc.getDefaultRootElement();
        check("html".equals(root.getName()), "root element name html");
        check(root instanceof HTMLDocument.BlockElement, "root is a BlockElement");
        check(root.getElementCount() == 2, "root has head and body");

        Element head = root.getElement(0);
        check("head".equals(head.getName()), "first child is head");
        check(head.getStartOffset() == 0 && head.getEndOffset() == 3, "head span [0,3)");
        Element body = root.getElement(1);
        check("body".equals(body.getName()), "second child is body");
        check(body.getStartOffset() == 3 && body.getEndOffset() == 22, "body span [3,22)");

        // head keeps an implied paragraph holding title begin/end and a CR run
        Element implied = head.getElement(0);
        check("p-implied".equals(implied.getName()), "head paragraph is p-implied");
        check("title".equals(implied.getElement(0).getName()), "title start element");
        check(implied.getElement(0).getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.TITLE,
              "title element name attribute");
        check("title".equals(implied.getElement(1).getName()), "title end element");
        check("true".equals(implied.getElement(1).getAttributes().getAttribute(HTML.Attribute.ENDTAG)),
              "title end element carries endtag=true");
        check(implied.getElement(1).getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.TITLE,
              "title end element named by tag");
        check("content".equals(implied.getElement(2).getName()), "head ends with content run");
        check(Boolean.TRUE.equals(implied.getElement(2).getAttributes().getAttribute("CR")),
              "head content run carries CR=true");

        // body paragraphs
        check(body.getElementCount() == 2, "body has two paragraphs");
        Element p1 = body.getElement(0);
        check("p".equals(p1.getName()), "first paragraph named p");
        check(p1.getStartOffset() == 3 && p1.getEndOffset() == 15, "p1 span [3,15)");
        check("content".equals(p1.getElement(0).getName()), "p1 text run");
        check(p1.getElement(0).getStartOffset() == 3 && p1.getElement(0).getEndOffset() == 14,
              "p1 text run spans the 11 decoded characters");
        check(Boolean.TRUE.equals(p1.getElement(1).getAttributes().getAttribute("CR")), "p1 ends with CR run");
        Element p2 = body.getElement(1);
        check(p2.getElement(0).getEndOffset() - p2.getElement(0).getStartOffset() == 6, "p2 holds 'Second'");

        // paragraph lookup by offset and by id
        check(doc.getParagraphElement(4) == p1, "getParagraphElement resolves p1");
        check(doc.getElement("p1") == p1, "getElement(id) finds p1");
        check(doc.getElement("no-such-id") == null, "getElement(unknown id) null");
        check(doc.getElement(null) == null, "getElement(null) null");
        check(doc.getElement(root, HTML.Attribute.ID, "p1") == p1, "getElement(root, ID, value)");
        check(doc.getElement(root, HTML.Attribute.ID, "zzz") == null, "getElement(root, unknown ID) null");
        check(doc.getElement(root, StyleConstants.NameAttribute, HTML.Tag.BODY) == body,
              "getElement(root, NameAttribute, Tag.BODY)");
    }

    static void testIterator() throws Exception {
        HTMLDocument doc = parse(DOC_HTML);
        check(doc.getIterator(HTML.Tag.P) == null, "getIterator(block tag) returns null");
        HTMLDocument.Iterator notPresent = doc.getIterator(HTML.Tag.HR);
        check(notPresent != null, "getIterator(absent leaf tag) still returns iterator");
        check(!notPresent.isValid(), "iterator over absent tag is not valid");

        HTMLDocument di = parse("<html><body><p>text <img src='a.png' alt='A'> mid "
                + "<img src='b.png' alt='B' width=5> end</p></body></html>");
        HTMLDocument.Iterator it = di.getIterator(HTML.Tag.IMG);
        check(it != null, "getIterator(IMG)");
        check(it.isValid(), "fresh iterator is valid");

        int count = 0;
        String[] srcs = new String[2];
        int[] starts = new int[2];
        while (it.isValid()) {
            srcs[count] = (String) it.getAttributes().getAttribute(HTML.Attribute.SRC);
            starts[count] = it.getStartOffset();
            check(it.getEndOffset() == it.getStartOffset() + 1, "IMG occurrence spans one position");
            check(it.getTag() == HTML.Tag.IMG, "iterator tag is IMG");
            it.next();
            count++;
        }
        check(count == 2, "iterator found both images");
        check("a.png".equals(srcs[0]) && "b.png".equals(srcs[1]), "iterator visits in document order");
        check(starts[0] == 6 && starts[1] == 12, "iterator offsets 6 and 12");
        boolean nextOk = true;
        try { it.next(); } catch (Throwable t) { nextOk = false; }
        check(nextOk, "next() after exhaustion is tolerated");
    }

    static void testReader() {
        HTMLDocument empty = new HTMLDocument();
        check(empty.getReader(0) instanceof HTMLDocument.HTMLReader, "empty doc getReader(0)");
        check(empty.getReader(0, 1, 2, HTML.Tag.TD) instanceof HTMLDocument.HTMLReader,
              "pushBack reader variant");

        HTMLDocument doc;
        try { doc = parse(DOC_HTML); }
        catch (Exception e) { throw new AssertionError(e); }
        check(doc.getReader(4) instanceof HTMLDocument.HTMLReader, "reader inside body accepted");
        check(doc.getReader(1000) instanceof HTMLDocument.HTMLReader, "reader past end clamps into body");

        RuntimeException re1 = null;
        try { doc.getReader(0); } catch (RuntimeException e) { re1 = e; }
        check(re1 != null, "getReader(0) on non-empty doc throws RuntimeException");
        check(re1.getMessage() != null && re1.getMessage().startsWith("Must insert new content into body element-"),
              "reader rejection message: " + re1.getMessage());

        RuntimeException re2 = null;
        try { doc.getReader(-5); } catch (RuntimeException e) { re2 = e; }
        check(re2 != null, "getReader(negative) on non-empty doc throws RuntimeException");
    }

    static void testInserts() throws Exception {
        HTMLDocument d3 = parse("<html><body><p id='target'>AB</p></body></html>");
        Element t3 = d3.getElement("target");
        d3.insertAfterStart(t3, "<b>X</b>");
        d3.insertBeforeEnd(t3, "<b>Y</b>");
        d3.insertBeforeStart(t3, "<b>Z</b>");
        d3.insertAfterEnd(t3, "<b>W</b>");
        String t = d3.getText(0, d3.getLength());
        check(t.equals("\nZXABY\n"), "four inserts produce \\nZXABY\\n (after-end inline content dropped)");
        List<String> names = new ArrayList<String>();
        collectNames(d3.getDefaultRootElement(), names);
        check(containsName(names, "p"), "paragraph survived the inserts");
        check(countName(names, "content") >= 4, "inline inserts created separate content runs");

        HTMLDocument d4 = parse("<html><body><p id='t'>Old</p><p>Keep</p></body></html>");
        d4.setInnerHTML(d4.getElement("t"), "<b>New</b>");
        String t4 = d4.getText(0, d4.getLength());
        check(t4.contains("New") && !t4.contains("Old") && t4.contains("Keep"),
              "setInnerHTML replaces inner content, siblings kept");
        check(d4.getElement("t") != null, "element identity kept after setInnerHTML");

        HTMLDocument d5 = parse("<html><body><p id='t'>Old</p><p>Keep</p></body></html>");
        d5.setOuterHTML(d5.getElement("t"), "<h1>Replaced</h1>");
        String t5 = d5.getText(0, d5.getLength());
        check(t5.contains("Replaced") && !t5.contains("Old") && t5.contains("Keep"),
              "setOuterHTML replaces the element itself");
        Element body = d5.getDefaultRootElement().getElement(1);
        check("h1".equals(body.getElement(0).getName()), "setOuterHTML installed an h1 block");

        // unknown tags preserved or dropped per preservesUnknownTags
        HTMLDocument preserved = parse("<html><body><customtag>inner</customtag></body></html>");
        List<String> pnames = new ArrayList<String>();
        collectNames(preserved.getDefaultRootElement(), pnames);
        check(countName(pnames, "customtag") == 2, "unknown tags preserved by default (open+close)");
        check(preserved.getText(0, preserved.getLength()).contains("inner"), "inner text kept");
        HTMLEditorKit droppedKit = new HTMLEditorKit();
        HTMLDocument dropped = (HTMLDocument) droppedKit.createDefaultDocument();
        dropped.setPreservesUnknownTags(false);
        droppedKit.read(new StringReader("<html><body><customtag>inner</customtag></body></html>"), dropped, 0);
        List<String> dnames = new ArrayList<String>();
        collectNames(dropped.getDefaultRootElement(), dnames);
        check(countName(dnames, "customtag") == 0, "unknown tags dropped when disabled");
        check(dropped.getText(0, dropped.getLength()).contains("inner"), "text kept when tags dropped");
    }

    static void testNegatives() throws Exception {
        HTMLDocument d6 = parse("<html><body><p>one</p></body></html>");
        boolean ok = true;
        try { d6.insertAfterStart(null, "<b>a</b>"); } catch (Throwable t) { ok = false; }
        check(ok, "insertAfterStart(null, html) tolerated");
        ok = true;
        try { d6.insertAfterStart(d6.getDefaultRootElement(), null); } catch (Throwable t) { ok = false; }
        check(ok, "insertAfterStart(elem, null) tolerated");
        ok = true;
        try { d6.insertAfterStart(d6.getDefaultRootElement(), "plain text"); } catch (Throwable t) { ok = false; }
        check(ok, "insertAfterStart(root, tagless text) tolerated");
        ok = true;
        try { d6.setInnerHTML(null, "<b>a</b>"); } catch (Throwable t) { ok = false; }
        check(ok, "setInnerHTML(null, html) tolerated");
        ok = true;
        try { d6.setOuterHTML(d6.getDefaultRootElement().getElement(0).getElement(0), "<b>a</b>"); }
        catch (Throwable t) { ok = false; }
        check(ok, "setOuterHTML(leaf element) tolerated");

    }

    static void testFrameProcessing() throws Exception {
        HTMLDocument d6 = parse("<html><body><p>one</p></body></html>");
        HTMLFrameHyperlinkEvent fe = new HTMLFrameHyperlinkEvent(d6,
                HyperlinkEvent.EventType.ACTIVATED,
                new URL("http://x.example/f.html"), "sourceFrame", null, null, "targetFrame");
        check("targetFrame".equals(fe.getTarget()), "event target frame");
        boolean ok = true;
        try { d6.processHTMLFrameHyperlinkEvent(fe); } catch (Throwable t) { ok = false; }
        check(ok, "processHTMLFrameHyperlinkEvent(no matching frame) tolerated");
        check(d6.getLength() == 4, "document unchanged when no frame matches");

        HTMLFrameHyperlinkEvent nullUrl = new HTMLFrameHyperlinkEvent(d6,
                HyperlinkEvent.EventType.ACTIVATED, null, "sf", null, null, "tf");
        boolean npe = false;
        try { d6.processHTMLFrameHyperlinkEvent(nullUrl); } catch (NullPointerException e) { npe = true; }
        check(npe, "processHTMLFrameHyperlinkEvent(null url) NPE");

        HTMLDocument fd = parse("<html><frameset cols='50%,50%'>"
                + "<frame src='a.html' name='f1'><frame src='b.html' name='f2'></frameset></html>");
        Element frameEl = fd.getElement(fd.getDefaultRootElement(), HTML.Attribute.SRC, "a.html");
        check(frameEl != null, "frame element located by SRC");
        final int[] changed = { 0 };
        final int[] undos = { 0 };
        fd.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { }
            public void removeUpdate(DocumentEvent e) { }
            public void changedUpdate(DocumentEvent e) { changed[0]++; }
        });
        fd.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) { undos[0]++; }
        });
        HTMLFrameHyperlinkEvent update = new HTMLFrameHyperlinkEvent(fd,
                HyperlinkEvent.EventType.ACTIVATED,
                new URL("http://x.example/new.html"), "sf", null, null, "f1");
        fd.processHTMLFrameHyperlinkEvent(update);
        check("http://x.example/new.html".equals(
                  String.valueOf(frameEl.getAttributes().getAttribute(HTML.Attribute.SRC))),
              "matching frame SRC attribute updated");
        check(changed[0] == 1, "one attribute-change event fired");
        check(undos[0] == 1, "one undoable edit fired");
    }


    static void testLazyInit() throws Exception {
        HTMLDocument fresh = new HTMLDocument();
        check("AdditionalComments".equals(HTMLDocument.AdditionalComments), "AdditionalComments property name");
        check(fresh.getParser() == null, "parser lazily unset on a fresh document");
        check(fresh.getTokenThreshold() == Integer.MAX_VALUE, "fresh document threshold unbounded");
        check(new HTMLDocument(new StyleSheet()).getStyleSheet() != null, "ctor sheet kept");
        HTMLDocument parsed = parse("<html><body><p>x</p></body></html>");
        check("javax.swing.text.html.parser.ParserDelegator".equals(parsed.getParser().getClass().getName()),
              "default parser is ParserDelegator once the reader ran");
        check(parsed.getTokenThreshold() == 100, "kit-read document threshold 100");
    }


    static final String DOC_HTML =
            "<html><head><title>TT</title></head><body>"
            + "<p id='p1'>Hello &amp; bye</p><p>Second</p></body></html>";

    static void collectNames(Element e, java.util.List<String> out) {
        out.add(e.getName());
        for (int i = 0; i < e.getElementCount(); i++) collectNames(e.getElement(i), out);
    }

    static int countName(java.util.List<String> names, String n) {
        int c = 0;
        for (String s : names) if (n.equals(s)) c++;
        return c;
    }

    static boolean containsName(java.util.List<String> names, String n) {
        for (String s : names) if (n.equals(s)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLazyInit();
        testTreeShape();
        testIterator();
        testReader();
        testInserts();
        testNegatives();
        testFrameProcessing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
