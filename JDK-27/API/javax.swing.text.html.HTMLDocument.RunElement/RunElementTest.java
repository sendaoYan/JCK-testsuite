/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.RunElement conformance: white-box structure + black-box behavior.
 * @run main RunElementTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class RunElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$RunElement");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$LeafElement"), "superclass");
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
        HTMLDocument doc = parse("<html><body><p>hello world</p></body></html>");
        Element para = doc.getParagraphElement(1);
        Element leaf = doc.getCharacterElement(para.getStartOffset());
        check(leaf instanceof HTMLDocument.RunElement, "character element instanceof RunElement");
        check("content".equals(leaf.getName()), "RunElement getName content");

        // every character-level element under the paragraph is a RunElement leaf
        for (int i = para.getStartOffset(); i < para.getEndOffset(); i++) {
            Element el = doc.getCharacterElement(i);
            check(el instanceof HTMLDocument.RunElement, "leaf at offset " + i + " is RunElement");
        }

        Element leaf2 = doc.getCharacterElement(para.getStartOffset());
        check(leaf2.isLeaf(), "RunElement isLeaf");
        check(leaf2.getStartOffset() <= leaf2.getEndOffset(), "RunElement offsets ordered");
        check(leaf2.getParentElement() != null, "RunElement has parent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
