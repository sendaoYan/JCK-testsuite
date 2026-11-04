/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.SectionElement conformance: white-box structure + black-box behavior.
 * @run main SectionElementTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

public class SectionElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$SectionElement");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$BranchElement"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DefaultStyledDocument doc = new DefaultStyledDocument();
        Element root = doc.getDefaultRootElement();

        check(root != null, "root non-null");
        check(Class.forName("javax.swing.text.DefaultStyledDocument$SectionElement").isInstance(root),
              "root is a SectionElement");
        check("section".equals(root.getName()), "section name");
        check(!root.isLeaf(), "section is not a leaf");
        check(((javax.swing.tree.TreeNode) root).getAllowsChildren(), "section allows children");
        check(root.getElementCount() == 1, "section initially one paragraph");
        check(root.getStartOffset() == 0, "section start offset 0");
        check(root.getEndOffset() == doc.getLength() + 1, "section end covers trailing newline");

        // Paragraph structure updates as text is added.
        doc.insertString(0, "one\ntwo\nthree", null);
        check(root.getElementCount() == 3, "three paragraphs after insert");
        check(root.getEndOffset() == doc.getLength() + 1, "section end still covers trailing newline");
        check(doc.getParagraphElement(0) == root.getElement(0), "paragraph 0 is first section child");
        check(doc.getParagraphElement(4) == root.getElement(1), "paragraph 1 is second section child");
        check(root.getParentElement() == null, "section has no parent");
        check(root.getDocument() == doc, "section document");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
