/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.HTMLReader conformance: white-box structure + black-box behavior.
 * @run main HTMLReaderTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class HTMLReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$HTMLReader");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLEditorKit$ParserCallback"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        HTMLDocument doc = new HTMLDocument();
        HTMLDocument.HTMLReader reader = doc.new HTMLReader(0);
        check(reader != null, "construct reader");
        check(reader instanceof HTMLEditorKit.ParserCallback, "reader instanceof ParserCallback");

        MutableAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
        reader.handleStartTag(HTML.Tag.P, attrs, 0);
        reader.handleText("hello".toCharArray(), 0);
        reader.handleEndTag(HTML.Tag.P, 0);
        reader.flush();
        check(doc.getText(0, doc.getLength()).contains("hello"), "reader built content");

        HTMLDocument.HTMLReader reader2 = doc.new HTMLReader(0, 0, 0, HTML.Tag.BODY);
        check(reader2 != null, "construct 5-arg reader");
        reader2.handleComment(new char[]{'x'}, 0);
        reader2.handleSimpleTag(HTML.Tag.BR, new SimpleAttributeSet(), 0);
        reader2.handleEndOfLineString(System.lineSeparator());

        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc2 = (HTMLDocument) kit.createDefaultDocument();
        check(doc2.getReader(0) instanceof HTMLDocument.HTMLReader, "getReader returns HTMLReader");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
