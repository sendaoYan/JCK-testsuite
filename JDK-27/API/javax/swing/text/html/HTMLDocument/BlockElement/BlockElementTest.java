/*
 * @test
 * @summary javax.swing.text.html.HTMLDocument.BlockElement conformance: white-box structure + black-box behavior.
 * @run main BlockElementTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class BlockElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLDocument$BlockElement");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$BranchElement"), "superclass");
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
        HTMLDocument doc = parse("<html><body><p>hello</p></body></html>");
        Element root = doc.getDefaultRootElement();
        check(root instanceof HTMLDocument.BlockElement, "root instanceof BlockElement");
        check("html".equals(root.getName()), "root getName html");

        Element head = root.getElement(0);
        Element body = root.getElement(1);
        check(head instanceof HTMLDocument.BlockElement, "head instanceof BlockElement");
        check(body instanceof HTMLDocument.BlockElement, "body instanceof BlockElement");
        check("head".equals(head.getName()), "head getName");
        check("body".equals(body.getName()), "body getName");

        Element para = doc.getParagraphElement(1);
        check(para instanceof HTMLDocument.BlockElement, "paragraph instanceof BlockElement");
        check("p".equals(para.getName()), "paragraph getName p");
        check(para.getParentElement() == body, "paragraph parent is body");
        check(para.getStartOffset() <= para.getEndOffset(), "paragraph offsets ordered");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
