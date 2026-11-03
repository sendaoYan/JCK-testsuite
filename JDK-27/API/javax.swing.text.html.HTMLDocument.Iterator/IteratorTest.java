/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.Iterator conformance: white-box structure + black-box behavior.
 * @run main IteratorTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class IteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$Iterator");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static HTMLDocument parse(String html) throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), doc, 0);
        return doc;
    }

    static void testBlackBox() throws Exception {
        HTMLDocument doc = parse("<html><body><p>hi <a href=\"http://example.com/\">link</a></p></body></html>");

        HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
        check(it != null, "getIterator(A) non-null");

        int count = 0;
        while (it.isValid()) {
            check(it.getTag() == HTML.Tag.A, "iterator tag A");
            check(it.getStartOffset() >= 0, "start offset >= 0");
            check(it.getEndOffset() >= it.getStartOffset(), "end >= start");
            AttributeSet attrs = it.getAttributes();
            check(attrs != null, "attributes non-null");
            check("http://example.com/".equals(attrs.getAttribute(HTML.Attribute.HREF)), "href attribute");
            it.next();
            count++;
        }
        check(count == 1, "one <a> occurrence");
        check(it.getStartOffset() == -1, "exhausted start == -1");
        check(!it.isValid(), "exhausted not valid");

        check(doc.getIterator(HTML.Tag.P) == null, "block tag iterator null");
        check(doc.getIterator(HTML.Tag.A) != null, "new iterator each call");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
