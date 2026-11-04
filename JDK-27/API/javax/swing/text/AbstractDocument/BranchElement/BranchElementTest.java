/*
 * @test
 * @summary javax.swing.text.AbstractDocument.BranchElement conformance: white-box structure + black-box behavior.
 * @run main BranchElementTest
 */

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.tree.TreeNode;

public class BranchElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$BranchElement");
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
        AbstractDocument.BranchElement be = pd.new BranchElement(null, null);

        check(!be.isLeaf(), "branch is not a leaf");
        check(be.getElementCount() == 0, "fresh branch has 0 children");
        check("paragraph".equals(be.getName()), "branch default name paragraph");
        check(be.getAllowsChildren(), "branch allows children");
        check(be.children() == null, "children() null when empty");

        Element c0 = pd.new LeafElement(be, null, 0, 1);
        Element c1 = pd.new LeafElement(be, null, 1, 2);
        be.replace(0, 0, new Element[]{c0, c1});

        check(be.getElementCount() == 2, "replace inserts 2 children");
        check(be.getStartOffset() == 0 && be.getEndOffset() == 2, "branch range from children");
        check(be.getElement(0) == c0 && be.getElement(1) == c1, "getElement by index");
        check(be.getElementIndex(0) == 0 && be.getElementIndex(1) == 1, "getElementIndex by offset");
        check(be.positionToElement(0) == c0 && be.positionToElement(1) == c1, "positionToElement");
        check(be.positionToElement(5) == null, "positionToElement out of range null");

        Enumeration<TreeNode> en = be.children();
        int cnt = 0;
        while (en.hasMoreElements()) { en.nextElement(); cnt++; }
        check(cnt == 2, "children enumeration count 2");
        check(be.toString().startsWith("BranchElement"), "toString prefix");

        // Document default root is a BranchElement whose children are lines.
        PlainDocument doc = new PlainDocument();
        doc.insertString(0, "a\nb\nc", null);
        Element root = doc.getDefaultRootElement();
        check(root instanceof AbstractDocument.BranchElement, "default root is BranchElement");
        check(root.getElementCount() == 3, "three line elements");
        check(root.getElement(0).getStartOffset() == 0 && root.getElement(0).getEndOffset() == 2, "first line range");
        check(doc.getParagraphElement(4) == root.getElement(2), "getParagraphElement maps into line element");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
