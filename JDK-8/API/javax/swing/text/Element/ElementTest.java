/*
 * @test
 * @summary javax.swing.text.Element conformance: interface shape + element-tree contract over PlainDocument/DefaultStyledDocument (headless-safe).
 * @run main ElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

public class ElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Element", false, ElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDocument"), "method getDocument");
        check(names.contains("getParentElement"), "method getParentElement");
        check(names.contains("getName"), "method getName");
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("getStartOffset"), "method getStartOffset");
        check(names.contains("getEndOffset"), "method getEndOffset");
        check(names.contains("getElementIndex"), "method getElementIndex");
        check(names.contains("getElementCount"), "method getElementCount");
        check(names.contains("getElement"), "method getElement");
        check(names.contains("isLeaf"), "method isLeaf");

        testPlainTree();
        testStyledTree();
        testInvalidIndices();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testPlainTree() throws Exception {
        PlainDocument d = new PlainDocument();
        Element root = d.getDefaultRootElement();
        check(root.getDocument() == d, "root getDocument identity");
        check(root.getParentElement() == null, "root parent null");
        check(root.getName().equals("paragraph"), "plain root named paragraph");
        check(root.getAttributes() != null, "root attributes non-null");
        check(root.getStartOffset() == 0, "root start 0");
        check(root.getEndOffset() == 1, "empty root end 1");
        check(!root.isLeaf(), "root is branch");
        check(root.getElementCount() == 1, "root one child");
        check(root.getElement(0) != null, "getElement(0)");
        // indices at/above the child count read empty spare slots and yield null
        check(root.getElement(5) == null, "getElement beyond count is null");
        // a negative index throws instead
        try {
            root.getElement(-1);
            check(false, "getElement(-1) should throw AIOOBE");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "getElement(-1) throws AIOOBE");
        }

        d.insertString(0, "ab\ncd", null);
        check(root.getStartOffset() == 0 && root.getEndOffset() == 6, "root [0,6) after insert");
        check(root.getElementCount() == 2, "two line elements");
        Element l0 = root.getElement(0);
        Element l1 = root.getElement(1);
        check(l0.getStartOffset() == 0 && l0.getEndOffset() == 3, "line0 [0,3)");
        check(l1.getStartOffset() == 3 && l1.getEndOffset() == 6, "line1 [3,6)");
        check(l0.getParentElement() == root && l1.getParentElement() == root, "children parent identity");
        check(l0.isLeaf() && l1.isLeaf(), "plain lines are leaves");
        check(l0.getName().equals("content"), "plain line named content");
        check(l0.getElementCount() == 0, "leaf has no children");
        check(l0.getDocument() == d, "leaf getDocument identity");
        // offset tracking after further edits
        d.insertString(1, "\n", null); // "a\nb\ncd"
        check(root.getElementCount() == 3, "insert splits element");
        check(root.getElement(1).getStartOffset() == 2 && root.getElement(1).getEndOffset() == 4, "middle element spans [2,4)");
        d.remove(1, 1);
        check(root.getElementCount() == 2, "remove merges elements");
        check(d.getText(0, d.getLength()).equals("ab\ncd"), "content after merge");
    }

    static void testStyledTree() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        d.insertString(0, "ab\ncd", null);
        Element sec = d.getDefaultRootElement();
        check(sec.getName().equals("section"), "styled root named section");
        check(sec.getElementCount() == 2, "two paragraphs");
        Element p0 = sec.getElement(0);
        check(p0.getName().equals("paragraph"), "paragraph named");
        check(!p0.isLeaf(), "paragraph is branch");
        check(p0.getElementCount() == 1, "paragraph one content run");
        Element run = p0.getElement(0);
        check(run.getName().equals("content"), "run named content");
        check(run.isLeaf(), "run is leaf");
        check(run.getStartOffset() == 0 && run.getEndOffset() == 3, "run [0,3)");
        check(sec.getStartOffset() == 0 && sec.getEndOffset() == 6, "section [0,6)");
        // three-level parent chain
        check(run.getParentElement() == p0 && p0.getParentElement() == sec && sec.getParentElement() == null,
                "full parent chain");
    }

    static void testInvalidIndices() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "a\nb", null);
        Element root = d.getDefaultRootElement();
        // out-of-range index positions clamp into the valid range
        check(root.getElementIndex(0) == 0, "index(0)");
        check(root.getElementIndex(1) == 0, "index(1) first line");
        check(root.getElementIndex(2) == 1, "index(2) second line");
        check(root.getElementIndex(100) == 1, "index(100) clamps to last");
        check(root.getElementIndex(-1) == 0, "index(-1) clamps to first");
        // leaves have no children to index
        Element leaf = root.getElement(0);
        check(leaf.getElementIndex(0) == -1, "leaf index -1");
        check(leaf.getElement(0) == null, "leaf getElement null");
        check(leaf.getElementCount() == 0, "leaf count 0");
        // attributes of a plain leaf are empty but never null
        check(leaf.getAttributes() != null && leaf.getAttributes().getAttributeCount() == 0, "leaf attrs empty");
        Document doc = leaf.getDocument();
        check(doc == d, "getDocument consistent");
    }
}
