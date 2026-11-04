/*
 * @test
 * @summary javax.swing.text.AbstractDocument.LeafElement conformance: white-box structure + black-box behavior.
 * @run main LeafElementTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

public class LeafElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$LeafElement");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$AbstractElement"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PlainDocument pd = new PlainDocument();
        AbstractDocument.LeafElement le = pd.new LeafElement(null, null, 3, 8);

        check(le.isLeaf(), "leaf isLeaf");
        check(le.getStartOffset() == 3 && le.getEndOffset() == 8, "leaf offset range");
        check("content".equals(le.getName()), "leaf name content");
        check(le.getElementCount() == 0, "leaf child count 0");
        check(le.getElement(0) == null, "leaf getElement null");
        check(le.getElementIndex(1) == -1, "leaf getElementIndex -1");
        check(!le.getAllowsChildren(), "leaf disallows children");
        check(le.children() == null, "leaf children null");
        check(le.toString().startsWith("LeafElement"), "toString prefix");

        // Leaf elements reached through the document tree track the text.
        PlainDocument doc = new PlainDocument();
        doc.insertString(0, "hello\nworld", null);
        Element root = doc.getDefaultRootElement();
        Element line0 = root.getElement(0);
        Element line1 = root.getElement(1);
        check(line0 instanceof AbstractDocument.LeafElement, "line element is LeafElement");
        check(line0.getStartOffset() == 0 && line0.getEndOffset() == 6, "line0 range");
        check(line1.getStartOffset() == 6 && line1.getEndOffset() == doc.getLength() + 1, "line1 range");
        check(doc.getParagraphElement(0) == line0, "getParagraphElement(0)");
        check(doc.getParagraphElement(6) == line1, "getParagraphElement(6)");
        check(line0.getParentElement() == root, "line parent is root");
        check(line0.getDocument() == doc, "line document");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
