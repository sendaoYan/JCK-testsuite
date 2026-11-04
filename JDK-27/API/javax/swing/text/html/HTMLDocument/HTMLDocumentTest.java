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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
